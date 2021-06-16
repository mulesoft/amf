package amf.apicontract.internal.validation.shacl

import amf.apicontract.internal.validation.plugin.BaseApiValidationPlugin.standardApiProfiles
import amf.core.internal.plugins.validation.ValidationInfo
import amf.core.internal.validation.core.ShaclValidationOptions
import amf.plugins.features.validation.PlatformValidator
import amf.plugins.features.validation.shacl.custom.CustomShaclValidator.CustomShaclFunctions
import amf.plugins.features.validation.shacl.{FullShaclValidator, ShaclValidator}

object FullShaclModelValidationPlugin {

  protected val id: String                      = this.getClass.getSimpleName
  protected val functions: CustomShaclFunctions = CustomShaclFunctions.functions

  def apply() = new FullShaclModelValidationPlugin()
}

class FullShaclModelValidationPlugin extends ShaclValidationPlugin {

  override val id: String = FullShaclModelValidationPlugin.id

  override def applies(element: ValidationInfo): Boolean =
    super.applies(element) && !standardApiProfiles.contains(element.profile)

  override protected def validator(options: ShaclValidationOptions): ShaclValidator =
    new FullShaclValidator(PlatformValidator.instance(), options)
}
