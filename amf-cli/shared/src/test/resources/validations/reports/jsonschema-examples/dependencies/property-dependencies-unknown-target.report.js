Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/property-dependencies-unkown-target.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have property otherUndefinedProperty when property undefinedProperty is present
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/property-dependencies-unkown-target.raml#/web-api/endpoint/end-points/%2Fep1/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/property-dependencies-unkown-target.raml#/web-api/endpoint/end-points/%2Fep1/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Position: Some(LexicalInformation([(21,0)-(23,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/property-dependencies-unkown-target.raml
