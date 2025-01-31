package amf.emit

import amf.apicontract.client.scala.WebAPIConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParser
import amf.core.client.scala.validation.AMFValidator
import amf.core.internal.remote.{Hint, Oas20JsonHint, Raml10YamlHint}
import amf.core.internal.resource.StringResourceLoader
import amf.core.io.FileAssertionTest
import org.scalatest.Assertion

import scala.concurrent.Future

class CompatibilityTest extends FileAssertionTest {

  private val basePath = "amf-cli/shared/src/test/resources/upanddown/"

  test("leagues raml to oas") {
    compatibility("leagues-api.raml", Raml10YamlHint, Oas20JsonHint)
  }

  test("leagues oas to raml") {

    compatibility("leagues-api.json", Oas20JsonHint, Raml10YamlHint)
  }

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  private def compatibility(source: String, l: Hint, r: Hint): Future[Assertion] = {
    for {
      input  <- fs.asyncFile(basePath + source).read()
      left   <- parseBaseUnit(input.toString, l)
      target <- Future.successful(new AMFRenderer(left, r, RenderOptions()).renderToString)
      _      <- parseBaseUnit(target, r)
    } yield {
      succeed
    }
  }

  private def parseBaseUnit(content: String, hint: Hint): Future[BaseUnit] = {
    val eh = DefaultErrorHandler()
    val conf = WebAPIConfiguration
      .WebAPI()
      .withErrorHandlerProvider(() => eh)
      .withResourceLoader(StringResourceLoader("amf://id#", content, Some(hint.spec.mediaType)))
    for {
      unit <- AMFParser.parse("amf://id#", conf)
      _    <- AMFValidator.validate(unit.baseUnit, conf)
    } yield unit.baseUnit
  }
}
