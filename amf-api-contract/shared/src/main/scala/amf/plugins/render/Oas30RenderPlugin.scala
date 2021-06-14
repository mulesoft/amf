package amf.plugins.render

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, ExternalFragment, Fragment, Module}
import amf.core.internal.plugins.render.AMFRenderPlugin
import amf.core.internal.remote.Vendor
import amf.plugins.common.Oas30MediaTypes
import amf.plugins.document.apicontract.contexts.emitter.oas.Oas3SpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.oas.{Oas30ModuleEmitter, Oas3DocumentEmitter, OasFragmentEmitter}
import org.yaml.model.{YDocument, YNode}

object Oas30RenderPlugin extends OasRenderPlugin {

  override def vendor: Vendor = Vendor.OAS30

  override def mediaTypes: Seq[String] = Oas30MediaTypes.mediaTypes

  override def defaultSyntax(): String = AMFRenderPlugin.APPLICATION_JSON

  override def priority: PluginPriority = NormalPriority

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderOptions: RenderOptions,
                                            errorHandler: AMFErrorHandler): Option[YDocument] =
    unit match {
      case module: Module => Some(Oas30ModuleEmitter(module)(specContext(renderOptions, errorHandler)).emitModule())
      case document: Document =>
        Some(Oas3DocumentEmitter(document)(specContext(renderOptions, errorHandler)).emitDocument())
      case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
      case fragment: Fragment =>
        Some(new OasFragmentEmitter(fragment)(specContext(renderOptions, errorHandler)).emitFragment())
      case _ => None
    }

  private def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): Oas3SpecEmitterContext =
    new Oas3SpecEmitterContext(errorHandler, options = options.shapeRenderOptions)
}
