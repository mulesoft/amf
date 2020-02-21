package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.async.emitters.bindings.AsyncApiServerBindingsEmitter
import amf.plugins.document.webapi.parser.spec.declaration.AnnotationsEmitter
import amf.plugins.document.webapi.parser.spec.domain.{OasServerVariablesEmitter, SecurityRequirementsEmitter}
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.plugins.domain.webapi.metamodel.ServerModel
import amf.plugins.domain.webapi.models.Server
import amf.plugins.domain.webapi.models.bindings.ServerBinding
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiServersEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  val key = "servers"

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val serverEmitters =
      f.array.values.map(x => x.asInstanceOf[Server]).map(new AsyncApiSingleServerEmitter(_, ordering))
    b.entry(
      key,
      _.obj(b => serverEmitters.map(e => e.emit(b)))
    )
  }

  override def position(): Position = pos(f.element.annotations)
}

private class AsyncApiSingleServerEmitter(server: Server, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val result     = ListBuffer[EntryEmitter]()
    val serverName = server.name.value()
    val fs         = server.fields

    val bindingOrphanAnnotations =
      server.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))

    fs.entry(ServerModel.Url).foreach(f => result += ValueEmitter("url", f))
    fs.entry(ServerModel.Protocol).foreach(f => result += ValueEmitter("protocol", f))
    fs.entry(ServerModel.ProtocolVersion).foreach(f => result += ValueEmitter("protocolVersion", f))
    fs.entry(ServerModel.Description).foreach(f => result += ValueEmitter("description", f))
    fs.entry(ServerModel.Variables).foreach(f => result += OasServerVariablesEmitter(f, ordering))
    fs.entry(ServerModel.Security).foreach(f => result += SecurityRequirementsEmitter("security", f, ordering))
    fs.entry(ServerModel.Bindings)
      .foreach(f => result += new AsyncApiBindingsEmitter(f, ordering, bindingOrphanAnnotations))

    result ++= AnnotationsEmitter(server, ordering).emitters

    b.entry(YNode(serverName), _.obj(traverse(ordering.sorted(result), _)))
  }

  override def position(): Position = pos(server.annotations)
}
