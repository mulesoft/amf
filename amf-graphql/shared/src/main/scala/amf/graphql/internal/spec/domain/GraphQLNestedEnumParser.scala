package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.{DomainElement, ScalarNode}
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.ScalarShape
import org.mulesoft.antlrast.ast.{ASTElement, Node, Terminal}

class GraphQLNestedEnumParser(enumTypeDef: Node)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {
  val enum: ScalarShape = ScalarShape(toAnnotations(enumTypeDef)).withDataType(Namespace.XsdTypes.xsdString.iri())

  def parse(parentId: String): ScalarShape = {
    parseName()
    enum.adopted(parentId)
    parseValues()
    GraphQLDirectiveApplicationParser(enumTypeDef, enum).parse(enum.id)
    enum
  }

  private def parseName(): Unit = {
    val name = findName(enumTypeDef, "AnonymousEnum", "Missing enumeration type name", enum.id)
    enum.withName(name)
  }

  private def parseValues(): Unit = {
    path(enumTypeDef, Seq(ENUM_VALUES_DEFINITION)) map { case valuesNode: Node =>
      val values = collect(valuesNode, Seq(ENUM_VALUE_DEFINITION)) map { case valueDefNode: Node =>
        getEnumValue(valueDefNode) match {
          case Some(value: ScalarNode) =>
            parseDirectives(valueDefNode, value)
            value
          case None => ScalarNode()
        }
      }
      enum.withValues(values)
    }
  }

  private def getEnumValue(valueNode: Node): Option[ScalarNode] =
    valueFrom(valueNode, Seq(ENUM_VALUE, NAME))
      .orElse(valueFrom(valueNode, Seq(ENUM_VALUE, NAME, KEYWORD)))

  private def valueFrom(element: ASTElement, pathToValue: Seq[String]): Option[ScalarNode] = {
    path(element, pathToValue) match {
      case Some(n: Node) if hasTerminalChild(n) =>
        val t = n.children.head.asInstanceOf[Terminal]
        val s = ScalarNode(t.value, Some(XsdTypes.xsdString.iri()), toAnnotations(t)).withName(t.value)
        Some(s.adopted(enum.id))
      case _ => None
    }
  }

  private def hasTerminalChild(n: Node) = n.children.head.isInstanceOf[Terminal]

  private def parseDirectives(n: Node, element: DomainElement): Unit =
    GraphQLDirectiveApplicationParser(n, element).parse(element.id)
}
