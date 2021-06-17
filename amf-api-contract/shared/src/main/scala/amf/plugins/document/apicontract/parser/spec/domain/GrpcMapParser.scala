package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes.{IDENTIFIER, KEY_TYPE, MAP_NAME}
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import org.mulesoft.antlrast.ast.{ASTElement, Node}

case class GrpcMapParser(ast: Node)(implicit ctx: GrpcWebApiContext) extends AntlrASTParserHelper {
  val propertyMap: PropertyShape = PropertyShape(toAnnotations(ast))


  def parse(adopt: PropertyShape => Unit): PropertyShape = {
    parseName(adopt)
    parseFields()
    propertyMap
  }

  def parseName(adopt: PropertyShape => Unit): Unit = withName(ast, MAP_NAME, propertyMap, { _ => adopt(propertyMap) })

  def parseFieldName(field: ASTElement): String = {
    path(field, Seq(MAP_NAME, IDENTIFIER)) match {
      case Some(f) =>
        withOptTerminal(f) {
          case Some(t) =>
            t.value
          case _       =>
            astError(propertyMap.id, "missing mandatory Proto3 map field name", toAnnotations(field))
            ""
        }
      case _       =>
        astError(propertyMap.id, "missing mandatory Proto3 map field name", toAnnotations(field))
        ""
    }
  }

  def parseFields(): Any = {
    val range = parseFieldRange(ast).getOrElse(ScalarShape().withDataType(XsdTypes.xsdString.iri()))
    val key = parseFieldRange(ast, KEY_TYPE).getOrElse(ScalarShape().withDataType(XsdTypes.xsdString.iri()))
    val mapValueSchema = NodeShape(toAnnotations(ast)).adopted(propertyMap.id)
    mapValueSchema.withAdditionalPropertiesSchema(range)
    mapValueSchema.withAdditionalPropertiesKeySchema(key)

    val order = parseFieldNumber(ast).getOrElse(0)
    val name = parseFieldName(ast)
    propertyMap
      .withName(name)
      .withSerializationOrder(order)
      .withRange(mapValueSchema)
  }
}
