package amf.apicontract.internal.spec.oas

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.apicontract.internal.spec.oas.parser.context.{Oas3WebApiContext, OasWebApiContext}
import amf.apicontract.internal.spec.oas.parser.OasFragmentParser
import amf.apicontract.internal.spec.oas.parser.document.{Oas3DocumentParser, OasFragmentParser}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.exception.InvalidDocumentHeaderException
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Oas30, Vendor}
import amf.plugins.document.apicontract.parser.OasHeader.Oas30Header
import amf.plugins.document.apicontract.parser.spec.oas.Oas3DocumentParser
import amf.shapes.internal.spec.contexts.parser.oas.OasWebApiContext

object Oas30ParsePlugin extends OasParsePlugin {

  override def vendor: Vendor = Oas30

  override def applies(element: Root): Boolean = OasHeader(element).contains(Oas30Header)

  override def mediaTypes: Seq[String] = Oas30MediaTypes.mediaTypes

  override protected def parseSpecificVersion(root: Root)(implicit ctx: OasWebApiContext): BaseUnit =
    OasHeader(root) match {
      case Some(Oas30Header) => Oas3DocumentParser(root).parseDocument()
      case Some(f)           => OasFragmentParser(root, Some(f)).parseFragment()
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(vendor.name)
    }

  override protected def context(loc: String,
                                 refs: Seq[ParsedReference],
                                 options: ParsingOptions,
                                 wrapped: ParserContext,
                                 ds: Option[OasWebApiDeclarations]): OasWebApiContext =
    new Oas3WebApiContext(loc, refs, wrapped, ds, options)
}
