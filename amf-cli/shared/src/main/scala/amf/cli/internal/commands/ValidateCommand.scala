package amf.cli.internal.commands

import amf.apicontract.client.scala.config.AMFConfiguration
import amf.client.remod.parsing.AMLDialectInstanceParsingPlugin
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.Platform
import amf.plugins.document.vocabularies.custom.ParsedValidationProfile
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance}
import amf.plugins.document.vocabularies.model.domain.DialectDomainElement
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ValidateCommand(override val platform: Platform) extends CommandHelper {

  def run(parserConfig: ParserConfig, configuration: AMFConfiguration): Future[Any] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val res = for {
      newConfig <- processDialects(parserConfig, configuration)
      model     <- parseInput(parserConfig, newConfig)
      report    <- report(model, parserConfig, newConfig)
    } yield {
      processOutput(report, parserConfig)
    }

    res.onComplete {
      case Failure(ex) =>
        parserConfig.stderr.print(ex)
        parserConfig.proc.exit(ExitCodes.Exception)
      case Success(other) =>
        other
    }

    res
  }

  // TODO ARM: move to registry? or to context parsing? discuss with tomi
  def findDialect(configuration: AMFConfiguration, id: String): Option[Dialect] = {
    configuration.registry.plugins.parsePlugins.collectFirst({
      case aml: AMLDialectInstanceParsingPlugin if aml.dialect.id == id => aml.dialect
    })
  }

  def report(model: BaseUnit, config: ParserConfig, configuration: AMFConfiguration): Future[AMFValidationReport] = {
    implicit val executionContext: ExecutionContext = configuration.getExecutionContext
    val customProfileLoaded: Future[(ProfileName, AMFConfiguration)] = if (config.customProfile.isDefined) {
      for {
        confCustom    <- configuration.withCustomValidationsEnabled
        customProfile <- confCustom.createClient().parseDialectInstance(config.customProfile.get)
      } yield {
        val profile = ParsedValidationProfile(customProfile.dialectInstance.encodes.asInstanceOf[DialectDomainElement])
        (profile.name, confCustom.withValidationProfile(profile))
      }
    } else {
      Future {
        model match {
          case dialectInstance: DialectInstance =>
            findDialect(configuration, dialectInstance.definedBy().value()) match {
              case Some(dialect) =>
                (ProfileName(dialect.nameAndVersion()), configuration)
              case _ =>
                (config.profile, configuration)
            }
          case _ =>
            (config.profile, configuration)
        }
      }
    }
    customProfileLoaded flatMap {
      case (profileName, conf) =>
        conf.createClient().validate(model, profileName)
    }
  }

  def processOutput(report: AMFValidationReport, config: ParserConfig)(implicit ec: ExecutionContext): Unit = {
    val json = ValidationReportJSONLDEmitter.emitJSON(report)
    config.output match {
      case Some(f) => platform.write(f, json)
      case None    => config.stdout.print(json)
    }
    if (!report.conforms) {
      config.proc.exit(ExitCodes.FailingValidation)
    }
  }

}

object ValidateCommand {
  def apply(platform: Platform) = new ValidateCommand(platform)
}
