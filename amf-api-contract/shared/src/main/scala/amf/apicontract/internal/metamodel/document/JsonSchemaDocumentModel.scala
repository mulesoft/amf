package amf.apicontract.internal.metamodel.document

import amf.apicontract.client.scala.model.document.JsonSchemaDocument
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Document
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.document.{BaseUnitModel, DocumentModel}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

/** JSON Schema Fragment metamodel
  *
  * A JSON Schema Fragment is a parsing Unit that encodes a single Shape and could declares Shapes. It main purpose is
  * to expose the encoded and declared references so they can be re-used
  */
object JsonSchemaDocumentModel extends DocumentModel {

  val SchemaVersion: Field = Field(
    Str,
    Document + "schemaVersion",
    ModelDoc(ModelVocabularies.AmlDoc, "SchemaVersion", "JSON Schema version of the document")
  )

  override val `type`: List[ValueType] = Namespace.Document + "JsonSchemaDocument" :: DocumentModel.`type`

  override val fields: List[Field] = SchemaVersion :: DocumentModel.fields

  override def modelInstance: AmfObject = JsonSchemaDocument()
}
