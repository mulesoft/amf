package amf.shapes.internal.spec.oas.parser

import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec.OAS31
import amf.shapes.internal.spec.common.parser._
import amf.shapes.internal.spec.common.{OAS31SchemaVersion, SchemaPosition, SchemaVersion}
import amf.shapes.internal.spec.raml.parser.RamlWebApiContextType.RamlWebApiContextType
import org.yaml.model.YNode

case class Oas31Settings(syntax: SpecSyntax) extends SpecSettings {
  override val spec: Spec = OAS31

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = OasLink.getLinkValue(node)

  override def ignoreCriteria: IgnoreCriteria = OasLikeIgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType] = None

  override val defaultSchemaVersion: SchemaVersion = OAS31SchemaVersion.apply(SchemaPosition.Other)
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = IgnoreAnnotationSchemaValidatorBuilder

  override def shouldLinkTypes(parent: ParserContext) = parent match {
    case ctx: ShapeParserContext if ctx.isRamlContext => false
    case _                                            => true
  }
}
