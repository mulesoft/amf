package amf.apicontract.internal.spec.raml.parser.domain

import amf.apicontract.client.scala.model.domain.Payload
import amf.apicontract.internal.metamodel.domain.PayloadModel
import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidPayload
import amf.core.client.scala.model.domain.{AmfScalar, Shape}
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.shapes.internal.domain.resolution.ExampleTracking.tracking
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import amf.shapes.internal.spec.common.parser.AnnotationParser
import amf.shapes.internal.spec.raml.parser._
import org.yaml.model._

/**
  *
  */
case class Raml10PayloadParser(entry: YMapEntry, parentId: String, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlPayloadParser(entry: YMapEntry, parentId: String, parseOptional) {

  override def parse(): Payload = {
    val payload = super.parse()

    entry.value.tagType match {
      case YType.Map => // ignore, in this case it will be parsed in the shape
      case _ =>
        entry.value.to[YMap] match {
          case Right(map) => AnnotationParser(payload, map)(WebApiShapeParserContextAdapter(ctx)).parse()
          case _          =>
        }
    }

    entry.value.tagType match {
      case YType.Null =>
        Raml10TypeParser(entry, shape => shape.withName("schema"), TypeInfo(), AnyDefaultType)(
          WebApiShapeParserContextAdapter(ctx))
          .parse()
          .foreach { schema =>
            ctx.autoGeneratedAnnotation(schema)
            payload.setWithoutId(PayloadModel.Schema, tracking(schema, payload), Annotations.inferred())
          }
      case _ =>
        Raml10TypeParser(entry, shape => shape.withName("schema"), TypeInfo(), AnyDefaultType)(
          WebApiShapeParserContextAdapter(ctx))
          .parse()
          .foreach(s => {
            ctx.autoGeneratedAnnotation(s)
            payload.setWithoutId(PayloadModel.Schema, tracking(s, payload), Annotations.inferred())
          })

    }

    payload
  }
}

case class Raml08PayloadParser(entry: YMapEntry, parentId: String, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlPayloadParser(entry: YMapEntry, parentId: String, parseOptional) {

  override def parse(): Payload = {
    val payload = super.parse()

    val mediaType = payload.mediaType.value()

    if (mediaType.endsWith("?")) {
      payload.set(PayloadModel.Optional, value = true)
      payload.set(PayloadModel.MediaType, mediaType.stripSuffix("?"))
    }

    entry.value.tagType match {
      case YType.Null =>
        val shape: AnyShape = AnyShape()
        val anyShape        = shape.withName("schema")
        payload.setWithoutId(PayloadModel.Schema, anyShape, Annotations.synthesized())

      case YType.Map =>
        if (List("application/x-www-form-urlencoded", "multipart/form-data").contains(mediaType)) {
          Raml08WebFormParser(entry.value.as[YMap], payload.id)
            .parse()
            .foreach(s => payload.setWithoutId(PayloadModel.Schema, s, Annotations.inferred()))
        } else {
          Raml08TypeParser(entry, (shape: Shape) => Unit, isAnnotation = false, AnyDefaultType)(
            WebApiShapeParserContextAdapter(ctx))
            .parse()
            .foreach(s => payload.setWithoutId(PayloadModel.Schema, tracking(s, payload), Annotations.inferred()))

        }

      case _ =>
        ctx.violation(
          InvalidPayload,
          payload,
          "Invalid payload. Payload must be a map or null"
        )
    }

    payload
  }

}

case class Raml08WebFormParser(map: YMap, parentId: String)(implicit ctx: RamlWebApiContext) {
  def parse(): Option[NodeShape] = {
    map
      .key("formParameters")
      .flatMap(entry => {
        val entries = entry.value.as[YMap].entries
        entries.headOption.map {
          _ =>
            val nodeShape: NodeShape = NodeShape(entry.value)
            val webFormShape         = nodeShape.withName("schema")
            entries.foreach(e => {

              Raml08TypeParser(e,
                               (shape: Shape) => Unit,
                               isAnnotation = false,
                               StringDefaultType)(WebApiShapeParserContextAdapter(ctx))
                .parse()
                .foreach {
                  s =>
                    val property = webFormShape.withProperty(e.key.toString())
                    // by default 0.8 fields are optional
                    property.withMinCount(0)
                    // find for an explicit annotation
                    e.value.asOption[YMap] match {
                      case Some(nestedMap) =>
                        nestedMap.key(
                          "required",
                          entry => {
                            val required = ScalarNode(entry.value).boolean().value.asInstanceOf[Boolean]
                            property.setWithoutId(PropertyShapeModel.MinCount,
                                         AmfScalar(if (required) 1 else 0),
                                         Annotations(entry) += ExplicitField())
                          }
                        )
                      case _ =>
                    }
                    property.add(Annotations(e)).withRange(s)
                }
            })
            webFormShape
              .set(NodeShapeModel.Closed, value = true) // RAML 0.8 does not support open node shapes (see APIMF-732)
            webFormShape
        }
      })
  }
}

abstract class RamlPayloadParser(entry: YMapEntry, parentId: String, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext) {

  def parse(): Payload = {
    val name    = ScalarNode(entry.key)
    val payload = Payload(Annotations(entry))
    payload.set(PayloadModel.MediaType, name.string(), Annotations.inferred())
    payload
  }
}
