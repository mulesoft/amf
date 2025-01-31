package amf.validation

import amf.apicontract.client.scala.{RAMLConfiguration, WebAPIConfiguration}
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import org.scalatest.matchers.should.Matchers

class SingleConfigurationTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("valid and invalid parsing errors with single instance") {
    val invalidApi = "file://amf-cli/shared/src/test/resources/parser-results/raml/error/map-key.raml"
    val validApi   = "file://amf-cli/shared/src/test/resources/validations/raml/valid-number-format.raml"
    val config     = WebAPIConfiguration.WebAPI()
    for {
      parsedInvalid <- config.baseUnitClient().parse(invalidApi)
      parsedValid   <- config.baseUnitClient().parse(validApi)
    } yield {
      assert(!parsedInvalid.conforms)
      assert(parsedValid.conforms)
    }
  }

  test("deterministic syaml errors when parsing multiple times") {
    val api    = "file://amf-cli/shared/src/test/resources/validations/raml/multiple-syaml-errors/api.raml"
    val client = RAMLConfiguration.RAML().baseUnitClient()
    for {
      firstParsed  <- client.parse(api)
      secondParsed <- client.parse(api)
      thirdParsed  <- client.parse(api)
    } yield {
      val amtOfErrors = firstParsed.results.size
      assert(amtOfErrors == 46)
      assert(secondParsed.results.size == amtOfErrors)
      assert(thirdParsed.results.size == amtOfErrors)
    }
  }

}
