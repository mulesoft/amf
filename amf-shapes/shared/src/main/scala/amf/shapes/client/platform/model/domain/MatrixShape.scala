package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.domain.Shape
import amf.shapes.client.scala.domain.models
import amf.shapes.client.scala.model.domain

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class MatrixShape(override private[amf] val _internal: domain.ArrayShape) extends ArrayShape(_internal) {

  @JSExportTopLevel("model.domain.MatrixShape")
  def this() = this(InternalArrayShape())

  override def withItems(items: Shape): this.type = {
    items match {
      case _: ArrayShape => super.withItems(items)
      case _             => throw new Exception("Matrix shapes can only accept arrays as items")
    }
  }
}
