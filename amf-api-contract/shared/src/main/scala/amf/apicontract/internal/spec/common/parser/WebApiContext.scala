package amf.apicontract.internal.spec.common.parser

import amf.aml.client.scala.model.document.Dialect
import amf.aml.internal.parse.common.DeclarationContext
import amf.aml.internal.registries.AMLRegistry
import amf.aml.internal.semantic.{SemanticExtensionsFacade, SemanticExtensionsFacadeBuilder}
import amf.apicontract.internal.spec.common.emitter.SpecAwareContext
import amf.apicontract.internal.spec.common.{OasParameter, WebApiDeclarations}
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  ClosedShapeSpecification,
  ClosedShapeSpecificationWarning
}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.{ExternalFragment, Fragment, RecursiveUnit}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{AmfObject, Shape}
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.datanode.DataNodeParserContext
import amf.core.internal.parser._
import amf.core.internal.parser.domain.{Annotations, FragmentRef, SearchScope}
import amf.core.internal.plugins.syntax.{SYamlAMFParserErrorHandler, SyamlAMFErrorHandler}
import amf.core.internal.remote.Spec
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils.{AliasCounter, IdCounter, QName}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.parser.{SpecSyntax, YMapEntryLike}
import amf.shapes.internal.spec.common.{JSONSchemaDraft4SchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.contexts.JsonSchemaRefGuide
import amf.shapes.internal.spec.jsonschema.ref.{AstFinder, AstIndex, AstIndexBuilder, JsonSchemaInference}
import org.mulesoft.lexer.SourceLocation
import org.yaml.model._

import scala.collection.mutable

abstract class ExtensionsContext(val loc: String,
                                 refs: Seq[ParsedReference],
                                 val options: ParsingOptions,
                                 wrapped: ParserContext,
                                 val declarationsOption: Option[WebApiDeclarations] = None,
                                 val nodeRefIds: mutable.Map[YNode, String] = mutable.Map.empty)
    extends ParserContext(loc, refs, wrapped.futureDeclarations, wrapped.config)
    with DataNodeParserContext {

  private def getExtensionsMap: Map[String, Dialect] = wrapped.config.registryContext.getRegistry match {
    case amlRegistry: AMLRegistry => amlRegistry.getExtensionRegistry
    case _                        => Map.empty
  }

  val declarations: WebApiDeclarations = declarationsOption.getOrElse(
    new WebApiDeclarations(None,
                           errorHandler = eh,
                           futureDeclarations = futureDeclarations,
                           extensions = getExtensionsMap))

  override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] =
    declarations.findAnnotation(key, scope)

  override def getMaxYamlReferences: Option[Int] = options.getMaxYamlReferences

  override def fragments: Map[String, FragmentRef] = declarations.fragments
}

abstract class WebApiContext(loc: String,
                             refs: Seq[ParsedReference],
                             options: ParsingOptions,
                             wrapped: ParserContext,
                             declarationsOption: Option[WebApiDeclarations] = None,
                             nodeRefIds: mutable.Map[YNode, String] = mutable.Map.empty)
    extends ExtensionsContext(loc, refs, options, wrapped, declarationsOption, nodeRefIds)
    with DeclarationContext
    with SpecAwareContext
    with PlatformSecrets
    with JsonSchemaInference
    with ParseErrorHandler
    with IllegalTypeHandler {

  val syamleh                                                            = new SyamlAMFErrorHandler(wrapped.config.eh)
  override def handle[T](error: YError, defaultValue: T): T              = syamleh.handle(error, defaultValue)
  override def handle(location: SourceLocation, e: SyamlException): Unit = syamleh.handle(location, e)

  override val defaultSchemaVersion: SchemaVersion = JSONSchemaDraft4SchemaVersion

  def validateRefFormatWithError(ref: String): Boolean = true

  val syntax: SpecSyntax
  val spec: Spec

  val extensionsFacadeBuilder: SemanticExtensionsFacadeBuilder = WebApiSemanticExtensionsFacadeBuilder()

  var localJSONSchemaContext: Option[YNode] = wrapped match {
    case wac: WebApiContext => wac.localJSONSchemaContext
    case _                  => None
  }

  private var jsonSchemaIndex: Option[AstIndex] = wrapped match {
    case wac: WebApiContext => wac.jsonSchemaIndex
    case _                  => None
  }

  var jsonSchemaRefGuide: JsonSchemaRefGuide = JsonSchemaRefGuide(loc, refs)(WebApiShapeParserContextAdapter(this))

  var indexCache: mutable.Map[String, AstIndex] = mutable.Map[String, AstIndex]()

  def setJsonSchemaAST(value: YNode): Unit = {
    val location = value.sourceName
    localJSONSchemaContext = Some(value)
    val index = indexCache.getOrElse(
      location, {
        val result = AstIndexBuilder.buildAst(value,
                                              AliasCounter(options.getMaxYamlReferences),
                                              computeJsonSchemaVersion(value))(WebApiShapeParserContextAdapter(this))
        indexCache.put(location, result)
        result
      }
    )
    jsonSchemaIndex = Some(index)
  }

  globalSpace = wrapped.globalSpace

  // JSON Schema has a global namespace

  protected def normalizedJsonPointer(url: String): String = if (url.endsWith("/")) url.dropRight(1) else url

  def findJsonSchema(url: String): Option[AnyShape] =
    globalSpace
      .get(normalizedJsonPointer(url))
      .collect { case shape: AnyShape => shape }

  def registerJsonSchema(url: String, shape: AnyShape): Unit =
    globalSpace.update(normalizedJsonPointer(url), shape)

  // TODO this should not have OasWebApiContext as a dependency
  def parseRemoteOasParameter(fileUrl: String, parentId: String)(
      implicit ctx: OasWebApiContext): Option[OasParameter] = {
    val referenceUrl = getReferenceUrl(fileUrl)
    obtainFragment(fileUrl) flatMap { fragment =>
      AstFinder.findAst(fragment, referenceUrl)(WebApiShapeParserContextAdapter(ctx)).map { node =>
        ctx.factory
          .parameterParser(YMapEntryLike(node)(new SYamlAMFParserErrorHandler(ctx.eh)),
                           parentId,
                           None,
                           new IdCounter())
          .parse
      }
    }
  }

  def obtainRemoteYNode(ref: String, refAnnotations: Annotations = Annotations())(
      implicit ctx: WebApiContext): Option[YNode] = {
    jsonSchemaRefGuide.obtainRemoteYNode(ref)
  }

  private def obtainFragment(fileUrl: String): Option[Fragment] = {
    val baseFileUrl = fileUrl.split("#").head
    refs
      .filter(r => r.unit.location().isDefined)
      .filter(_.unit.location().get == baseFileUrl) collectFirst {
      case ref if ref.unit.isInstanceOf[ExternalFragment] =>
        ref.unit.asInstanceOf[ExternalFragment]
      case ref if ref.unit.isInstanceOf[RecursiveUnit] =>
        ref.unit.asInstanceOf[RecursiveUnit]
    }
  }

  private def getReferenceUrl(fileUrl: String): Option[String] = {
    fileUrl.split("#") match {
      case s: Array[String] if s.size > 1 => Some(s.last)
      case _                              => None
    }
  }

  def computeJsonSchemaVersion(ast: YNode): SchemaVersion = parseSchemaVersion(ast, eh)

  private def normalizeJsonPath(path: String): String = {
    if (path == "#" || path == "" || path == "/") "/" // exception root cases
    else {
      val s = if (path.startsWith("#/")) path.replace("#/", "") else path
      if (s.startsWith("/")) s.stripPrefix("/") else s
    }
  }

  def findJsonPathIn(index: AstIndex, path: String): Option[YMapEntryLike] = index.getNode(normalizeJsonPath(path))

  def findLocalJSONPath(path: String): Option[YMapEntryLike] = {
    jsonSchemaIndex.flatMap(index => findJsonPathIn(index, path))
  }

  def link(node: YNode): Either[String, YNode]
  protected def ignore(shape: String, property: String): Boolean
  def autoGeneratedAnnotation(s: Shape): Unit

  /** Validate closed shape. */
  def closedShape(node: AmfObject, ast: YMap, shape: String): Unit = closedShape(node, ast, shape, syntax)

  protected def closedShape(node: AmfObject, ast: YMap, shape: String, syntax: SpecSyntax): Unit =
    syntax.nodes.get(shape) match {
      case Some(properties) =>
        ast.entries.foreach { entry =>
          val key: String = getEntryKey(entry)
          if (!ignore(shape, key) && !properties(key)) {
            throwClosedShapeError(node, s"Property '$key' not supported in a $spec $shape node", entry)
          }
        }
      case None => nextValidation(node, shape, ast)
    }

  def getEntryKey(entry: YMapEntry): String = {
    entry.key.asOption[YScalar].map(_.text).getOrElse(entry.key.toString)
  }

  protected def nextValidation(node: AmfObject, shape: String, ast: YMap): Unit =
    throwClosedShapeError(node, s"Cannot validate unknown node type $shape for $spec", ast)

  protected def throwClosedShapeError(node: AmfObject,
                                      message: String,
                                      entry: YPart,
                                      isWarning: Boolean = false): Unit =
    if (isWarning) eh.warning(ClosedShapeSpecificationWarning, node, message, entry.location)
    else eh.violation(ClosedShapeSpecification, node, message, entry.location)

  case class WebApiSemanticExtensionsFacadeBuilder() extends SemanticExtensionsFacadeBuilder {
    override def extensionName(name: String): SemanticExtensionsFacade = {
      val fqn = QName(name)
      val dialect = if (fqn.isQualified) {
        val maybeDeclarations: Option[WebApiDeclarations] =
          declarations.libraries.get(fqn.qualification).collectFirst({ case w: WebApiDeclarations => w })
        maybeDeclarations.flatMap(_.extensions.get(fqn.name))
      } else declarations.extensions.get(name)
      dialect.map(SemanticExtensionsFacade(fqn.name, _)).getOrElse(SemanticExtensionsFacade(name, wrapped.config))
    }
  }
}
