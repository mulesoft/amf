Model: file://amf-cli/shared/src/test/resources/validations/resource_types/parametrized-includes-of-examples/api.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be integer
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/resource_types/parametrized-includes-of-examples/api.raml#/web-api/endpoint/%2Fendpoint2/supportedOperation/get/request/default/scalar/default/example/myExample
  Property: file://amf-cli/shared/src/test/resources/validations/resource_types/parametrized-includes-of-examples/api.raml#/web-api/endpoint/%2Fendpoint2/supportedOperation/get/request/default/scalar/default/example/myExample
  Position: Some(LexicalInformation([(3,9)-(3,27)]))
  Location: file://amf-cli/shared/src/test/resources/validations/resource_types/parametrized-includes-of-examples/example.raml
