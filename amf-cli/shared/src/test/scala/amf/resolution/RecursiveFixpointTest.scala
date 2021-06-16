package amf.resolution

import amf.apicontract.client.scala.config.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.client.environment.AsyncAPIConfiguration
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.FieldsFilter.All
import amf.core.client.scala.model.domain.{AmfObject, RecursiveShape}
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.remote.Syntax.Yaml
import amf.core.internal.remote.{AsyncApi20, Hint, Oas20YamlHint, Raml10YamlHint}
import amf.core.internal.unsafe.PlatformSecrets
import org.mulesoft.common.collections.FilterType
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers.{contain, convertToAnyShouldWrapper}
import amf.core.client.common.transform._

import scala.concurrent.{ExecutionContext, Future}

class RecursiveFixpointTest() extends AsyncFunSuite with PlatformSecrets with ResolutionCapabilities {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-cli/shared/src/test/resources/resolution/"

  case class RecursiveTestCase(path: String, hint: Hint)

  val testCases: Seq[RecursiveTestCase] = Seq(
    RecursiveTestCase("nested-library-with-recursive-shape/api.raml", Raml10YamlHint),
    RecursiveTestCase("additional-prop-recursive-shape/api.yaml", Oas20YamlHint),
    RecursiveTestCase("not-facet-recursive-shape/api.yaml", Hint(AsyncApi20, Yaml))
  )

  testCases.foreach { data =>
    test(s"Test fixpoint values in ${data.path}") {
      val config = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20())
      for {

        parseResult <- config
          .withErrorHandlerProvider(() => UnhandledErrorHandler)
          .createClient()
          .parse(s"file://$basePath${data.path}")
        _ <- Future(transform(parseResult.bu, PipelineId.Editing, data.hint.vendor, config))
      } yield {
        val elements                    = parseResult.bu.iterator(fieldsFilter = All).toList
        val fixpointValues: Seq[String] = elements.filterType[RecursiveShape].map(_.fixpoint.value())
        val allIds                      = elements.collect { case o: AmfObject => o.id }.toSet
        allIds should contain allElementsOf (fixpointValues)
      }
    }
  }

}
