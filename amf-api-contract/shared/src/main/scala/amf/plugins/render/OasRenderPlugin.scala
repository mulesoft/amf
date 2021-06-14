package amf.plugins.render

import amf.core.client.scala.model.document.{Document, Fragment, Module}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.plugins.render.RenderInfo
import amf.plugins.document.apicontract.model.{Extension, Overlay}
import amf.plugins.domain.apicontract.models.api.Api

trait OasRenderPlugin extends ApiRenderPlugin {

  override def applies(element: RenderInfo): Boolean = element.unit match {
    case _: Overlay         => true
    case _: Extension       => true
    case document: Document => document.encodes.isInstanceOf[Api]
    case module: Module =>
      module.declares exists {
        case _: DomainElement => true
        case _                => false
      }
    case _: Fragment => true
    case _           => false
  }
}
