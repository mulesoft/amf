package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.parser.spec.async.emitters.Draft6ExamplesEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft6SchemaVersion,
  JSONSchemaDraft7SchemaVersion
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{OasLikeShapeEmitterContext, ShapeEmitterContext}
import amf.plugins.document.webapi.parser.spec.oas.emitters.OasExampleEmitters
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, ExampleModel}
import amf.plugins.domain.shapes.models.{AnyShape, Example}

import scala.collection.mutable.ListBuffer

object OasAnyShapeEmitter {
  def apply(shape: AnyShape,
            ordering: SpecOrdering,
            references: Seq[BaseUnit],
            pointer: Seq[String] = Nil,
            schemaPath: Seq[(String, String)] = Nil,
            isHeader: Boolean = false)(implicit spec: OasLikeShapeEmitterContext): OasAnyShapeEmitter =
    new OasAnyShapeEmitter(shape, ordering, references, pointer, schemaPath, isHeader)(spec)
}

class OasAnyShapeEmitter(shape: AnyShape,
                         ordering: SpecOrdering,
                         references: Seq[BaseUnit],
                         pointer: Seq[String] = Nil,
                         schemaPath: Seq[(String, String)] = Nil,
                         isHeader: Boolean = false)(implicit spec: OasLikeShapeEmitterContext)
    extends OasShapeEmitter(shape, ordering, references, pointer, schemaPath) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    if (spec.options.isWithDocumentation) {
      shape.fields
        .entry(AnyShapeModel.Examples)
        .map { f =>
          val examples = spec.filterLocal(f.array.values.collect({ case e: Example => e }))
          if (examples.nonEmpty) {
            val (anonymous, named) =
              examples.partition(e => !e.fields.fieldsMeta().contains(ExampleModel.Name) && !e.isLink)
            result ++= examplesEmitters(anonymous.headOption, anonymous.drop(1) ++ named, isHeader)
          }
        }
      if (spec.schemaVersion isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion)
        shape.fields.entry(AnyShapeModel.Comment).map(c => result += ValueEmitter("$comment", c))
    }

    super.emitters() ++ result
  }

  private def examplesEmitters(main: Option[Example], extensions: Seq[Example], isHeader: Boolean) =
    if (spec.schemaVersion.isBiggerThanOrEqualTo(JSONSchemaDraft6SchemaVersion))
      Draft6ExamplesEmitter(main.toSeq ++ extensions, ordering).emitters()
    else OasExampleEmitters.apply(isHeader, main, ordering, extensions, references).emitters()
}
