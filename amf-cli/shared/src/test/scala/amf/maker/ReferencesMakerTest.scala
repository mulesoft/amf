package amf.maker

import amf.apicontract.client.scala.WebAPIConfiguration
import amf.apicontract.client.scala.model.document.{APIContractProcessingData, DataTypeFragment}
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.common.AmfObjectTestMatcher
import amf.compiler.CompilerTestBuilder
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.document.{Document, Fragment}
import amf.core.client.scala.model.domain.AmfObject
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.metamodel.document.BaseUnitModel
import amf.core.internal.remote._
import amf.shapes.client.scala.model.domain.DomainExtensions.propertyShapeToPropertyShape
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.{Assertion, Succeeded}

import scala.concurrent.Future
/** */
class ReferencesMakerTest
    extends AsyncFunSuiteWithPlatformGlobalExecutionContext
    with CompilerTestBuilder
    with AmfObjectTestMatcher {

  test("Data type fragment test raml") {
    val rootDocument = "file://amf-cli/shared/src/test/resources/references/data-type-fragment.reference.raml"
    assertFixture(rootDocument, Raml10YamlHint)
  }

  test("Data type fragment test oas") {
    val rootDocument = "file://amf-cli/shared/src/test/resources/references/data-type-fragment.json"
    assertFixture(rootDocument, Oas20JsonHint)
  }

  private def assertFixture(rootFile: String, hint: Hint): Future[Assertion] = {

    val rootExpected = UnitsCreator(hint.spec).usesDataType
    val amfConfig    = WebAPIConfiguration.WebAPI()
    build(rootFile, hint, amfConfig, None)
      .map({ case actual: Document =>
        actual
      })
      .map({ actual =>
        AmfObjectMatcher(withoutLocationOrSourceInfo(rootExpected)).assert(withoutLocationOrSourceInfo(actual))
        actual.references.zipWithIndex foreach { case (actualRef, index) =>
          AmfObjectMatcher(withoutLocationOrSourceInfo(rootExpected.references(index)))
            .assert(withoutLocationOrSourceInfo(actualRef))
        }
        Succeeded
      })
  }

  def withoutLocationOrSourceInfo(e: AmfObject): AmfObject = {
    e.fields.removeField(amf.core.internal.metamodel.document.DocumentModel.Location)
    e.fields.removeField(BaseUnitModel.SourceInformation)
    e
  }

  case class UnitsCreator(spec: Spec) {

    val (file, fragmentFile, minCount, recursive) = spec match {
      case Raml10 => ("data-type-fragment.reference.raml", "person.raml", 1, false)
      case _      => ("data-type-fragment.json", "person.json", 1, true)
    }

    private val person: NodeShape = {
      val shape = NodeShape().withName("type").withClosed(false)
      shape
        .withProperty("name")
        .withPath("http://a.ml/vocabularies/data#name")
        .withMinCount(minCount)
        .withScalarSchema("name")
        .withDataType("http://www.w3.org/2001/XMLSchema#string")
      shape
    }

    private val dataTypeFragment: Fragment = {
      DataTypeFragment()
        .withLocation("file://amf-cli/shared/src/test/resources/references/fragments/" + fragmentFile)
        .withEncodes(person)
        .withRoot(false)
        .withProcessingData(APIContractProcessingData().withSourceSpec(spec))
    }

    val usesDataType: Document = {

      val personLink = person.link("fragments/" + fragmentFile).asInstanceOf[NodeShape].withName("person")
      if (recursive) personLink.withSupportsRecursion(true)
      val api = WebApi()
        .withName("API")
        .withVersion("1.0")
      if (spec == Oas20 || spec == Oas30) api.withEndPoints(Nil)
      val result = Document()
        .withLocation("amf-cli/shared/src/test/resources/references/" + file)
        .withEncodes(api)
        .withReferences(Seq(dataTypeFragment))
        .withDeclares(Seq(personLink))
        .withRoot(true)
        .withProcessingData(APIContractProcessingData().withSourceSpec(spec))
      AMFGraphConfiguration
        .predefined()
        .baseUnitClient()
        .setBaseUri(result, "file://amf-cli/shared/src/test/resources/references/" + file)
      result
    }
  }
}
