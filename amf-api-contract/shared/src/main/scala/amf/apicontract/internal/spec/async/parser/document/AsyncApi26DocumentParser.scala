package amf.apicontract.internal.spec.async.parser.document

import amf.apicontract.internal.spec.async.AsyncApi26
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.declarations.Async26DeclarationParser
import amf.core.internal.parser.Root

object AsyncApi26DocumentParser {
  // Doesn't add new functionality to previous version
  def apply(root: Root)(implicit ctx: AsyncWebApiContext) =
    AsyncApi21DocumentParser(root, AsyncApi26, Async26DeclarationParser())
}
