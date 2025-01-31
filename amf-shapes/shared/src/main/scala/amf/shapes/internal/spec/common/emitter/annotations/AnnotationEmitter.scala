package amf.shapes.internal.spec.common.emitter.annotations

import amf.aml.internal.annotations.DiscriminatorExtension
import amf.aml.internal.semantic.SemanticExtensionsFacade
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{ArrayNode, CustomizableElement, DataNode, LinkNode, ObjectNode, ScalarNode}
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.annotations.OrphanOasExtension
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationEmitter.ComputeName
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

/** AnnotationsEmitter emits normal Annotations and Semantic Extensions, see also
  * [[amf.shapes.internal.spec.common.parser.AnnotationParser]]
  */
case class AnnotationsEmitter(element: CustomizableElement, ordering: SpecOrdering)(implicit
    spec: ShapeEmitterContext
) {
  def emitters: Seq[EntryEmitter] = {
    element.customDomainProperties
      .filter(shouldEmit)
      .map(spec.annotationEmitter(element, _, ordering))
  }

  private def shouldEmit(customProperty: DomainExtension): Boolean = {
    val filterAnnotations = Seq(classOf[OrphanOasExtension], classOf[DiscriminatorExtension])
    !filterAnnotations.exists { c =>
      customProperty.extension match {
        case node: DataNode => node.annotations.contains(c)
        case _              => false
      }
    }
  }
}

object AnnotationEmitter {
  type ComputeName = DomainExtension => String
}

class AstAnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering, computeName: ComputeName)(implicit
    spec: ShapeEmitterContext
) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    Option(domainExtension.extension).foreach(emitAst(b, _))
  }

  protected def emitAst(b: EntryBuilder, extension: DataNode): Unit = {
    b.complexEntry(
      b => b += computeName(domainExtension),
      b => DataNodeEmitter(extension, ordering)(spec.eh).emit(b)
    )
  }

  override def position(): Position = pos(domainExtension.annotations)
}

case class AnnotationEmitter(
    element: CustomizableElement,
    domainExtension: DomainExtension,
    ordering: SpecOrdering,
    computeName: ComputeName
)(implicit spec: ShapeEmitterContext)
    extends AstAnnotationEmitter(domainExtension, ordering, computeName) {

  override def emit(b: EntryBuilder): Unit = {
    Option(domainExtension.extension) match {
      case Some(node) => emitAst(b, node)
      case None       => emitSemanticExtension(b)
    }
  }

  private def emitSemanticExtension(b: EntryBuilder): Unit = {
    SemanticExtensionsFacade(computeName(domainExtension), spec.config)
      .render(domainExtension, element.typeIris, ordering, spec.options)
      .foreach(_.emit(b))
  }

  override def position(): Position = pos(domainExtension.annotations)
}

case class RamlScalarAnnotationEmitter(extension: DomainExtension, ordering: SpecOrdering)(implicit
    spec: ShapeEmitterContext
) extends EntryEmitter {

  private val name = RamlAnnotationEmitter.computeName(extension)

  override def emit(b: EntryBuilder): Unit = {
    b.complexEntry(
      b => b += name,
      b =>
        Option(extension.extension).foreach { ast =>
          DataNodeEmitter(ast, ordering)(spec.eh).emit(b)
        }
    )
  }

  override def position(): Position = pos(extension.annotations)
}

object OasAstAnnotationEmitter {
  private val computeName: ComputeName = ext => s"x-${ext.name.value()}"

  def apply(domainExtension: DomainExtension, ordering: SpecOrdering)(implicit
      spec: ShapeEmitterContext
  ): AstAnnotationEmitter = {
    new AstAnnotationEmitter(domainExtension, ordering, computeName)
  }
}

object OasAnnotationEmitter {

  val computeName: ComputeName = ext => s"x-${ext.name.value()}"

  def apply(element: CustomizableElement, domainExtension: DomainExtension, ordering: SpecOrdering)(implicit
      spec: ShapeEmitterContext
  ): AnnotationEmitter = {
    AnnotationEmitter(element, domainExtension, ordering, computeName)
  }
}

object RamlAnnotationEmitter {

  val computeName: ComputeName = ext => s"(${ext.name.value()})"

  def apply(element: CustomizableElement, domainExtension: DomainExtension, ordering: SpecOrdering)(implicit
      spec: ShapeEmitterContext
  ): AnnotationEmitter = {
    AnnotationEmitter(element, domainExtension, ordering, computeName)
  }
}
