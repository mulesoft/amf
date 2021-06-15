package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.core.internal.utils.AmfStrings
import amf.core.internal.validation.CoreValidations
import amf.plugins.document.apicontract.annotations.ExternalReferenceUrl
import amf.shapes.internal.spec.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.ErrorSecurityScheme
import amf.plugins.document.apicontract.parser.spec.common.AnnotationParser
import amf.plugins.document.apicontract.parser.spec.toRaml
import amf.plugins.domain.apicontract.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.apicontract.models.security.SecurityScheme
import amf.validations.ParserSideValidations.{
  CrossSecurityWarningSpecification,
  MissingSecuritySchemeErrorSpecification
}
import amf.core.client.common.position.Range
import org.yaml.model.{YMap, YNode, YPart, YType}

abstract class OasLikeSecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(
    implicit ctx: OasLikeWebApiContext)
    extends SecuritySchemeParser(part, adopt) {

  override def parse(): SecurityScheme = {
    val node = getNode

    ctx.link(node) match {
      case Left(link) => parseReferenced(link, node, adopt)
      case Right(value) =>
        val scheme = adopt(SecurityScheme(part))
        val map    = value.as[YMap]

        parseType(map, scheme)

        map.key("displayName".asOasExtension, SecuritySchemeModel.DisplayName in scheme)
        map.key("description", SecuritySchemeModel.Description in scheme)

        RamlDescribedByParser("describedBy".asOasExtension, map, scheme)(toRaml(ctx)).parse()

        ctx.factory
          .securitySettingsParser(map, scheme)
          .parse()
          .map { settings =>
            scheme.set(SecuritySchemeModel.Settings, settings, Annotations(map))
          }

        AnnotationParser(scheme, map)(WebApiShapeParserContextAdapter(ctx)).parse()
        closedShape(scheme, map, "securityScheme")
        scheme
    }
  }

  protected def closedShape(scheme: SecurityScheme, map: YMap, shape: String): Unit =
    ctx.closedShape(scheme.id, map, shape)

  def parseType(map: YMap, scheme: SecurityScheme): Unit = {
    map.key("type", SecuritySchemeModel.Type in scheme)

    scheme.`type`.option() match {
      case Some("OAuth 1.0" | "OAuth 2.0" | "Basic Authentication" | "Digest Authentication" | "Pass Through") =>
        ctx.eh.warning(
          CrossSecurityWarningSpecification,
          scheme.id,
          Some(SecuritySchemeModel.Type.value.iri()),
          s"RAML 1.0 security scheme type detected in OAS 2.0 spec",
          scheme.`type`.annotations().find(classOf[LexicalInformation]),
          Some(ctx.rootContextDocument)
        )
      case Some(s) if s.startsWith("x-") =>
        ctx.eh.warning(
          CrossSecurityWarningSpecification,
          scheme.id,
          Some(SecuritySchemeModel.Type.value.iri()),
          s"RAML 1.0 extension security scheme type '$s' detected in ${ctx.vendor.name} spec",
          scheme.`type`.annotations().find(classOf[LexicalInformation]),
          Some(ctx.rootContextDocument)
        )
      case _ =>
    }

    map.key(
      "type",
      value => {
        // we need to check this because of the problem parsing nulls like empty strings of value null
        if (value.value.tagType == YType.Null && scheme.`type`.option().contains("")) {
          ctx.eh.violation(
            MissingSecuritySchemeErrorSpecification,
            scheme.id,
            Some(SecuritySchemeModel.Type.value.iri()),
            "Security Scheme must have a mandatory value from 'oauth2', 'basic' or 'apiKey'",
            Some(LexicalInformation(Range(map.range))),
            Some(ctx.rootContextDocument)
          )
        }
      }
    )

    scheme.normalizeType() // normalize the common type
  }

  def parseReferenced(parsedUrl: String, node: YNode, adopt: SecurityScheme => SecurityScheme): SecurityScheme = {
    ctx.declarations
      .findSecurityScheme(parsedUrl, SearchScope.Fragments)
      .map(securityScheme => {
        val scheme: SecurityScheme =
          securityScheme.link(AmfScalar(parsedUrl), Annotations(node), Annotations.synthesized())
        adopt(scheme)
        scheme
      })
      .getOrElse {
        ctx.obtainRemoteYNode(parsedUrl) match {
          case Some(schemeNode) =>
            ctx.factory.securitySchemeParser(schemeNode, adopt).parse().add(ExternalReferenceUrl(parsedUrl))
          case None =>
            ctx.eh.violation(CoreValidations.UnresolvedReference,
                             "",
                             s"Cannot find security scheme reference $parsedUrl",
                             Annotations(node))
            adopt(ErrorSecurityScheme(parsedUrl, node))
        }
      }
  }

}
