package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.compatibility.raml._
import amf.plugins.domain.webapi.resolution.stages.{
  AnnotationRemovalStage,
  MediaTypeResolutionStage,
  RamlCompatiblePayloadAndParameterResolutionStage
}
import amf.{ProfileName, RamlProfile}

class RamlCompatibilityPipeline() extends ResolutionPipeline() {

  override def steps(model: BaseUnit, sourceVendor: String)(
      implicit errorHandler: ErrorHandler): Seq[ResolutionStage] =
    Seq(
      new MandatoryDocumentationTitle(),
      new MandatoryAnnotationType(),
      new MediaTypeResolutionStage(RamlProfile, keepEditingInfo = true),
      new DefaultPayloadMediaType(),
      new MandatoryCreativeWorkFields(),
      new DefaultToNumericDefaultResponse(),
      new MakeExamplesOptional(),
      new CapitalizeSchemes(),
      new SecuritySettingsMapper(),
      new ShapeFormatAdjuster(),
      new CustomAnnotationDeclaration(),
      new PushSingleOperationPathParams(),
      new UnionsAsTypeExpressions(),
      new EscapeTypeNames(),
      new MakeRequiredFieldImplicitForOptionalProperties(),
      new ResolveRamlCompatibleDeclarations(),
      new ResolveLinksWithNonDeclaredTargets(),
      new RamlCompatiblePayloadAndParameterResolutionStage(RamlProfile),
      new SanitizeCustomTypeNames(),
      new RecursionDetection(),
      new AnnotationRemovalStage()
    )

}
