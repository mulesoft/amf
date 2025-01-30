package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.oas.parser.domain.OasLikeSecuritySchemeParser
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.YPart

case class Async2SecuritySchemeParser(part: YMapEntryLike, adopt: SecurityScheme => SecurityScheme)(implicit
    ctx: AsyncWebApiContext
) extends OasLikeSecuritySchemeParser(part, adopt)
