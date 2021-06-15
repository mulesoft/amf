package amf.plugins.domain.apicontract.models

import amf.core.client.scala.model.domain.Shape

trait SchemaContainer {
  def schema: Shape
  def setSchema(shape: Shape): Shape
  def id: String
  def examples: Seq[Example]
  def removeExamples(): Unit
}
