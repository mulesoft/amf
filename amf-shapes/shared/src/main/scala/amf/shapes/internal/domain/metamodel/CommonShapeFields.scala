package amf.shapes.internal.domain.metamodel

import amf.core.client.scala.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Bool, Double, Int, Str}
import amf.core.internal.metamodel.domain.{ExternalModelVocabularies, ModelDoc, ModelVocabularies}

trait CommonShapeFields {

  val Pattern =
    Field(Str, Shacl + "pattern", ModelDoc(ExternalModelVocabularies.Shacl, "pattern", "Pattern constraint"))

  val MinLength =
    Field(Int, Shacl + "minLength", ModelDoc(ExternalModelVocabularies.Shacl, "minLength", "Minimum lenght constraint"))

  val MaxLength =
    Field(Int, Shacl + "maxLength", ModelDoc(ExternalModelVocabularies.Shacl, "maxLength", "Maximum length constraint"))

  val Minimum = Field(
    Double,
    Shacl + "minInclusive",
    ModelDoc(ExternalModelVocabularies.Shacl, "minInclusive", "Minimum inclusive constraint")
  )

  val Maximum = Field(
    Double,
    Shacl + "maxInclusive",
    ModelDoc(ExternalModelVocabularies.Shacl, "max. inclusive", "Maximum inclusive constraint")
  )

  val ExclusiveMinimum = Field(
    Bool,
    Shacl + "minExclusive",
    ModelDoc(ExternalModelVocabularies.Shacl, "min. exclusive", "Minimum exclusive constraint")
  )

  val ExclusiveMaximum = Field(
    Bool,
    Shacl + "maxExclusive",
    ModelDoc(ExternalModelVocabularies.Shacl, "max. exclusive", "Maximum exclusive constraint")
  )

  val ExclusiveMinimumNumeric = Field(
    Double,
    Shapes + "exclusiveMinimumNumeric",
    ModelDoc(ModelVocabularies.Shapes, "min. exclusive numeric", "Minimum exclusive constraint"),
    deprecated = true
  )

  val ExclusiveMaximumNumeric = Field(
    Double,
    Shapes + "exclusiveMaximumNumeric",
    ModelDoc(ModelVocabularies.Shapes, "max. exclusive numeric", "Maximum exclusive constraint"),
    deprecated = true
  )

  val Format = Field(Str, Shapes + "format", ModelDoc(ModelVocabularies.Shapes, "format", "Format constraint"))

  val MultipleOf =
    Field(Double, Shapes + "multipleOf", ModelDoc(ModelVocabularies.Shapes, "multiple of", "Multiple of constraint"))

  val commonOASFields =
    List(Pattern, MinLength, MaxLength, Minimum, Maximum, ExclusiveMinimum, ExclusiveMaximum, ExclusiveMinimumNumeric, ExclusiveMaximumNumeric, Format, MultipleOf)
}
