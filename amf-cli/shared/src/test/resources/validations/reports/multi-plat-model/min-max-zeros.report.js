Model: file://amf-cli/shared/src/test/resources/validations/facets/min-max-zeros.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 0
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/facets/min-max-zeros.raml#/type/property/property/SSN/scalar/SSN%3F/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/facets/min-max-zeros.raml#/type/property/property/SSN/scalar/SSN%3F/examples/example/default-example
  Position: Some(LexicalInformation([(7,13)-(7,22)]))
  Location: file://amf-cli/shared/src/test/resources/validations/facets/min-max-zeros.raml
