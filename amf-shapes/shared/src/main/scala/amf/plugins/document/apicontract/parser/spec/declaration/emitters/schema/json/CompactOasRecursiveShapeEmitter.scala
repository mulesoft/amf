package amf.plugins.document.apicontract.parser.spec.declaration.emitters.schema.json

import amf.core.client.scala.model.domain.RecursiveShape
import amf.core.internal.render.SpecOrdering
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas.OasRecursiveShapeEmitter
import org.yaml.model.YDocument.EntryBuilder

class CompactOasRecursiveShapeEmitter(recursive: RecursiveShape,
                                      ordering: SpecOrdering,
                                      schemaPath: Seq[(String, String)])(implicit spec: OasLikeShapeEmitterContext)
    extends OasRecursiveShapeEmitter(recursive, ordering, schemaPath) {

  override def emit(b: EntryBuilder): Unit = {
    val label = recursive.fixpoint
      .option()
      .flatMap { fixpoint =>
        spec.definitionsQueue.labelOfShape(fixpoint)
      }
      .orElse {
        recursive.fixpointTarget.map(spec.definitionsQueue.enqueue)
      }
    label match {
      case Some(name) => b.entry("$ref", s"#${spec.schemasDeclarationsPath}$name")
      case None       => super.emit(b) // default to normal behaviour
    }
  }
}
