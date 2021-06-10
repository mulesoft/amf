package amf.plugins.parser.dialect

import amf.core.parser.Fields
import amf.plugins.document.vocabularies.model.domain.{NodeMappable, NodeMapping, UnionNodeMapping}
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}


case class NodeShapeTransformer(shape: NodeShape, ctx: ShapeTransformationContext) {

  val nodeMapping: NodeMapping = NodeMapping(Fields(),shape.annotations).withId(shape.id)

  def transform(): NodeMapping = {
    updateContext()
    nameShape()

    val propertyMappings = shape.properties.map { property =>
      PropertyShapeTransformer(property, ctx).transform()
    }

    checkInheritance()
    checkSemantics()
    nodeMapping.withPropertiesMapping(propertyMappings)

  }

  private def checkInheritance(): Unit = {
    val superSchemas = shape.and
    if (superSchemas.nonEmpty) { // @TODO: support more than 1 super schema
      val hierarchy = superSchemas.map { case s: AnyShape =>
        val transformed = ShapeTransformer(s, ctx).transform()
        transformed match {
          case nm: NodeMapping       => nm.link[NodeMapping](nm.name.value())
          case unm: UnionNodeMapping => unm.link[UnionNodeMapping](unm.name.value())
        }
      }
      nodeMapping.withExtends(hierarchy)
    }
  }

  private def checkSemantics(): Unit = {
    ctx.semantics.typeMappings match {
      case types if types.nonEmpty =>
        nodeMapping.withNodeTypeMapping(types.head.value()) // @TODO: support multiple type mappings
      case _                       =>
        // ignore
    }
  }

  private def nameShape() {
    shape.displayName.option() match {
      case Some(name) => nodeMapping.withName(name.replaceAll(" ", ""))
      case _          => ctx.genName(nodeMapping)
    }
  }

  private def updateContext(): Unit = {
    ctx.registerNodeMapping(nodeMapping)
  }
}
