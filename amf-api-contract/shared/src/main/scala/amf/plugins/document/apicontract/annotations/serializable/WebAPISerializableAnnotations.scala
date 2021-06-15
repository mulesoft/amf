package amf.plugins.document.apicontract.annotations.serializable

import amf.core.client.scala.model.domain.AnnotationGraphLoader
import amf.core.internal.annotations.{AutoGeneratedName, DeclaredElement, ExternalFragmentRef, InlineElement}
import amf.core.internal.annotations.serializable.SerializableAnnotations
import amf.plugins.document.apicontract.annotations.{
  FormBodyParameter,
  JSONSchemaId,
  LocalLinkPath,
  ParameterNameForPayload,
  ParsedJSONSchema,
  ParsedRamlDatatype,
  RequiredParamPayload
}

private[amf] object WebAPISerializableAnnotations extends SerializableAnnotations {

  override val annotations: Map[String, AnnotationGraphLoader] = Map(
    "parsed-json-schema"         -> ParsedJSONSchema,
    "parsed-raml-datatype"       -> ParsedRamlDatatype,
    "external-fragment-ref"      -> ExternalFragmentRef,
    "json-schema-id"             -> JSONSchemaId,
    "declared-element"           -> DeclaredElement,
    "inline-element"             -> InlineElement,
    "local-link-path"            -> LocalLinkPath,
    "form-body-parameter"        -> FormBodyParameter,
    "parameter-name-for-payload" -> ParameterNameForPayload,
    "required-param-payload"     -> RequiredParamPayload,
    "auto-generated-name"        -> AutoGeneratedName
  )

}
