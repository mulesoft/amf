Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/not/api4.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: foo should NOT be valid
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/not/api4.raml#/web-api/endpoint/end-points/%2Fep1/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/not/api4.raml#/web-api/endpoint/end-points/%2Fep1/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Position: Some(LexicalInformation([(22,0)-(25,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/not/api4.raml
