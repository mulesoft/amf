package amf.validation

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.io.FileAssertionTest
import org.scalatest.Assertion

import scala.concurrent.Future

abstract class AbstractValidationTest extends FileAssertionTest {

  protected def config: AMFConfiguration
  protected val pipeline: String
  protected val profile: ProfileName

  def assertReport(api: String, report: String, directory: String): Future[Assertion] = {
    val client = config.baseUnitClient()
    val url    = s"file://$directory/$api"
    val golden = s"$directory/$report"
    for {
      parseResult      <- client.parse(url)
      transformResult  <- Future.successful(client.transform(parseResult.baseUnit, pipeline))
      validationResult <- client.validate(transformResult.baseUnit)
      actual <- {
        val results = parseResult.results ++ transformResult.results ++ validationResult.results
        val report  = AMFValidationReport(url, profile, results)
        writeTemporaryFile(golden)(report.toString)
      }
      assertion <- assertDifferences(actual, golden)
    } yield {
      assertion
    }
  }

}
