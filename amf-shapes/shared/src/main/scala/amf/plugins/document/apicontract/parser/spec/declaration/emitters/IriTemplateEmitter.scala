package amf.plugins.document.apicontract.parser.spec.declaration.emitters

import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.AmfObject
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.domain.apicontract.metamodel.IriTemplateMappingModel
import org.yaml.model.YDocument.EntryBuilder

// TODO: is this for OAS only?
case class IriTemplateEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj { b =>
        val emitters = f
          .arrayValues[AmfObject]
          .flatMap(iriMapping => {
            for {
              variable <- iriMapping.fields.entry(IriTemplateMappingModel.TemplateVariable)
              link     <- iriMapping.fields.entry(IriTemplateMappingModel.LinkExpression)
            } yield {
              ValueEmitter(variable.scalar.toString, link)
            }
          })
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}
