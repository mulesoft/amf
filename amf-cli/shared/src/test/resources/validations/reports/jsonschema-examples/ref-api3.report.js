Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api3.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [1] should be integer
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api3.raml/#/web-api/endpoint/end-points/%2Fep2/supportedOperation/get/returns/200/payload/application%2Fjson/any/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api3.raml/#/web-api/endpoint/end-points/%2Fep2/supportedOperation/get/returns/200/payload/application%2Fjson/any/schema/examples/example/default-example
  Position: Some(LexicalInformation([(29,21)-(29,31)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api3.raml
