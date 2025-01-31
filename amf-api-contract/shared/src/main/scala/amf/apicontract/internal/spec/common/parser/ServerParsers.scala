package amf.apicontract.internal.spec.common.parser

import amf.apicontract.client.scala.model.domain.api.{Api, WebApi}
import amf.apicontract.client.scala.model.domain.{Parameter, Server}
import amf.apicontract.internal.metamodel.domain.ServerModel
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.oas.parser.domain.{OasLikeServerParser, OasServersParser}
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.spec.{toOas, toRaml}
import amf.apicontract.internal.validation.definitions.ParserSideValidations._
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.internal.annotations._
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Spec
import amf.core.internal.utils.{AmfStrings, TemplateUri}
import amf.shapes.internal.spec.common.parser.{RamlScalarNode, YMapEntryLike}
import org.mulesoft.common.client.lexical.PositionRange
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

case class RamlServersParser(map: YMap, api: WebApi)(implicit val ctx: RamlWebApiContext) extends SpecParserOps {

  var baseUriVars: Map[String, (LexicalInformation, LexicalInformation)] = Map()

  private def setBaseUriVars(entry: YMapEntry): Unit = {
    val path = entry.value.tagType match {
      case YType.Map => entry.value.as[YMap].key("value").get.value.as[YScalar].text
      case _         => entry.value.as[YScalar].text
    }
    val pathParams  = TemplateUri.variables(path)
    val pathLexical = entry.value.range

    baseUriVars = pathParams.map { paramName =>
      val paramTemplate    = s"{$paramName}"
      val startOfParameter = getParamStart(path, paramTemplate)
      val endOfParameter   = startOfParameter + paramTemplate.length

      val lexicalWithBraces    = getLexicalPortion(pathLexical, startOfParameter, endOfParameter)
      val lexicalWithoutBraces = getLexicalPortion(pathLexical, startOfParameter + 1, endOfParameter - 1)

      paramName -> (lexicalWithBraces, lexicalWithoutBraces)
    }.toMap
  }

  private def getLexicalPortion(pathLexical: PositionRange, from: Int, to: Int): LexicalInformation = {
    LexicalInformation(
      pathLexical.lineFrom,
      pathLexical.columnFrom + from,
      pathLexical.lineTo,
      pathLexical.columnFrom + to
    )
  }

  private def getParamStart(path: String, paramTemplate: String) = path.indexOfSlice(paramTemplate)

  def parse(): Unit = {
    map.key("baseUri") match {
      case Some(entry) =>
        setBaseUriVars(entry)
        val node   = RamlScalarNode(entry.value)
        val value  = node.text().toString
        val server = api.withServer(value)

        (ServerModel.Url in server).allowingAnnotations(entry)

        checkBalancedParams(value, entry.value, server, ServerModel.Url.value.iri(), ctx)

        if (!TemplateUri.isValid(value))
          ctx.eh.violation(InvalidServerPath, api, TemplateUri.invalidMsg(value), entry.value.location)

        map.key("serverDescription".asRamlAnnotation, ServerModel.Description in server)

        val variables = TemplateUri.variables(value)
        checkForUndefinedVersion(entry, variables)
        parseBaseUriParameters(server, TemplateUri.variables(value))

        api.setWithoutId(
          WebApiModel.Servers,
          AmfArray(Seq(server.add(SynthesizedField())), Annotations(entry.value)),
          Annotations(entry)
        )
      case None =>
        map
          .key("baseUriParameters")
          .foreach { entry =>
            ctx.eh.violation(
              ParametersWithoutBaseUri,
              api,
              "'baseUri' not defined and 'baseUriParameters' defined.",
              entry.location
            )

            val server = Server()
            parseBaseUriParameters(server, Nil)

            api.setWithoutId(
              WebApiModel.Servers,
              AmfArray(Seq(server.add(SynthesizedField())), Annotations(entry.value)),
              Annotations(entry)
            )
          }
    }

    map.key("servers".asRamlAnnotation).foreach { entry =>
      entry.value
        .as[Seq[YMap]]
        .map(m => new OasLikeServerParser(api.id, YMapEntryLike(m))(toOas(ctx)).parse())
        .foreach { server =>
          api.add(WebApiModel.Servers, server)
        }
    }
  }

  private def parseBaseUriParameters(server: Server, orderedVariables: Seq[String]): Unit = {
    val maybeEntry = map.key("baseUriParameters")
    maybeEntry match {
      case Some(entry) =>
        val definedParams = parseParameterDefinitions(entry)
        checkIfVersionParameterIsDefined(orderedVariables, definedParams, entry)
        val allParams = parseAllParams(orderedVariables, definedParams)
        server.setWithoutId(ServerModel.Variables, AmfArray(allParams, Annotations(entry.value)), Annotations(entry))

      case None if orderedVariables.nonEmpty =>
        val implicitParams = orderedVariables.map(parseImplicitPathParam)
        server.setWithoutId(ServerModel.Variables, AmfArray(implicitParams))

      case _ => // ignore
    }

  }

  private def parseAllParams(orderedVariables: Seq[String], definedParams: Seq[Parameter]): Seq[Parameter] = {
    val baseUriParams = orderedVariables.map(varName =>
      definedParams.find(_.name.value().equals(varName)) match {
        case Some(parameter) => parameter                       // ignore defined params (already parsed)
        case _               => parseImplicitPathParam(varName) // parse implicit params

      }
    )
    val unusedParams = definedParams.diff(baseUriParams)
    warnUnusedParams(unusedParams)
    baseUriParams ++ unusedParams
  }

  private def warnUnusedParams(unusedParams: Seq[Parameter]): Unit = {
    unusedParams.foreach { p =>
      ctx.eh.warning(
        UnusedBaseUriParameter,
        p,
        None,
        s"Unused base uri parameter ${p.name.value()}",
        p.position(),
        p.location()
      )
    }
  }

  private def parseParameterDefinitions(entry: YMapEntry): Seq[Parameter] = {
    entry.value.tagType match {
      case YType.Map =>
        RamlParametersParser(entry.value.as[YMap], _ => Unit, binding = "path")
          .parse()
      case YType.Null => Nil
      case _ =>
        ctx.eh.violation(InvalidBaseUriParametersType, "", "Invalid node for baseUriParameters", entry.value.location)
        Nil
    }
  }

  private def parseImplicitPathParam(varName: String): Parameter = {
    val (lexical, nameLexical) = baseUriVars(varName)
    val param = Parameter(Annotations(lexical))
      .withName(varName, Annotations(nameLexical))
      .syntheticBinding("path")
      .withRequired(true)
    param.withScalarSchema(varName).withDataType(DataType.String)
    param.annotations += VirtualElement()
    param
  }

  private def checkForUndefinedVersion(entry: YMapEntry, variables: Seq[String]): Unit = {
    val webapiHasVersion = map.key("version").isDefined
    if (variables.contains("version") && !webapiHasVersion) {
      ctx.eh.warning(
        ImplicitVersionParameterWithoutApiVersion,
        api,
        "'baseUri' defines 'version' variable without the API defining one",
        entry.location
      )
    }
  }

  private def checkIfVersionParameterIsDefined(
      orderedVariables: Seq[String],
      parameters: Seq[Parameter],
      entry: YMapEntry
  ): Unit = {
    val apiHasVersion          = api.version.option().isDefined
    val versionParameterExists = parameters.exists(_.name.option().exists(name => name.equals("version")))
    if (orderedVariables.contains("version") && versionParameterExists && apiHasVersion) {
      ctx.eh.warning(
        InvalidVersionBaseUriParameterDefinition,
        api,
        "'version' baseUriParameter can't be defined if present in baseUri as variable",
        entry.location
      )
    }
  }

}

case class Oas3ServersParser(map: YMap, elem: DomainElement, field: Field)(implicit override val ctx: OasWebApiContext)
    extends OasServersParser(map, elem, field) {

  override def parse(): Unit = if (ctx.spec == Spec.OAS30 || ctx.spec == Spec.OAS31) parseServers("servers")
}

case class Oas2ServersParser(map: YMap, api: Api)(implicit override val ctx: OasWebApiContext)
    extends OasServersParser(map, api, WebApiModel.Servers) {
  override def parse(): Unit = {
    if (baseUriExists(map)) {
      var host     = ""
      var basePath = ""

      val annotations = Annotations.synthesized()

      map.key("basePath").foreach { entry =>
        annotations += BasePathLexicalInformation(entry.range)
        basePath = entry.value.as[String]

        if (!basePath.startsWith("/")) {
          ctx.eh.violation(InvalidBasePath, api, "'basePath' property must start with '/'", entry.value.location)
          basePath = "/" + basePath
        }
      }
      map.key("host").foreach { entry =>
        annotations += HostLexicalInformation(entry.range)
        host = entry.value.as[String]
      }

      val server = Server(Annotations.virtual()).set(ServerModel.Url, AmfScalar(host + basePath), annotations)

      map.key("serverDescription".asOasExtension, ServerModel.Description in server)

      map.key(
        "baseUriParameters".asOasExtension,
        entry => {
          val uriParameters =
            RamlParametersParser(entry.value.as[YMap], _ => Unit, binding = "path")(toRaml(ctx)).parse()

          server.set(ServerModel.Variables, AmfArray(uriParameters, Annotations(entry.value)), Annotations(entry))
        }
      )

      api.set(WebApiModel.Servers, AmfArray(Seq(server), Annotations.inferred()), Annotations.inferred())
    }

    parseServers("servers".asOasExtension)
  }

  def baseUriExists(map: YMap): Boolean = map.key("host").orElse(map.key("basePath")).isDefined
}
