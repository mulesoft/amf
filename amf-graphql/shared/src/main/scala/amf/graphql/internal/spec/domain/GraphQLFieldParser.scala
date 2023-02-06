package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION}
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.client.scala.model.domain.operations.ShapeOperation
import org.mulesoft.antlrast.ast.{ASTNode, Node}

case class GraphQLFieldParser(ast: Node, parent: NodeShape)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {

  def parse(setterFn: Either[PropertyShape, ShapeOperation] => Unit): Unit = {
    if (hasArguments)
      GraphQLOperationFieldParser(ast).parse(operation => setterFn(Right(operation)))
    else
      GraphQLPropertyFieldParser(ast, parent).parse(property => setterFn(Left(property)))
  }

  private def hasArguments: Boolean = {
    val arguments = collect(ast, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION))
    arguments.nonEmpty
  }

}
