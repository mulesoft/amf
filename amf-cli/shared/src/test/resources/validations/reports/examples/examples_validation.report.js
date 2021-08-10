Model: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml
Profile: RAML 1.0
Conforms? false
Number of results: 4

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: b should be integer
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/A/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/A/examples/example/default-example
  Position: Some(LexicalInformation([(13,0)-(16,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be integer
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/scalar/D/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/scalar/D/examples/example/default-example
  Position: Some(LexicalInformation([(33,13)-(33,17)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'g'
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/H/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/H/examples/example/default-example
  Position: Some(LexicalInformation([(52,12)-(55,13)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/I
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/I/examples/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Position: Some(LexicalInformation([(62,12)-(67,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml
