package amf.validation

import amf.apicontract.client.scala.{OASConfiguration, RAMLConfiguration, WebAPIConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation._
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.validation.CoreValidations
import org.scalatest.Assertion

import scala.concurrent.Future

case class ExpectedReport(
    conforms: Boolean,
    numErrors: Integer,
    profile: ProfileName,
    jsNumErrors: Option[Integer] = None
) // todo: we should remove this, both platforms should validate the same

class ValidationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext {

  val basePath         = "file://amf-cli/shared/src/test/resources/vocabularies2/production/validation/"
  val vocabulariesPath = "file://amf-cli/shared/src/test/resources/vocabularies2/production/validation/"
  val examplesPath     = "file://amf-cli/shared/src/test/resources/validations/"
  val productionPath   = "file://amf-cli/shared/src/test/resources/production/"
  val validationsPath  = "file://amf-cli/shared/src/test/resources/validations/"
  val upDownPath       = "file://amf-cli/shared/src/test/resources/upanddown/"
  val parserPath       = "file://amf-cli/shared/src/test/resources/org/raml/parser/"
  val jsonSchemaPath   = "file://amf-cli/shared/src/test/resources/validations/jsonschema"

  // todo serialize json of validation report?
  // Example validations test and Example model validation test were the same, because the resolution runs always for validation

  // is testing that the api has no errors. Should be in Platform?
  test("Some production api with includes") {
    for {
      report <- parseAndValidate(productionPath + "includes-api/api.raml", RAMLConfiguration.RAML10())
    } yield {
      val (violations, others) =
        report.results.partition(r => r.severityLevel.equals(SeverityLevels.VIOLATION))
      assert(violations.isEmpty)
      assert(others.lengthCompare(1) == 0)
      assert(others.head.severityLevel == SeverityLevels.WARNING)
      assert(others.head.message.equals("'schema' keyword it's deprecated for 1.0 version, should use 'type' instead"))
    }
  }

  // tck examples?! for definition this name its wrong. What it's testing? the name makes reference to an external fragment exception, but the golden its a normal and small api.
  test("Test validate external fragment cast exception") {
    for {
      report <- parseAndValidate(
        validationsPath + "/tck-examples/cast-external-exception.raml",
        RAMLConfiguration.RAML10()
      )
    } yield {
      assert(report.conforms)
    }
  }

  // the reported null pointer case could not be reproduced. This test was added with the whole api to prove that there is any null pointer.
  // should we delete this case?
  test("Raml 0.8 Null pointer tck case APIMF-429") {
    for {
      report <- parseAndValidate(
        validationsPath + "/tck-examples/nullpointer-spec-example.raml",
        RAMLConfiguration.RAML08()
      )
    } yield {
      assert(report.results.isEmpty)
    }
  }

  // this is a real case, recursion in json schema??
  test("Test stackoverflow case from Platform") {
    for {
      report <- parseAndValidate(validationsPath + "/stackoverflow/api.raml", RAMLConfiguration.RAML08())
    } yield {
      assert(!report.results.exists(_.validationId != CoreValidations.RecursiveShapeSpecification.id))
    }
  }

  // same than the previous one
  test("Test stackoverflow case 0.8 from Platform") {
    for {
      report <- parseAndValidate(validationsPath + "/stackoverflow2/api.raml", RAMLConfiguration.RAML08())
    } yield {
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }
// why the generation???? Move to MovelValidationReportTest?
  test("Security scheme and traits test") {
    for {

      client      <- Future.successful(RAMLConfiguration.RAML10().baseUnitClient())
      parseResult <- client.parse(validationsPath + "/security-schemes/security1.raml")
      transformResult <- Future {
        client.transform(parseResult.baseUnit, PipelineId.Default)
      }
    } yield {
      assert(!transformResult.conforms)
      assert(transformResult.results.size == 2)
      assert(
        transformResult.results
          .exists(_.message.contains("Security scheme 'undefined' not found in declarations."))
      )
    }
  }

  test("Custom validaton problems 1") {
    for {

      client      <- Future.successful(RAMLConfiguration.RAML10().baseUnitClient())
      parseResult <- client.parse(validationsPath + "/missing-annotation-types/api.raml")
      transformResult <- Future {
        client.transform(parseResult.baseUnit, PipelineId.Default)
      }
      report <- client.validate(transformResult.baseUnit)
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Custom validation problems 2 (RAML)") {
    for {
      client      <- Future.successful(RAMLConfiguration.RAML10().baseUnitClient())
      parseResult <- client.parse(validationsPath + "/enumeration-arrays/api.raml")
      transformResult <- Future {
        client.transform(parseResult.baseUnit, PipelineId.Default)
      }
      report <- client.validate(transformResult.baseUnit)
    } yield {
      assert(report.conforms)
    }
  }

  // TODO: Validating a RAML API as OAS -> Doesn't make sense
  ignore("Custom validation problems 2 (OAS)") {
    for {
      client      <- Future.successful(WebAPIConfiguration.WebAPI().baseUnitClient())
      parseResult <- client.parse(validationsPath + "/enumeration-arrays/api.raml")
      transformResult <- Future {
        client.transform(parseResult.baseUnit, PipelineId.Default)
      }
      report <- client.validate(transformResult.baseUnit)
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 2)
    }
  }

  test("Matrix tests") {
    for {
      report <- parseAndValidate(
        validationsPath + "/types/arrays/matrix_type_expression.raml",
        RAMLConfiguration.RAML10()
      )
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Patterned properties tests") {
    for {
      report <- parseAndValidate(validationsPath + "/types/patterned_properties.raml", RAMLConfiguration.RAML10())
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Recursive array test") {
    for {
      report <- parseAndValidate(validationsPath + "/types/recursive_array.raml", RAMLConfiguration.RAML10())
    } yield {
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("oas example error in shape becomes warning") {
    for {
      report <- parseAndValidate(validationsPath + "/production/oas_example1.yaml", OASConfiguration.OAS20())
    } yield {
      assert(report.conforms)
    }
  }

  test("Null super-array items test") {
    for {
      report <- parseAndValidate(productionPath + "/null_superarray_items/api.raml", RAMLConfiguration.RAML10())
    } yield {
      assert(report.conforms)
    }
  }

  test("0.8 'id' identifiers in JSON Schema shapes test") {
    for {
      report <- parseAndValidate(productionPath + "/id_json_schema_locations.raml", RAMLConfiguration.RAML08())
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 2)
    }
  }

  test("Erroneous JSON schema in ResourceType test") {
    for {
      report <- parseAndValidate(productionPath + "/resource_type_failing_schema/api.raml", RAMLConfiguration.RAML10())
    } yield {
      assert(report.conforms)
    }
  }

  test("Nested XML Schema test") {
    for {
      report <- parseAndValidate(productionPath + "/nested_xml_schema/api.raml", RAMLConfiguration.RAML10())
    } yield {
      assert(report.conforms)
    }
  }

  test("Recursion introduced after resource type application test") {
    for {
      report <- parseAndValidate(productionPath + "/recursion_after_resource_type/api.raml", RAMLConfiguration.RAML08())
    } yield {
      assert(!report.conforms)
      assert(report.results.size == 1)
    }
  }

  test("Numeric status codes in OAS responses") {
    for {
      report <- parseAndValidate(productionPath + "/oas_numeric_resources.yaml", OASConfiguration.OAS20())
    } yield {
      assert(report.conforms)
    }
  }

  ignore("emilio performance") {
    for {
      report <- parseAndValidate(
        productionPath + "sys-sabre-air-api-1.0.3-fat-raml/ha-sys-sabre-air-api.raml",
        RAMLConfiguration.RAML10()
      )
    } yield {
      // RAML10Plugin.resolve(model) // Change plugin here to resolve for a different spec.
      assert(report.results.isEmpty)
    }
    // assert(true)
  }

  test("Object that inherits from union payload validation") {
    for {
      report <- parseAndValidate(productionPath + "object-inherits-union.raml", RAMLConfiguration.RAML10())
    } yield {
      assert(report.conforms)
    }
  }

  test("Test complex object inherits several levels with root overrided property") {
    for {
      report <- parseAndValidate(productionPath + "missing-property-inherits.raml", RAMLConfiguration.RAML10())
    } yield {
      assert(report.conforms)
    }
  }

  test(
    "Test the most complex case of two endpoint with same resource types but different objects converging at same recursion"
  ) {
    for {
      report <- parseAndValidate(
        productionPath + "delete-test-fhir-r5/delete-test-fhir-r5.raml",
        RAMLConfiguration.RAML10()
      )
    } yield {
      assert(report.conforms)
    }
  }

  /** type: string | integer example: "120"
    *
    * At the header trait, used to produce string | string because of duplicated id, after resolve inheritance
    */
  test(
    "Test union of scalars with example at trait (duplicate id for scalar ending to have same scalar duplicated)"
  ) {
    for {
      report <- parseAndValidate(
        productionPath + "scalar-union-inheritance-trait/api.raml",
        RAMLConfiguration.RAML10()
      )
    } yield {
      assert(report.conforms)
    }
  }

  test("Test complex FHIR example with property overrided cross different files and inheritances") {
    conformsWithEditing("ce-platform-gateway-api-v1/ce-platform-gateway-api-v1.raml")
  }

  test("Test override property with same union range than super") {
    conformsWithEditing("simple-union-override/library.raml")
  }

  test("Test overrided union range property with future declaration") {
    conformsWithEditing("override-union-range-future-declaration/api.raml")
  }

  private def conformsWithEditing(source: String): Future[Assertion] = {
    val config = RAMLConfiguration.RAML10()
    for {
      report     <- config.baseUnitClient().parse(productionPath + source)
      transform  <- Future.successful(config.baseUnitClient().transform(report.baseUnit, PipelineId.Editing))
      validation <- config.baseUnitClient().validate(transform.baseUnit)
    } yield {
      val parseReport     = AMFValidationReport.unknownProfile(report)
      val transformReport = AMFValidationReport.unknownProfile(transform)
      assert(validation.merge(transformReport).merge(parseReport).conforms)
    }
  }
  private def parseAndValidate(url: String, config: => AMFGraphConfiguration): Future[AMFValidationReport] = {
    val client = config.baseUnitClient()
    for {
      parseResult <- client.parse(url)
      report      <- client.validate(parseResult.baseUnit)
    } yield {
      val parseReport = AMFValidationReport.unknownProfile(parseResult)
      val unified =
        if (!parseResult.conforms) parseReport
        else parseReport.merge(report)
      unified
    }
  }
}
