Model: file://amf-cli/shared/src/test/resources/validations/oas3/not-constraint.json
Profile: OAS 3.0
Conforms? true
Number of results: 1

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be valid
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/not-constraint.json/#/web-api/endpoint/end-points/%2Ftest/supportedOperation/get/returns/200/payload/application%2Fjson/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/not-constraint.json/#/web-api/endpoint/end-points/%2Ftest/supportedOperation/get/returns/200/payload/application%2Fjson/examples/example/invalid
  Position: Some(LexicalInformation([(25,29)-(25,42)]))
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/not-constraint.json
