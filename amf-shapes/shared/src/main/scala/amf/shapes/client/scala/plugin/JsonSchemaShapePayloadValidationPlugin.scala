package amf.shapes.client.scala.plugin

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.{
  AMFShapePayloadValidationPlugin,
  AMFShapePayloadValidator,
  ShapeValidationConfiguration,
  ValidatePayloadRequest
}
import amf.core.internal.remote.Mimes._
import amf.shapes.internal.domain.apicontract.unsafe.JsonSchemaValidatorBuilder

trait JsonSchemaShapePayloadValidationPlugin extends AMFShapePayloadValidationPlugin with CommonShapeValidation {
  override val id: String                   = "AMF Payload Validation"
  private val payloadMediaType: Seq[String] = Seq(`application/json`, `application/yaml`, `text/vnd.yaml`)

  override def applies(element: ValidatePayloadRequest): Boolean = {
    val ValidatePayloadRequest(shape, mediaType, _) = element
    isAnyShape(shape) && supportsMediaType(mediaType) && !isAvroSchemaShape(shape)
  }

  private def supportsMediaType(mediaType: String) = payloadMediaType.contains(mediaType)
}
object JsonSchemaShapePayloadValidationPlugin extends JsonSchemaShapePayloadValidationPlugin {

  override def validator(
      shape: Shape,
      mediaType: String,
      config: ShapeValidationConfiguration,
      validationMode: ValidationMode
  ): AMFShapePayloadValidator = {
    JsonSchemaValidatorBuilder.payloadValidator(shape, mediaType, validationMode, config)
  }
}

private[amf] object FailFastJsonSchemaPayloadValidationPlugin extends JsonSchemaShapePayloadValidationPlugin {

  override def validator(
      shape: Shape,
      mediaType: String,
      config: ShapeValidationConfiguration,
      validationMode: ValidationMode
  ): AMFShapePayloadValidator = {
    JsonSchemaValidatorBuilder.failFastValidator(shape, mediaType, validationMode, config)
  }
}
