Model: file://amf-cli/shared/src/test/resources/validations/examples/discriminator2.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: phone should match pattern "^[0-9|-]+$"
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/discriminator2.raml/#/web-api/endpoint/end-points/%2Forgs%2F%7BorgId%7D/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/discriminator2.raml/#/web-api/endpoint/end-points/%2Forgs%2F%7BorgId%7D/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Position: Some(LexicalInformation([(44,0)-(52,29)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/discriminator2.raml
