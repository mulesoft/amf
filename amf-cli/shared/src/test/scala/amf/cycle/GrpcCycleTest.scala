package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.internal.remote.GrpcProtoHint
import amf.grpc.client.scala.GRPCConfiguration
import amf.io.FunSuiteCycleTests

class GrpcCycleTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/grpc/"

  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    GRPCConfiguration
      .GRPC()
      .withRenderOptions(options.getOrElse(renderOptions()))
      .withErrorHandlerProvider(() => eh.getOrElse(IgnoringErrorHandler))
  }

  test("Can cycle through a simple gRPC API") {
    cycle("simple/api.proto", "simple/dumped.proto", GrpcProtoHint, GrpcProtoHint)
  }
}
