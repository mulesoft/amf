package amf.emit

import amf.apicontract.client.scala.WebAPIConfiguration
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, Module}
import amf.core.internal.remote.{Hint, Oas20JsonHint, Raml10YamlHint}
import amf.core.io.FileAssertionTest
import amf.shapes.client.scala.model.domain.AnyShape
import amf.testing.ConfigProvider.configFor
import org.scalatest.Assertion

import scala.concurrent.Future

class ShapeToJsonSchemaTest extends FileAssertionTest {

  test("Test api with x-examples") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("x-examples.json", "x-examples.schema.json", func, hint = Oas20JsonHint)
  }

  test("Test api with x-examples 2") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("x-examples2.json", "x-examples2.schema.json", func, hint = Oas20JsonHint)
  }

  test("Test array with object items") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("array-of-object.raml", "array-of-object.json", func)
  }

  test("Test array annotation type") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.request.queryParameters.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("param-with-annotation.raml", "param-with-annotation.json", func)
  }

  test("Test parsed from json expression generations") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("json-expression.raml", "json-expression.json", func)
  }

  test("Test parsed from json expression forced to build new") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("json-expression.raml", "json-expression-new.json", func)
  }

  test("Test recursive shape") {
    val func = (u: BaseUnit) =>
      encodedWebApi(u)
        .flatMap(_.endPoints.headOption)
        .flatMap(_.operations.headOption)
        .flatMap(_.responses.headOption)
        .flatMap(_.payloads.headOption)
        .map(_.schema)
        .collectFirst({ case any: AnyShape => any })

    cycle("recursive.raml", "recursive.json", func)
  }

  test("Test shape id preservation") {
    val file   = "shapeIdPreservation.raml"
    val config = WebAPIConfiguration.WebAPI().withErrorHandlerProvider(() => UnhandledErrorHandler)
    parse(file, config).map { case u: Module =>
      assert(
        u.declares.forall { case anyShape: AnyShape =>
          val originalId = anyShape.id
          config.elementClient().toJsonSchema(anyShape)
          val newId = anyShape.id
          originalId == newId
        }
      )
    }
  }

  private val basePath: String   = "file://amf-cli/shared/src/test/resources/tojson/tojsonschema/source/"
  private val goldenPath: String = "amf-cli/shared/src/test/resources/tojson/tojsonschema/schemas/"

  private def parse(file: String, config: AMFGraphConfiguration): Future[BaseUnit] = {
    val client = config.baseUnitClient()
    for {
      unit <- client.parse(basePath + file).map(_.baseUnit)
    } yield {
      unit
    }
  }

  private def cycle(
      file: String,
      golden: String,
      findShapeFunc: BaseUnit => Option[AnyShape],
      hint: Hint = Raml10YamlHint
  ): Future[Assertion] = {
    val config = WebAPIConfiguration
      .WebAPI()
      .withRenderOptions(RenderOptions().withoutCompactedEmission)
    val jsonSchema: Future[String] = for {
      unit <- parse(file, config)
    } yield {
      findShapeFunc(
        configFor(hint.spec)
          .baseUnitClient()
          .transform(unit, PipelineId.Default)
          .baseUnit
      ).map { element =>
        config.elementClient().toJsonSchema(element)
      }.getOrElse("")
    }

    jsonSchema.flatMap { writeTemporaryFile(golden) }.flatMap(assertDifferences(_, goldenPath + golden))
  }

  private def encodedWebApi(u: BaseUnit) =
    Option(u).collectFirst({ case d: Document if d.encodes.isInstanceOf[WebApi] => d.encodes.asInstanceOf[WebApi] })
}
