package amf.plugins.document.apicontract.contexts.parser

import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.ExternalFragment
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.parser.YMapOps
import amf.plugins.document.apicontract.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.apicontract.contexts.{SpecVersionFactory, WebApiContext}
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.OasLikeWebApiDeclarations
import amf.plugins.document.apicontract.parser.spec.declaration.OasLikeSecuritySettingsParser
import amf.plugins.document.apicontract.parser.spec.domain.{
  OasLikeEndpointParser,
  OasLikeOperationParser,
  OasLikeServerVariableParser,
  ParsingHelpers
}
import amf.plugins.document.apicontract.parser.spec.jsonschema.JsonSchemaParser
import amf.plugins.domain.apicontract.models.security.SecurityScheme
import amf.plugins.domain.apicontract.models.{EndPoint, Operation}
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}

import scala.collection.mutable

trait OasLikeSpecVersionFactory extends SpecVersionFactory {
  def serverVariableParser(entry: YMapEntry, parent: String): OasLikeServerVariableParser
  // TODO ASYNC complete this
  def operationParser(entry: YMapEntry, adopt: Operation => Operation): OasLikeOperationParser
  def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser
  def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser
}

abstract class OasLikeWebApiContext(loc: String,
                                    refs: Seq[ParsedReference],
                                    options: ParsingOptions,
                                    private val wrapped: ParserContext,
                                    private val ds: Option[OasLikeWebApiDeclarations] = None,
                                    private val operationIds: mutable.Set[String] = mutable.HashSet())
    extends WebApiContext(loc, refs, options, wrapped, ds) {

  val factory: OasLikeSpecVersionFactory

  def makeCopy(): OasLikeWebApiContext
  private def makeCopyWithJsonPointerContext() = {
    val copy = makeCopy()
    copy.jsonSchemaRefGuide = this.jsonSchemaRefGuide
    copy.indexCache = this.indexCache
    copy
  }

  def isMainFileContext: Boolean = loc == jsonSchemaRefGuide.currentLoc

  override val declarations: OasLikeWebApiDeclarations =
    ds.getOrElse(
      new OasLikeWebApiDeclarations(
        refs
          .flatMap(
            r =>
              if (r.isExternalFragment)
                r.unit.asInstanceOf[ExternalFragment].encodes.parsed.map(node => r.origin.url -> node)
              else None)
          .toMap,
        None,
        errorHandler = eh,
        futureDeclarations = futureDeclarations
      ))

  override def link(node: YNode): Either[String, YNode] = {
    node.to[YMap] match {
      case Right(map) =>
        val ref: Option[String] = map.key("$ref").flatMap(v => v.value.asOption[YScalar]).map(_.text)
        ref match {
          case Some(url) => Left(url)
          case None      => Right(node)
        }
      case _ => Right(node)
    }
  }

  val linkTypes: Boolean = wrapped match {
    case _: RamlWebApiContext => false
    case _                    => true
  }

  val shapesThatDontPermitRef = List("paths", "operation")

  override def ignore(shape: String, property: String): Boolean =
    property.startsWith("x-") || (property == "$ref" && !shapesThatDontPermitRef.contains(shape)) || (property
      .startsWith("/") && shape == "paths")

  /** Used for accumulating operation ids.
    * returns true if id was not present, and false if operation being added is already present. */
  def registerOperationId(id: String): Boolean = operationIds.add(id)

  def navigateToRemoteYNode[T <: OasLikeWebApiContext](ref: String)(implicit ctx: T): Option[RemoteNodeNavigation[T]] = {
    val nodeOption = jsonSchemaRefGuide.obtainRemoteYNode(ref)
    val rootNode   = jsonSchemaRefGuide.getRootYNode(ref)
    nodeOption.map { node =>
      val newCtx = ctx.makeCopyWithJsonPointerContext().moveToReference(node.location.sourceName).asInstanceOf[T]
      rootNode.foreach(newCtx.setJsonSchemaAST)
      RemoteNodeNavigation(node, newCtx)
    }
  }

  def parseRemoteJSONPath(ref: String): Option[AnyShape] = {
    jsonSchemaRefGuide.withFragmentAndInFileReference(ref) { (fragment, referenceUrl) =>
      val newCtx = makeCopyWithJsonPointerContext().moveToReference(fragment.location().get)
      new JsonSchemaParser().parse(fragment, referenceUrl)(WebApiShapeParserContextAdapter(newCtx))
    }
  }

  def moveToReference(loc: String): this.type = {
    jsonSchemaRefGuide = jsonSchemaRefGuide.changeJsonSchemaSearchDestination(loc)
    this
  }

  override def autoGeneratedAnnotation(s: Shape): Unit = ParsingHelpers.oasAutoGeneratedAnnotation(s)
}

case class RemoteNodeNavigation[T <: OasLikeWebApiContext](remoteNode: YNode, context: T)

object RemoteNodeNavigation {

  def unapply[T <: OasLikeWebApiContext](arg: RemoteNodeNavigation[T]): Option[(YNode, T)] =
    Some((arg.remoteNode, arg.context))
}
