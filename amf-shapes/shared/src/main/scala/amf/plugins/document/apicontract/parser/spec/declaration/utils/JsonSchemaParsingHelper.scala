package amf.plugins.document.apicontract.parser.spec.declaration.utils

import amf.core.client.scala.model.domain.Shape
import amf.plugins.document.apicontract.parser.ShapeParserContext
import amf.plugins.domain.shapes.models.UnresolvedShape
import org.yaml.model.YMapEntry

object JsonSchemaParsingHelper {
  def createTemporaryShape(adopt: Shape => Unit,
                           schemaEntry: YMapEntry,
                           ctx: ShapeParserContext,
                           fullRef: String): UnresolvedShape = {
    val tmpShape =
      UnresolvedShape(fullRef, schemaEntry)
        .withName(fullRef)
        .withId(fullRef)
        .withSupportsRecursion(true)
    tmpShape.unresolved(fullRef, schemaEntry)(ctx)
    tmpShape.withContext(ctx)
    adopt(tmpShape)
    ctx.registerJsonSchema(fullRef, tmpShape)
    tmpShape
  }
}
