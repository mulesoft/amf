Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api8.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: formOfPayments[0].auditDetails.formOfPayment.createTime should be number
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api8.raml#/web-api/endpoint/end-points/%2Fep2/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api8.raml#/web-api/endpoint/end-points/%2Fep2/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Position: Some(LexicalInformation([(80,0)-(86,40)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api8.raml
