package amf.apicontract.internal.spec.async.parser.domain.declarations

import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import org.yaml.model.YMap

object Async24DeclarationParser extends AsyncDeclarationParser {
  override def parseDeclarations(map: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Unit = {
    Async23DeclarationParser.parseDeclarations(map, parent)
    // TODO: add stuff....
  }
}
