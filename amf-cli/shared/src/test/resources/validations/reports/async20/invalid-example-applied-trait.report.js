Model: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml
Profile: ASYNC 2.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml#/async-api/endpoint/end-points/%2Fuser%2Fsignedup/supportedOperation/subscribe/returns/resp/default-response/example/default-example_2
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml#/async-api/endpoint/end-points/%2Fuser%2Fsignedup/supportedOperation/subscribe/returns/resp/default-response/example/default-example_2
  Position: Some(LexicalInformation([(12,0)-(13,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a should be number
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml#/async-api/endpoint/end-points/%2Fuser%2Fsignedup/supportedOperation/subscribe/returns/resp/default-response/example/default-example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml#/async-api/endpoint/end-points/%2Fuser%2Fsignedup/supportedOperation/subscribe/returns/resp/default-response/example/default-example_1
  Position: Some(LexicalInformation([(14,0)-(16,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml
