package amf.plugins.domain.apicontract.models

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.plugins.domain.shapes.metamodel.DiscriminatorValueMappingModel
import amf.plugins.domain.shapes.metamodel.DiscriminatorValueMappingModel.{
  DiscriminatorValue,
  DiscriminatorValueTarget
}

case class DiscriminatorValueMapping(fields: Fields, annotations: Annotations) extends DomainElement {

  def value: StrField    = fields.field(DiscriminatorValue)
  def targetShape: Shape = fields.field(DiscriminatorValueTarget)

  def withValue(value: String): this.type      = set(DiscriminatorValue, value)
  def withTargetShape(shape: Shape): this.type = set(DiscriminatorValueTarget, shape)

  override def meta: DiscriminatorValueMappingModel.type = DiscriminatorValueMappingModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    s"/discriminator-value-mapping/${value.value().urlComponentEncoded}"
}

object DiscriminatorValueMapping {
  def apply(): DiscriminatorValueMapping = apply(Annotations())
  def apply(annotations: Annotations)    = new DiscriminatorValueMapping(Fields(), annotations)
}
