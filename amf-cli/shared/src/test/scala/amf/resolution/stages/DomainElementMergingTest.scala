package amf.resolution.stages

import amf.aml.internal.registries.AMLRegistry
import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.templates.{ParametrizedTrait, Trait}
import amf.apicontract.internal.spec.common.transformation.stage.DomainElementMerging
import amf.apicontract.internal.spec.raml.parser.context.Raml10WebApiContext
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.parser.LimitedParseConfig
import amf.shapes.client.scala.model.domain.ScalarShape
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/**
  * Created by pedro.colunga on 10/31/17.
  */
class DomainElementMergingTest extends AnyFunSuite with Matchers {

  test("Merge EndPoints") {

    val main = EndPoint()
      .withName("Main")
      .withPath("/main")
      .withDescription("Main description")

    main
      .withOperation("get")
      .withSummary("Get main operation")
      .withRequest()
      .withQueryParameter("a")
      .withDescription("Main query parameter a")

    main.withOperation("post").withSummary("Post main operation")
    main.withOperation("head")

    val other = EndPoint()
      .withName("Other")
      .withPath("/other")
      .withDescription("Other description")

    other
      .withOperation("get")
      .withSummary("Get other operation")
      .withRequest()
      .withQueryParameter("a")
      .withDescription("Other query parameter a")
      .withScalarSchema("integer")
      .withDataType("integer")

    other.withOperation("put").withSummary("Put other operation")
    other.withOperation("head").withSummary("Head other operation")

    DomainElementMerging()(ctx).merge(main, other)

    main.operations.size should be(4)

    val get = main.operations.head
    get.summary.value() should be("Get main operation")
    get.method.value() should be("get")

    val parameters = get.request.queryParameters
    parameters.size should be(1)
    val a = parameters.head
    a.name.value() should be("a")
    a.description.value() should be("Main query parameter a")
    a.schema.asInstanceOf[ScalarShape].dataType.value() should be("integer")

    val post = main.operations(1)
    post.summary.value() should be("Post main operation")
    post.method.value() should be("post")

    val head = main.operations(2)
    head.summary.value() should be("Head other operation")
    head.method.value() should be("head")

    val put = main.operations(3)
    put.summary.value() should be("Put other operation")
    put.method.value() should be("put")
  }

  test("Do not merge extends") {

    val a = ParametrizedTrait()
      .withName("a")
      .withTarget(Trait().withId("/trait/a"))

    val b = ParametrizedTrait()
      .withName("b")
      .withTarget(Trait().withId("/trait/b"))

    val main = EndPoint()
      .withName("Main")
      .withPath("/main")
      .withExtends(Seq(a))

    val other = EndPoint()
      .withName("Other")
      .withPath("/other")
      .withExtends(Seq(b))

    DomainElementMerging()(ctx).merge(main, other)

    main.extend.size should be(1)
    main.extend.head should be(a)
  }

  private def ctx: Raml10WebApiContext = {
    new Raml10WebApiContext("",
                            Nil,
                            ParserContext(config = LimitedParseConfig(UnhandledErrorHandler, AMLRegistry.empty)))
  }
}
