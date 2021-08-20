package amf.apicontract.internal.spec.raml.parser.domain

import amf.apicontract.client.scala.model.domain.templates.ParametrizedResourceType
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter}
import amf.apicontract.internal.annotations.{EndPointResourceTypeEntry, EndPointTraitEntry, ParentEndPoint}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, ParameterModel}
import amf.apicontract.internal.spec.common.Parameters
import amf.apicontract.internal.spec.common.parser.{
  OasParametersParser,
  RamlSecurityRequirementParser,
  SpecParserOps,
  WebApiShapeParserContextAdapter
}
import amf.apicontract.internal.spec.raml.parser.context.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.apicontract.internal.spec.spec
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  DuplicatedEndpointPath,
  InvalidEndpointPath,
  SlashInUriParameterValues,
  UnusedBaseUriParameter
}
import amf.apicontract.internal.validation.definitions.ResolutionSideValidations.NestedEndpoint
import amf.core.client.common.position.Range
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DataNode, Shape, ScalarNode => ScalarDataNode}
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.annotations.{LexicalInformation, VirtualElement}
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{CompilerConfiguration, YMapOps}
import amf.core.internal.utils.{AmfStrings, IdCounter, TemplateUri}
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.internal.spec.RamlWebApiContextType
import amf.shapes.internal.spec.common.parser.AnnotationParser
import amf.shapes.internal.vocabulary.VocabularyMappings
import org.yaml.model._

import scala.collection.mutable

/**
  *
  */
case class Raml10EndpointParser(entry: YMapEntry,
                                producer: String => EndPoint,
                                parent: Option[EndPoint],
                                collector: mutable.ListBuffer[EndPoint],
                                parseOptionalOperations: Boolean = false)(implicit ctx: RamlWebApiContext)
    extends RamlEndpointParser(entry, producer, parent, collector, parseOptionalOperations) {

  override protected def uriParametersKey: String = "uriParameters"
}

case class Raml08EndpointParser(entry: YMapEntry,
                                producer: String => EndPoint,
                                parent: Option[EndPoint],
                                collector: mutable.ListBuffer[EndPoint],
                                parseOptionalOperations: Boolean = false)(implicit ctx: RamlWebApiContext)
    extends RamlEndpointParser(entry, producer, parent, collector, parseOptionalOperations) {

  override protected def uriParametersKey: String = "uriParameters|baseUriParameters"
}

abstract class RamlEndpointParser(entry: YMapEntry,
                                  producer: String => EndPoint,
                                  parent: Option[EndPoint],
                                  collector: mutable.ListBuffer[EndPoint],
                                  parseOptionalOperations: Boolean = false)(implicit ctx: RamlWebApiContext)
    extends SpecParserOps {

  def parse(): Unit = {

    val path = parsePath()

    val endpoint = producer(path).add(Annotations(entry))
    parent.map(p => endpoint.add(ParentEndPoint(p)))

    checkBalancedParams(path, entry.value, endpoint, EndPointModel.Path.value.iri(), ctx)
    endpoint.setWithoutId(EndPointModel.Path, AmfScalar(path, Annotations(entry.key)), Annotations(entry.key))

    if (!TemplateUri.isValid(path))
      ctx.eh.violation(InvalidEndpointPath, endpoint, TemplateUri.invalidMsg(path), entry.value.location)

    val duplicated = collector.find(e => e.path.is(path))
    duplicated match {
      case None =>
        entry.value.tagType match {
          case YType.Null => collector += endpoint
          case _ =>
            val map = entry.value.as[YMap]
            parseEndpoint(endpoint, map)
        }
      case Some(alreadyDefined) =>
        ctx.eh.violation(DuplicatedEndpointPath, alreadyDefined, "Duplicated resource path " + path, entry.location)
    }
  }

  protected def parseEndpoint(endpoint: EndPoint, map: YMap): Unit = {
    val isResourceType = ctx.contextType == RamlWebApiContextType.RESOURCE_TYPE
    ctx.closedShape(endpoint, map, if (isResourceType) "resourceType" else "endPoint")

    map.key("displayName", (EndPointModel.Name in endpoint).allowingAnnotations)
    map.key("description", (EndPointModel.Description in endpoint).allowingAnnotations)

    map.key(
      "is",
      (e: YMapEntry) => {
        endpoint.annotations += EndPointTraitEntry(Range(e.range))
        (EndPointModel.Extends in endpoint using ParametrizedDeclarationParser
          .parse(endpoint.withTrait)).allowingSingleValue.optional(e)
      }
    )

    map.key(
      "type",
      entry => {
        endpoint.annotations += EndPointResourceTypeEntry(Range(entry.range))
        val declaration = ParametrizedDeclarationParser(
          entry.value,
          (name: String) => ParametrizedResourceType().withName(name),
          ctx.declarations.findResourceTypeOrError(entry.value))
          .parse()
        endpoint.setWithoutId(EndPointModel.Extends,
                     AmfArray(Seq(declaration) ++ endpoint.traits, Annotations(Annotations.virtual())),
                     Annotations(Annotations.inferred()))
      }
    )

    val optionalMethod = if (parseOptionalOperations) "\\??" else ""

    map.regex(
      s"(get|patch|put|post|delete|options|head|connect|trace)$optionalMethod",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach(entry => {

          val operationContext = ctx match {
            case _: Raml08WebApiContext =>
              new Raml08WebApiContext(ctx.loc,
                                      ctx.refs,
                                      ParserContext(config = ctx.wrapped.config),
                                      Some(ctx.declarations),
                                      ctx.contextType,
                                      ctx.options)
            case _ =>
              new Raml10WebApiContext(ctx.loc,
                                      ctx.refs,
                                      ParserContext(config = ctx.wrapped.config),
                                      Some(ctx.declarations),
                                      ctx.contextType,
                                      ctx.options)
          }
          operationContext.nodeRefIds ++= ctx.nodeRefIds
          val operation = RamlOperationParser(entry, endpoint.id, parseOptionalOperations)(operationContext)
            .parse()
          operations += operation
          ctx.operationContexts.put(operation, operationContext)
        })
        endpoint.setWithoutId(EndPointModel.Operations, AmfArray(operations, Annotations.virtual()), Annotations.inferred())
      }
    )

    val idCounter         = new IdCounter()
    val RequirementParser = RamlSecurityRequirementParser.parse(endpoint.id, idCounter) _
    map.key("securedBy", (EndPointModel.Security in endpoint using RequirementParser).allowingSingleValue)

    var parameters               = Parameters()
    var annotations: Annotations = Annotations()

    val entries = map.regex(uriParametersKey)
    val implicitExplicitPathParams = entries.collectFirst({ case e if e.value.tagType == YType.Map => e }) match {
      case None =>
        implicitPathParamsOrdered(endpoint, isResourceType)

      case Some(e) =>
        annotations = Annotations(e.value)

        val explicitParameters = e.value
          .as[YMap]
          .entries
          .map(entry => {
            val param = ctx.factory
              .parameterParser(entry, (p: Parameter) => Unit, false, "path")
              .parse()
            param.fields ? [Shape] ParameterModel.Schema foreach (schema => validateSlashsInSchema(schema, entry))
            param
          })

        implicitPathParamsOrdered(endpoint,
                                  isResourceType,
                                  variable => !explicitParameters.exists(_.name.is(variable)),
                                  explicitParameters)
    }

    parameters = parameters.add(Parameters(path = implicitExplicitPathParams))

    map.key(
      "parameters".asRamlAnnotation,
      entry => {
        parameters =
          parameters.add(OasParametersParser(entry.value.as[Seq[YNode]], endpoint.id)(spec.toOas(ctx)).parse())
        annotations = Annotations(entry.value)
      }
    )

    parameters match {
      case Parameters(query, path, header, _, _, _) if parameters.nonEmpty =>
        endpoint.setWithoutId(EndPointModel.Parameters, AmfArray(query ++ path ++ header, annotations), annotations)
      case _ =>
    }

    map.key(
      "payloads".asRamlAnnotation,
      entry => {
        endpoint.setWithoutId(EndPointModel.Payloads,
                     AmfArray(Seq(Raml10PayloadParser(entry, endpoint.id).parse()), Annotations(entry.value)),
                     Annotations(entry))
      }
    )

    collector += endpoint

    AnnotationParser(endpoint,
                     map,
                     if (isResourceType) List(VocabularyMappings.resourceType)
                     else List(VocabularyMappings.endpoint))(WebApiShapeParserContextAdapter(ctx)).parse()

    val nestedEndpointRegex = "^/.*"
    map.regex(
      nestedEndpointRegex,
      entries => {
        if (isResourceType) {
          entries.foreach { entry =>
            val nestedEndpointName = entry.key.toString()
            ctx.eh.violation(
              NestedEndpoint,
              endpoint,
              None,
              s"Nested endpoint in resourceType: '$nestedEndpointName'",
              Some(LexicalInformation(Range(entry.key.range))),
              Some(map.sourceName)
            )
          }
        } else {
          entries.foreach(ctx.factory.endPointParser(_, producer, Some(endpoint), collector, false).parse())
        }
      }
    )
  }

  private def validateSlashsInSchema(shape: Shape, entry: YMapEntry): Unit = {
    //default values
    Option(shape.default).foreach(validateSlashInDataNode(_, entry))
    //enum values
    shape.values.foreach(value => validateSlashInDataNode(value, entry))
    //example values
    shape match {
      case scalar: ScalarShape =>
        scalar.examples.foreach(example => Option(example.structuredValue).foreach(validateSlashInDataNode(_, entry)))
      case _ =>
    }
  }

  private def validateSlashInDataNode(node: DataNode, entry: YMapEntry): Unit = node match {
    case scalar: ScalarDataNode if scalar.value.option().exists(_.contains('/')) =>
      ctx.eh.violation(SlashInUriParameterValues,
                       node,
                       s"Value '${scalar.value.value()}' of uri parameter must not contain '/' character",
                       entry.value.location)
    case _ =>
  }

  def generateParam(endpoint: EndPoint, variable: String): Option[Parameter] = {
    val operationsDefineParam: Boolean =
      endpoint.operations.nonEmpty && endpoint.operations.forall { op =>
        Option(op.request).exists(_.uriParameters.exists(_.parameterName.option().contains(variable)))
      }

    if (operationsDefineParam) None
    else {
      val pathParam = Parameter(Annotations.virtual())
        .withSynthesizeName(variable)
        .set(ParameterModel.ParameterName, variable, Annotations.synthesized())
        .syntheticBinding("path")
        .set(ParameterModel.Required, AmfScalar(true), Annotations.synthesized())
      endpoint.add(EndPointModel.Parameters, pathParam)

      pathParam.withScalarSchema(variable).withDataType(DataType.String)
      Some(pathParam)
    }
  }

  private def implicitPathParamsOrdered(endpoint: EndPoint,
                                        isResourceType: Boolean,
                                        filter: String => Boolean = _ => true,
                                        explicitParams: Seq[Parameter] = Nil): Seq[Parameter] = {
    val parentParams: Map[String, Parameter] = parent
      .map(
        _.parameters
          .filter(_.binding.value() == "path")
          .foldLeft(Map[String, Parameter]()) {
            case (acc, p) =>
              acc.updated(p.name.value(), p)
          }
      )
      .getOrElse(Map())

    val pathParams: Seq[String] = TemplateUri.variables(parsePath())
    val params: Seq[Parameter] = pathParams
      .filter(filter)
      .flatMap { variable =>
        parentParams.get(variable) match {
          case Some(param) =>
            val pathParam = param.cloneParameter(endpoint.id)
            param.name.option().foreach(n => pathParam.withSynthesizeName(n))
            pathParam.annotations += VirtualElement()
            Some(pathParam)
          case None =>
            explicitParams.find(p => p.name.value().equals(variable) && p.binding.value().equals("path")) match {
              case Some(p) => Some(p)
              case None    => generateParam(endpoint, variable)
            }
        }
      }
    if (!isResourceType) {
      checkParamsUsage(endpoint, pathParams, explicitParams)
    }
    params ++ explicitParams.filter(!params.contains(_))

  }

  private def checkParamsUsage(endpoint: EndPoint, pathParams: Seq[String], endpointParams: Seq[Parameter]): Unit = {
    endpointParams.foreach { p =>
      if (!p.name.option().exists(n => pathParams.contains(n)))
        ctx.eh.warning(UnusedBaseUriParameter,
                       p,
                       None,
                       s"Unused uri parameter ${p.name.value()}",
                       p.position(),
                       p.location())
    }

    endpoint.operations.flatMap(o => Option(o.request)).flatMap(_.uriParameters).foreach { p =>
      if (!p.name.option().exists(n => pathParams.contains(n))) {
        ctx.eh.warning(UnusedBaseUriParameter,
                       p,
                       None,
                       s"Unused operation uri parameter ${p.name.value()}",
                       p.position(),
                       p.location())
      }
    }
  }
  protected def parsePath(): String = parent.map(_.path.value()).getOrElse("") + entry.key.as[YScalar].text

  protected def uriParametersKey: String
}
