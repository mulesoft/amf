ModelId: file://amf-cli/shared/src/test/resources/semantic/validation/api-not-any-schema.raml
    Profile:
        Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#annotation-schema-must-be-any
    Message: Annotation schema must be any for api-extensions override
Severity: Violation
Target: file://amf-cli/shared/src/test/resources/semantic/validation/api-not-any-schema.raml#/declares/pagination
    Property:
        Range: [(4,2)-(6,0)]
Location: file://amf-cli/shared/src/test/resources/semantic/validation/api-not-any-schema.raml
