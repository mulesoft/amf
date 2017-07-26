package amf.emit

import amf.builder._
import amf.common.AMFAST
import amf.document.Document
import amf.domain.WebApi
import amf.remote.Vendor
import amf.unsafe.PlatformSecrets

/**
  *
  */
trait AMFUnitFixtureTest extends PlatformSecrets {

  def buildExtendedUnit(vendor: Vendor): AMFAST = {
    val builder = apiComplete().toBuilder
    val documentation = new CreativeWorkBuilder()
      .withDescription("documentation operation")
      .withUrl("localhost:8080/endpoint/operation")
      .build

    val operationGet = new OperationBuilder()
      .withDescription("test operation get")
      .withDocumentation(documentation)
      .withMethod("get")
      .withName("test get")
      .withSchemes(List("http"))
      .withSummary("summary of operation get")
      .build

    val operationPost = new OperationBuilder()
      .withDescription("test operation post")
      .withDocumentation(documentation)
      .withMethod("post")
      .withName("test post")
      .withSchemes(List("http"))
      .withSummary("summary of operation post")
      .build

    val endpoint = new EndPointBuilder()
      .withDescription("test endpoint")
      .withName("endpoint")
      .withPath("/endpoint")
      .withOperations(List(operationGet, operationPost))
      .build

    val api = builder
      .withEndPoints(List(endpoint))
      .build

    AMFUnitMaker(doc(api), vendor)
  }

  def buildSimpleUnit(vendor: Vendor): AMFAST = AMFUnitMaker(doc(api()), vendor)

  def buildCompleteUnit(vendor: Vendor): AMFAST = AMFUnitMaker(doc(apiComplete()), vendor)

  def doc(api: WebApi): Document = DocumentBuilder().withEncodes(api).build

  def api(): WebApi = {
    WebApiBuilder()
      .withName("test")
      .withDescription("test description")
      .withHost("http://localhost.com/api")
      .withSchemes(List("http", "https"))
      .withBasePath("http://localhost.com/api")
      .withAccepts("application/json")
      .withContentType("application/json")
      .withVersion("1.1")
      .withTermsOfService("termsOfService")
      .build
  }

  def apiComplete(): WebApi = {
    val builder = api().toBuilder
    builder
      .withProvider(
        OrganizationBuilder()
          .withEmail("test@test")
          .withName("organizationName")
          .withUrl("organizationUrl")
          .build
      )
      .withLicense(
        LicenseBuilder()
          .withName("licenseName")
          .withUrl("licenseUrl")
          .build
      )
      .withDocumentation(
        CreativeWorkBuilder()
          .withUrl("creativoWorkUrl")
          .withDescription("creativeWorkDescription")
          .build
      )
      .build

  }
}
