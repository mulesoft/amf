package amf.parser
import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.AmfJsonHint
import amf.io.FunSuiteCycleTests

class GraphParsingTest extends FunSuiteCycleTests {

  override def basePath: String = "amf-cli/shared/src/test/resources/graphs/"

  test("Parse api with context with expanded term definitions") {
    val ro = RenderOptions().withCompactUris.withPrettyPrint.withFlattenedJsonLd
    cycle("api.source.jsonld", "api.golden.jsonld", AmfJsonHint, AmfJsonHint, renderOptions = Some(ro))
  }

  test("Parse api with link target maps") {
    val ro = RenderOptions().withPrettyPrint.withFlattenedJsonLd
    cycle(
      "api.source.jsonld",
      "api.golden.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro),
      directory = s"${basePath}link-target-map/"
    )
  }

  test("Conserve id values when parsing to maintain consistency with recursive fixpoints - flattened") {
    val ro = RenderOptions().withCompactUris.withPrettyPrint.withFlattenedJsonLd
    cycle(
      "recursive-api.flattened.jsonld",
      "recursive-api.flattened.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro)
    )
  }

  test("Conserve id values when parsing to maintain consistency with recursive fixpoints - expanded") {
    val ro = RenderOptions().withCompactUris.withPrettyPrint.withoutFlattenedJsonLd
    cycle(
      "recursive-api.expanded.jsonld",
      "recursive-api.expanded.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro)
    )
  }

  test("Parse compacted id fields correctly applying base - flattened source") {
    val ro = RenderOptions().withPrettyPrint.withoutFlattenedJsonLd
    cycle(
      "recursive-api.flattened.jsonld",
      "recursive-api-full-uris.expanded.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro)
    )
  }

  test("Parse compacted id fields correctly applying base - expanded source") {
    val ro = RenderOptions().withPrettyPrint.withoutFlattenedJsonLd
    cycle(
      "recursive-api.expanded.jsonld",
      "recursive-api-full-uris.expanded.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro)
    )
  }

  test("Parse expanded uri fields") {
    val ro = RenderOptions().withPrettyPrint.withoutFlattenedJsonLd
    cycle(
      "recursive-api-full-uris.expanded.jsonld",
      "recursive-api-full-uris.expanded.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro)
    )
  }

  test("Parse api with @base and absolute IRIs - flattened") {
    val ro = RenderOptions().withPrettyPrint.withFlattenedJsonLd
    cycle(
      "api.source.flattened.jsonld",
      "api.golden.flattened.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro),
      directory = s"$basePath/base-and-absolute-iris/"
    )
  }

  test("Parse api with @base and absolute IRIs - expanded") {
    val ro = RenderOptions().withPrettyPrint.withoutFlattenedJsonLd
    cycle(
      "api.source.expanded.jsonld",
      "api.golden.expanded.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro),
      directory = s"$basePath/base-and-absolute-iris/"
    )
  }

  test("Parse annotations with compact URIs") {
    val ro = RenderOptions().withPrettyPrint.withFlattenedJsonLd.withCompactUris
    cycle(
      "api.source.jsonld",
      "api.target.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro),
      directory = s"$basePath/annotations-compact/"
    )

  }

  test("Parse annotations with expanded URIs") {
    val ro = RenderOptions().withPrettyPrint.withFlattenedJsonLd
    cycle(
      "api.source.jsonld",
      "api.target.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro),
      directory = s"$basePath/annotations-expanded/"
    )
  }

  test("Parse non scalar annotations") {
    val ro = RenderOptions().withPrettyPrint.withFlattenedJsonLd
    cycle(
      "api.source.jsonld",
      "api.target.jsonld",
      AmfJsonHint,
      AmfJsonHint,
      renderOptions = Some(ro),
      directory = s"$basePath/annotations-non-scalar/"
    )
  }
}
