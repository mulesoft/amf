package amf.emit

import amf.apicontract.client.scala.config.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.client.environment.AsyncAPIConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.Syntax.Syntax
import amf.core.internal.remote.{Platform, Vendor}
import amf.core.internal.render.AMFSerializer
import amf.core.internal.unsafe.PlatformSecrets
import amf.plugins.domain.VocabulariesRegister

import scala.concurrent.{ExecutionContext, Future}

// TODO: this is only here for compatibility with the test suite
class AMFRenderer(unit: BaseUnit, vendor: Vendor, options: RenderOptions, syntax: Option[Syntax])
    extends PlatformSecrets {

  // Remod registering
  VocabulariesRegister.register(platform)
//  amf.core.registries.AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
//  amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(APIDomainPlugin)
//  amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(DataShapesDomainPlugin)

  /** Print ast to string. */
  def renderToString(implicit executionContext: ExecutionContext): String = render()

  /** Print ast to file. */
  def renderToFile(remote: Platform, path: String)(implicit executionContext: ExecutionContext): Future[Unit] = {
    val result = render()
    remote.write(path, result)
  }

  private def render()(implicit executionContext: ExecutionContext): String = {
    val config = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()).withRenderOptions(options)
    new AMFSerializer(unit, vendor.mediaType, config.renderConfiguration).renderToString
  }
}

object AMFRenderer {
  def apply(unit: BaseUnit, vendor: Vendor, options: RenderOptions, syntax: Option[Syntax] = None): AMFRenderer =
    new AMFRenderer(unit, vendor, options, syntax)
}
