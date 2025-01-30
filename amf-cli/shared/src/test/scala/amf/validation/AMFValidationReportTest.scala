package amf.validation

import amf.core.client.common.validation.Raml10Profile
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import org.scalatest.funsuite.AnyFunSuite

class AMFValidationReportTest extends AnyFunSuite {

  test("Test to string report with null values in results") {
    AMFValidationReport(
      null,
      Raml10Profile,
      Seq(
        AMFValidationResult(null, null, "", None, null, null, None, null),
        AMFValidationResult(null, null, "", None, null, None, None, null)
      )
    ).toString
  }
}
