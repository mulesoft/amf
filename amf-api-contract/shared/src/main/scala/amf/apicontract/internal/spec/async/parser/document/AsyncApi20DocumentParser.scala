package amf.apicontract.internal.spec.async.parser.document

import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.declarations.{Async20DeclarationParser, AsyncDeclarationParser}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec.ASYNC20

case class AsyncApi20DocumentParser(root: Root, declarationParser: AsyncDeclarationParser = Async20DeclarationParser())(
    override implicit val ctx: AsyncWebApiContext
) extends AsyncApiDocumentParser(root, ASYNC20, declarationParser)(ctx)
