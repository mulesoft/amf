package amf.plugins.domain.apicontract.resolution.stages

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.metamodel.Field
import amf.plugins.domain.apicontract.metamodel.api.BaseApiModel
import amf.plugins.domain.apicontract.metamodel.{EndPointModel, OperationModel}
import amf.plugins.domain.apicontract.models.api.Api
import amf.plugins.domain.apicontract.models.security.SecurityRequirement

class SecurityResolutionStage() extends TransformationStep() {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        resolveSecurity(doc.encodes.asInstanceOf[Api])
      case _ =>
    }
    model
  }

  protected def resolveSecurity(api: Api): Unit = {
    val rootSecurity = getAndRemove(api, BaseApiModel.Security)

    api.endPoints.foreach { endPoint =>
      val endPointSecurity = overrideWith(rootSecurity, getAndRemove(endPoint, EndPointModel.Security))

      endPoint.operations.foreach { operation =>
        // I need to know if this is an empty array or if it's not defined.
        val opSecurity = getAndRemove(operation, OperationModel.Security)

        overrideWith(endPointSecurity, opSecurity).foreach { requirements =>
          if (requirements.nonEmpty) operation.setArray(OperationModel.Security, requirements)
        }
      }
    }
  }

  private def getAndRemove(element: DomainElement, field: Field): Option[Seq[SecurityRequirement]] = {
    val result = element.fields.entry(field).map(_.array.values.map(v => v.asInstanceOf[SecurityRequirement]))
    element.fields.removeField(field)
    result
  }

  private def overrideWith(base: Option[Seq[SecurityRequirement]],
                           overrider: Option[Seq[SecurityRequirement]]): Option[Seq[SecurityRequirement]] =
    overrider.orElse(base).filter(_.nonEmpty)
}
