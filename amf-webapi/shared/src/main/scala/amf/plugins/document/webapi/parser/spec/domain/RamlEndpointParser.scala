package amf.plugins.document.webapi.parser.spec.domain

import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.utils.TemplateUri
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.domain.webapi.annotations.ParentEndPoint
import amf.plugins.domain.webapi.metamodel.EndPointModel
import amf.plugins.domain.webapi.metamodel.EndPointModel._
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter}
import org.yaml.model.{YMap, YMapEntry, YNode, YType}

import scala.collection.mutable

/**
  *
  */
case class Raml10EndpointParser(entry: YMapEntry,
                                producer: String => EndPoint,
                                parent: Option[EndPoint],
                                collector: mutable.ListBuffer[EndPoint],
                                parseOptionalOperations: Boolean = false)(implicit ctx: WebApiContext)
    extends RamlEndpointParser(entry, producer, parent, collector, parseOptionalOperations) {

  override def operationParser: (YMapEntry, (String) => Operation, Boolean) => RamlOperationParser =
    Raml10OperationParser.apply

  override def endpointParser
    : (YMapEntry, String => EndPoint, Option[EndPoint], mutable.ListBuffer[EndPoint], Boolean) => RamlEndpointParser =
    Raml10EndpointParser.apply

  override def parametersParser: (YMap, (String) => Parameter) => RamlParametersParser = Raml10ParametersParser.apply

  override protected def uriParametersKey: String = "uriParameters"
}

case class Raml08EndpointParser(entry: YMapEntry,
                                producer: String => EndPoint,
                                parent: Option[EndPoint],
                                collector: mutable.ListBuffer[EndPoint],
                                parseOptionalOperations: Boolean = false)(implicit ctx: WebApiContext)
    extends RamlEndpointParser(entry, producer, parent, collector, parseOptionalOperations) {

  override def operationParser: (YMapEntry, (String) => Operation, Boolean) => RamlOperationParser =
    Raml08OperationParser.apply

  override def endpointParser
    : (YMapEntry, String => EndPoint, Option[EndPoint], mutable.ListBuffer[EndPoint], Boolean) => RamlEndpointParser =
    Raml08EndpointParser.apply

  override def parametersParser: (YMap, (String) => Parameter) => RamlParametersParser = Raml08ParametersParser.apply

  override protected def uriParametersKey: String = "BaseUriParameter"
}

abstract class RamlEndpointParser(entry: YMapEntry,
                                  producer: String => EndPoint,
                                  parent: Option[EndPoint],
                                  collector: mutable.ListBuffer[EndPoint],
                                  parseOptionalOperations: Boolean = false)(implicit ctx: WebApiContext) {

  def operationParser: (YMapEntry, (String) => Operation, Boolean) => RamlOperationParser

  def endpointParser
    : (YMapEntry, String => EndPoint, Option[EndPoint], mutable.ListBuffer[EndPoint], Boolean) => RamlEndpointParser

  def parametersParser: (YMap, (String) => Parameter) => RamlParametersParser

  def parse(): Unit = {

    val path = parent.map(_.path).getOrElse("") + entry.key.as[String]

    val endpoint = producer(path).add(Annotations(entry))
    parent.map(p => endpoint.add(ParentEndPoint(p)))

    endpoint.set(Path, AmfScalar(path, Annotations(entry.key)))

    if (!TemplateUri.isValid(path))
      ctx.violation(endpoint.id, TemplateUri.invalidMsg(path), entry.value)

    if (collector.exists(e => e.path == path)) ctx.violation(endpoint.id, "Duplicated resource path " + path, entry)
    else {
      entry.value.tagType match {
        case YType.Null => collector += endpoint
        case _ =>
          val map = entry.value.as[YMap]
          parseEndpoint(endpoint, map)
      }
    }
  }

  protected def parseEndpoint(endpoint: EndPoint, map: YMap): Unit = {
    ctx.closedShape(endpoint.id, map, "endPoint")

    map.key("displayName", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Name, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "type",
      entry =>
        ParametrizedDeclarationParser(entry.value,
                                      endpoint.withResourceType,
                                      ctx.declarations.findResourceTypeOrError(entry.value))
          .parse()
    )

    map.key(
      "is",
      entry => {
        entry.value
          .as[Seq[YNode]]
          .map(value =>
            ParametrizedDeclarationParser(value, endpoint.withTrait, ctx.declarations.findTraitOrError(value))
              .parse())
      }
    )

    val optionalMethod = if (parseOptionalOperations) "\\??" else ""

    map.regex(
      s"(get|patch|put|post|delete|options|head)$optionalMethod",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach(entry => {
          operations += operationParser(entry, endpoint.withOperation, parseOptionalOperations)
            .parse()
        })
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )

    map.key(
      "securedBy",
      entry => {
        // TODO check for empty array for resolution ?
        val securedBy = entry.value
          .as[Seq[YNode]]
          .map(s => RamlParametrizedSecuritySchemeParser(s, endpoint.withSecurity).parse())

        endpoint.set(EndPointModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      uriParametersKey,
      entry => {
        val parameters: Seq[Parameter] =
          parametersParser(entry.value.as[YMap], endpoint.withParameter)
            .parse()
            .map(_.withBinding("path"))
        endpoint.set(EndPointModel.UriParameters, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    collector += endpoint

    AnnotationParser(() => endpoint, map).parse()

    map.regex(
      "^/.*",
      entries => {
        entries.foreach(endpointParser(_, producer, Some(endpoint), collector, false).parse())
      }
    )
  }

  protected def uriParametersKey: String
}
