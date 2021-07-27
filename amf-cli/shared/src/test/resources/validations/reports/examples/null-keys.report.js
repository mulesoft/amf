Model: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml
Profile: RAML 1.0
Conforms? false
Number of results: 3

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: b should be integer
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml/#/web-api/endpoint/end-points/%2FUsuario/supportedOperation/delete/expects/request/payload/application%2Fjson/application%2Fjson/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml/#/web-api/endpoint/end-points/%2FUsuario/supportedOperation/delete/expects/request/payload/application%2Fjson/application%2Fjson/examples/example/default-example
  Position: Some(LexicalInformation([(22,16)-(24,17)]))
  Location: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml

Level: Warning

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(11,8)-(11,14)]))
  Location: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(20,8)-(20,14)]))
  Location: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml
