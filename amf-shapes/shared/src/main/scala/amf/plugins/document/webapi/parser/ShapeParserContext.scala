package amf.plugins.document.webapi.parser

import amf.core.model.domain.Shape
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.parser.{Annotations, ErrorHandlingContext, FutureDeclarations, SearchScope, UnresolvedComponents}
import amf.core.remote.Vendor
import amf.plugins.document.webapi.parser.RamlWebApiContextType.RamlWebApiContextType
import amf.plugins.document.webapi.parser.spec.SpecSyntax
import amf.plugins.document.webapi.parser.spec.common.DataNodeParserContext
import amf.plugins.document.webapi.parser.spec.declaration.TypeInfo
import amf.plugins.document.webapi.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.domain.shapes.models.{AnyShape, CreativeWork, Example}
import org.yaml.model.{YMap, YNode, YPart}

abstract class ShapeParserContext(eh: ParserErrorHandler) extends DataNodeParserContext(eh) with UnresolvedComponents {

  def toOasNext: ShapeParserContext
  def findExample(key: String, scope: SearchScope.Scope): Option[Example]
  def rootContextDocument: String
  def futureDeclarations: FutureDeclarations
  def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit] = None): Option[AnyShape]
  def link(node: YNode): Either[String, YNode]
  def loc: String
  def vendor: Vendor
  def syntax: SpecSyntax
  def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo)
  def shapes: Map[String, Shape]
  def closedShape(node: String, ast: YMap, shape: String): Unit
  def registerJsonSchema(url: String, shape: AnyShape)
  def isMainFileContext: Boolean
  def findNamedExampleOrError(ast: YPart)(key: String): Example
  def findLocalJSONPath(path: String): Option[YMapEntryLike]
  def linkTypes: Boolean
  def findJsonSchema(url: String): Option[AnyShape]
  def findNamedExample(key: String, error: Option[String => Unit] = None): Option[Example]
  def isOasLikeContext: Boolean
  def isOas2Context: Boolean
  def isOas3Context: Boolean
  def isAsyncContext: Boolean
  def isRamlContext: Boolean
  def isOas3Syntax: Boolean
  def isOas2Syntax: Boolean
  def ramlContextType: RamlWebApiContextType
  def promoteExternaltoDataTypeFragment(text: String, fullRef: String, shape: Shape): Shape
  def parseRemoteJSONPath(ref: String): Option[AnyShape]
  def findDocumentations(key: String,
                         scope: SearchScope.Scope,
                         error: Option[String => Unit] = None): Option[CreativeWork]

  def obtainRemoteYNode(ref: String, refAnnotations: Annotations = Annotations()): Option[YNode]
}

object RamlWebApiContextType extends Enumeration {
  type RamlWebApiContextType = Value
  val DEFAULT, RESOURCE_TYPE, TRAIT, EXTENSION, OVERLAY, LIBRARY = Value
}
