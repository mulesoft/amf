Model: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: lastname should be string
middlename should be null

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml#/declares/shape/User/examples/example/wrong-type
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml#/declares/shape/User/examples/example/wrong-type
  Position: Some(LexicalInformation([(15,0)-(18,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'middlename'
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml#/declares/shape/User/examples/example/missing-field
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml#/declares/shape/User/examples/example/missing-field
  Position: Some(LexicalInformation([(19,0)-(21,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml
