package amf.apicontract.internal.spec.common.transformation.stage

import amf.apicontract.client.scala.model.domain.api.{Api, AsyncApi, WebApi}
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Server, ServerContainer}
import amf.apicontract.internal.metamodel.domain.api.{AsyncApiModel, WebApiModel}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, OperationModel}
import amf.core.client.common.validation.{Oas30Profile, Oas31Profile, ProfileName}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.TransformationStep

/** Place server models in the right locations according to OAS 3.0 and our own criteria for AMF
  *
  * @param profile
  *   target profile
  */
class ServersNormalizationStage(profile: ProfileName, val keepEditingInfo: Boolean = false)
    extends TransformationStep() {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    profile match {
      // TODO should run for Amf too
      case Oas30Profile | Oas31Profile => normalizeServers(model)
      case _                           => model
    }
  }

  /** Push all server definitions to the operation level.
    *
    * @param unit
    *   BaseUnit in
    * @return
    *   unit BaseUnit out
    */
  private def normalizeServers(unit: BaseUnit): BaseUnit = {
    unit match {
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        val api       = doc.encodes.asInstanceOf[Api]
        val endpoints = api.endPoints
        propagateServers(api, endpoints)
        endpoints.foreach { endPoint =>
          propagateServers(endPoint, endPoint.operations)
        }
        doc
      case _ => unit
    }
  }

  /** moves servers defined in base to each child that has no servers defined.
    */
  private def propagateServers(base: ServerContainer, children: Seq[ServerContainer]): Unit =
    if (children.nonEmpty && base.servers.nonEmpty) {
      val servers: Seq[Server] = base.servers
      if (!keepEditingInfo) base.removeServers()
      children.foreach { child =>
        if (child.servers.isEmpty)
          child match {
            case api: AsyncApi        => api.setArrayWithoutId(AsyncApiModel.Servers, servers)
            case api: WebApi          => api.setArrayWithoutId(WebApiModel.Servers, servers)
            case endpoint: EndPoint   => endpoint.setArrayWithoutId(EndPointModel.Servers, servers)
            case operation: Operation => operation.setArrayWithoutId(OperationModel.Servers, servers)
            case _                    => // ignore
          }
      }
    }

}
