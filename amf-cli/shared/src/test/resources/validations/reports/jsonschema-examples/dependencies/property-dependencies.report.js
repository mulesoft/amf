Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/property-dependencies.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have property otherProperty when property undefinedProperty is present
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/property-dependencies.raml/#/web-api/endpoint/end-points/%2Fep1/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/property-dependencies.raml/#/web-api/endpoint/end-points/%2Fep1/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Position: Some(LexicalInformation([(26,0)-(28,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/property-dependencies.raml
