package amf.apicontract.client.platform.model.domain

import amf.apicontract.internal.convert.ApiClientConverters.ClientList
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.apicontract.client.scala.model.domain.{TemplatedLink => InternalTemplatedLink}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.shapes.client.platform.model.domain.IriTemplateMapping

@JSExportAll
case class TemplatedLink(override private[amf] val _internal: InternalTemplatedLink)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("TemplatedLink")
  def this() = this(InternalTemplatedLink())

  def name: StrField                          = _internal.name
  def description: StrField                   = _internal.description
  def template: StrField                      = _internal.template
  def operationId: StrField                   = _internal.operationId
  def operationRef: StrField                  = _internal.operationRef
  def mapping: ClientList[IriTemplateMapping] = _internal.mapping.asClient
  def requestBody: StrField                   = _internal.requestBody
  def server: Server                          = _internal.server

  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  def withTemplate(template: String): this.type = {
    _internal.withTemplate(template)
    this
  }

  def withOperationId(operationId: String): this.type = {
    _internal.withOperationId(operationId)
    this
  }

  def withOperationRef(operationRef: String): this.type = {
    _internal.withOperationRef(operationRef)
    this
  }

  def withMapping(mapping: ClientList[IriTemplateMapping]): this.type = {
    _internal.withMapping(mapping.asInternal)
    this
  }

  def withRequestBody(requestBody: String): this.type = {
    _internal.withRequestBody(requestBody)
    this
  }

  def withServer(server: Server): this.type = {
    _internal.withServer(server)
    this
  }
}
