ModelId: file://amf-cli/shared/src/test/resources/avro/schemas/long-wrong-default.json
Profile: 
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#invalid-avro-schema
  Message: Internal error during Avro validation: Error: invalid "long": "wrong default type"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/avro/schemas/long-wrong-default.json#/shape/test
  Property: 
  Range: 
  Location: file://amf-cli/shared/src/test/resources/avro/schemas/long-wrong-default.json
