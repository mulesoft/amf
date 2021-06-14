package amf.plugins.domain.shapes.resolution.stages

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.client.scala.transform.stages.elements.resolution.ReferenceResolution
import amf.core.client.scala.transform.stages.selectors.LinkSelector
import amf.plugins.domain.apicontract.models.Request
import amf.core.client.scala.transform.stages.selectors.Selector

object RequestParamsLinkStage extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit =
    new RequestParamsLinkStage(errorHandler).resolve(model)
}

private class RequestParamsLinkStage(val errorHandler: AMFErrorHandler) {
  def resolve[T <: BaseUnit](model: T): T = {
    model.transform(LinkSelector && ReqWithParametersSelector, transform)(errorHandler).asInstanceOf[T]
  }

  private def transform(e: DomainElement, isCycle: Boolean): Option[DomainElement] = {
    val referenceResolution =
      new ReferenceResolution(errorHandler, customDomainElementTransformation = customDomainElementTransformation)
    referenceResolution.transform(e, conditions = Seq(ReferenceResolution.ASSERT_DIFFERENT))
  }

  private def customDomainElementTransformation(resolved: DomainElement, link: Linkable): DomainElement = {
    (resolved, link) match {
      case (resolvedReq: Request, link: Request) =>
        val copied = Request(resolvedReq.fields.copy(), resolvedReq.annotations.copy())
        copied.id = link.id
        setParams(link, copied)
        copied
      case _ => resolved
    }
  }

  private def setParams(from: Request, to: Request): Unit = {
    if (from.cookieParameters.nonEmpty) to.withCookieParameters(from.cookieParameters)
    if (from.headers.nonEmpty) to.withHeaders(from.headers)
    if (from.uriParameters.nonEmpty) to.withUriParameters(from.uriParameters)
    if (from.queryParameters.nonEmpty) to.withQueryParameters(from.queryParameters)
  }

}

object ReqWithParametersSelector extends Selector {
  override def apply(element: DomainElement): Boolean = element match {
    case req: Request => hasParameters(req)
    case _            => false
  }

  private def hasParameters(req: Request) =
    (req.queryParameters ++ req.uriParameters ++ req.cookieParameters ++ req.headers).nonEmpty
}
