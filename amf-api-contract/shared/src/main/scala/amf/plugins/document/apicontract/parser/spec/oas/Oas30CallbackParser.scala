package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.validation.CoreValidations
import amf.plugins.document.apicontract.annotations.ExternalReferenceUrl
import amf.shapes.internal.spec.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.apicontract.parser.spec.OasDefinitions
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.ErrorCallback
import amf.plugins.domain.apicontract.metamodel.{CallbackModel, EndPointModel}
import amf.plugins.domain.apicontract.models.Callback
import org.yaml.model.{YMap, YMapEntry, YScalar}

/**
  * A single named callback may be parsed into multiple Callback when multiple expressions are defined.
  * This is due to inconsistency in the model, pending refactor in APIMF-1771
  */
case class Oas30CallbackParser(map: YMap, adopt: Callback => Unit, name: String, rootEntry: YMapEntry)(
    implicit val ctx: OasWebApiContext) {
  def parse(): List[Callback] = {
    ctx.link(map) match {
      case Left(fullRef) =>
        val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "callbacks")
        ctx.declarations
          .findCallbackInDeclarations(label)
          .map { callbacks =>
            callbacks.map { callback =>
              val linkCallback: Callback = callback.link(AmfScalar(label), Annotations(map), Annotations.synthesized())
              adopt(linkCallback)
              linkCallback
            }
          }
          .getOrElse {
            ctx.navigateToRemoteYNode(fullRef) match {
              case Some(navigation) =>
                Oas30CallbackParser(navigation.remoteNode.as[YMap], adopt, name, rootEntry)(navigation.context)
                  .parse()
                  .map(_.add(ExternalReferenceUrl(fullRef)))
              case None =>
                ctx.eh.violation(CoreValidations.UnresolvedReference,
                                 "",
                                 s"Cannot find callback reference $fullRef",
                                 map)
                val callback: Callback = new ErrorCallback(label, map).link(name, Annotations(rootEntry))

                adopt(callback)
                List(callback)
            }
          }
      case Right(_) =>
        val callbackEntries = map.entries
        callbackEntries.map { entry =>
          val expression = entry.key.as[YScalar].text
          val callback   = Callback().add(Annotations(entry))
          callback.fields.setWithoutId(CallbackModel.Expression,
                                       AmfScalar(expression, Annotations(entry.key)),
                                       Annotations.inferred())
          adopt(callback)
          val collected = ctx.factory.endPointParser(entry, callback.id, List()).parse()
          collected.foreach(e => {
            e.set(EndPointModel.Path, AmfScalar(s"/$expression", Annotations(entry.key)), Annotations.inferred())
            callback.set(CallbackModel.Endpoint, e, Annotations.inferred())
          }) // rename path to avoid endpoint validations

          callback
        }.toList

    }

  }
}
