Model: file://amf-client/shared/src/test/resources/pce/invalid-property-dependencies.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have property B when property A is present
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/pce/invalid-property-dependencies.raml#/web-api/end-points/%2Ffoo/post/request/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/pce/invalid-property-dependencies.raml#/web-api/end-points/%2Ffoo/post/request/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(30,16)-(32,17)]))
  Location: file://amf-client/shared/src/test/resources/pce/invalid-property-dependencies.raml
