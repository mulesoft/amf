package amf.apicontract.client.platform

import amf.aml.client.platform.BaseAMLBaseUnitClient
import amf.apicontract.client.scala.{AMFBaseUnitClient => InternalAMFBaseUnitClient}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.AMFResult
import amf.core.client.platform.model.document.BaseUnit

import scala.scalajs.js.annotation.JSExportAll

/**
  * The AMF Client contains common AMF operations.
  * For more complex uses see [[AMFParser]] or [[AMFRenderer]]
  */
@JSExportAll
class AMFBaseUnitClient private[amf] (private val _internal: InternalAMFBaseUnitClient)
    extends BaseAMLBaseUnitClient(_internal) {

  private[amf] def this(configuration: AMFConfiguration) = {
    this(new InternalAMFBaseUnitClient(configuration))
  }

  override def getConfiguration(): AMFConfiguration = _internal.getConfiguration

  /**
    * parse a [[amf.client.model.document.Document]]
    * @param url of the resource to parse
    * @return a Future [[AMFDocumentResult]]
    */
  def parseDocument(url: String): ClientFuture[AMFDocumentResult] = _internal.parseDocument(url).asClient

  /**
    * parse a [[amf.client.model.document.Module]]
    * @param url of the resource to parse
    * @return a Future [[AMFLibraryResult]]
    */
  def parseLibrary(url: String): ClientFuture[AMFLibraryResult] = _internal.parseLibrary(url).asClient

  /**
    * Transforms a [[BaseUnit]] with using pipeline with default id.
    * @param baseUnit [[BaseUnit]] to transform
    * @param targetMediaType Provide a specification for obtaining the correct pipeline.
    *                        Must be <code>"application/spec"</code> or <code>"application/spec+syntax"</code>.
    *                        Examples: <code>"application/raml10"</code> or <code>"application/raml10+yaml"</code>
    * @return An [[AMFResult]] with the transformed BaseUnit and it's validation results
    */
  def transformDefault(baseUnit: BaseUnit, targetMediaType: String): AMFResult =
    _internal.transformDefault(baseUnit, targetMediaType)

  /**
    * Transforms a [[BaseUnit]] with using pipeline with editing id.
    * @param baseUnit [[BaseUnit]] to transform
    * @param targetMediaType Provide a specification for obtaining the correct pipeline.
    *                        Must be <code>"application/spec"</code> or <code>"application/spec+syntax"</code>.
    *                        Examples: <code>"application/raml10"</code> or <code>"application/raml10+yaml"</code>
    * @return An [[AMFResult]] with the transformed BaseUnit and it's validation results
    */
  def transformEditing(baseUnit: BaseUnit, targetMediaType: String): AMFResult =
    _internal.transformEditing(baseUnit, targetMediaType)

  /**
    * Transforms a [[BaseUnit]] with using pipeline with compatibility id.
    * @param baseUnit [[BaseUnit]] to transform
    * @param targetMediaType Provide a specification for obtaining the correct pipeline.
    *                        Must be <code>"application/spec"</code> or <code>"application/spec+syntax"</code>.
    *                        Examples: <code>"application/raml10"</code> or <code>"application/raml10+yaml"</code>
    * @return An [[AMFResult]] with the transformed BaseUnit and it's validation results
    */
  def transformCompatibility(baseUnit: BaseUnit, targetMediaType: String): AMFResult =
    _internal.transformCompatibility(baseUnit, targetMediaType)

  /**
    * Transforms a [[BaseUnit]] with using pipeline with cache id.
    * @param baseUnit [[BaseUnit]] to transform
    * @param targetMediaType Provide a specification for obtaining the correct pipeline.
    *                        Must be <code>"application/spec"</code> or <code>"application/spec+syntax"</code>.
    *                        Examples: <code>"application/raml10"</code> or <code>"application/raml10+yaml"</code>
    * @return An [[AMFResult]] with the transformed BaseUnit and it's validation results
    */
  def transformCache(baseUnit: BaseUnit, targetMediaType: String): AMFResult =
    _internal.transformCache(baseUnit, targetMediaType)
}
