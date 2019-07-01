/**
 * Copyright (C) 2019 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.properties.app.legacyImport

import java.util.UUID

import better.files.File
import cats.instances.either._
import cats.instances.option._
import cats.syntax.either._
import cats.syntax.option._
import cats.syntax.traverse._
import nl.knaw.dans.easy.properties.ApplicationErrorOr
import nl.knaw.dans.easy.properties.Command.FeedBackMessage
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentTypeValue, InputContentType }
import nl.knaw.dans.easy.properties.app.model.curation.InputCuration
import nl.knaw.dans.easy.properties.app.model.identifier.{ IdentifierType, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStepLabel, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, SpringfieldPlayMode }
import nl.knaw.dans.easy.properties.app.model.state.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, StateLabel }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DoiAction, DoiActionEvent, DoiRegisteredEvent, Timestamp }
import nl.knaw.dans.easy.properties.app.repository.DepositRepository
import nl.knaw.dans.easy.{ DataciteService, DataciteServiceException }
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.commons.lang.BooleanUtils
import org.joda.time.DateTime

class ImportProps(repository: DepositRepository, interactor: Interactor, datacite: DataciteService) {

  private var newPropertiesProvided = false

  def loadDepositProperties(file: File): ApplicationErrorOr[FeedBackMessage] = {
    newPropertiesProvided = false
    for {
      _ <- propsFileExists(file)
      _ <- propsFileReadable(file)
      _ <- propsFileWritable(file)
      properties = readDepositProperties(file)
      depositId <- getDepositId(file)
      creationTime = new DateTime(file.attributes.creationTime().toMillis)
      lastModifiedTime = new DateTime(file.attributes.lastModifiedTime().toMillis)
      _ <- repository.addDeposit(loadDeposit(depositId, creationTime, properties))
      state <- repository.setState(depositId, loadState(depositId, lastModifiedTime, properties))
      _ <- loadIngestStep(depositId, lastModifiedTime, properties, state.label).traverse(repository.setIngestStep(depositId, _))
      doi <- repository.addIdentifier(depositId, loadDoi(depositId, lastModifiedTime, properties))
      _ <- repository.addIdentifier(depositId, loadUrn(depositId, lastModifiedTime, properties))
      _ <- repository.addIdentifier(depositId, loadFedoraIdentifier(depositId, lastModifiedTime, properties))
      _ <- repository.addIdentifier(depositId, loadBagStoreIdentifier(depositId, lastModifiedTime, properties))
      _ <- repository.setDoiRegistered(depositId, loadDoiRegistered(depositId, lastModifiedTime, properties, doi.idValue))
      _ <- repository.setDoiAction(depositId, loadDoiAction(depositId, lastModifiedTime, properties))
      _ <- loadCuration(depositId, lastModifiedTime, properties).traverse(repository.setCuration(depositId, _))
      _ <- loadSpringfield(depositId, lastModifiedTime, properties).traverse(repository.setSpringfield(depositId, _))
      _ <- loadContentType(depositId, lastModifiedTime, properties).traverse(repository.setContentType(depositId, _))
      _ = savePropertiesIfChanged(properties)
    } yield s"Loading properties for deposit $depositId succeeded."
  }

  private def propsFileExists(file: File): LoadPropsErrorOr[Unit] = {
    Either.catchOnly[IllegalArgumentException] { require(file.exists) }
      .leftMap(_ => NoSuchPropertiesFileError(file))
  }

  private def propsFileReadable(file: File): LoadPropsErrorOr[Unit] = {
    Either.catchOnly[IllegalArgumentException] { require(file.isReadable) }
      .leftMap(_ => PropertiesFileNotReadableError(file))
  }

  private def propsFileWritable(file: File): LoadPropsErrorOr[Unit] = {
    Either.catchOnly[IllegalArgumentException] { require(file.isWriteable) }
      .leftMap(_ => PropertiesFileNotWritableError(file))
  }

  private def readDepositProperties(file: File): PropertiesConfiguration = {
    new PropertiesConfiguration() {
      setDelimiterParsingDisabled(true)
      setFile(file.toJava)
      load(file.toJava)
    }
  }

  private def getDepositId(file: File): LoadPropsErrorOr[DepositId] = {
    file.parentOption
      .map(_.name.asRight)
      .getOrElse { NoSuchParentDirError(file).asLeft }
      .flatMap(s => parseDepositId(s).leftMap(_ => NoDepositIdError(s)))
  }

  private def parseDepositId(s: String): Either[IllegalArgumentException, DepositId] = {
    Either.catchOnly[IllegalArgumentException](UUID.fromString(s))
  }

  private def storeProp[T](props: PropertiesConfiguration, key: String)(value: T): T = {
    props.setProperty(key, value)
    newPropertiesProvided = true
    value
  }

  private def storeProp[T](props: PropertiesConfiguration, key: String, transform: T => String)(value: T): T = {
    storeProp(props, key)(transform(value))

    value
  }

  private def loadDeposit(depositId: DepositId, creationTime: Timestamp, props: PropertiesConfiguration): Deposit = {
    val bagName = Option(props.getString("bag-store.bag-name"))
    val creationTimestamp = Option(props.getString("creation.timestamp"))
      .map(s => Either.catchOnly[IllegalArgumentException] { DateTime.parse(s) }
        .getOrElse {
          storeProp(props, "creation.timestamp") {
            interactor.ask(s => DateTime.parse(s))(s"Invalid value for creation timestamp for deposit $depositId. What value should this be?")
          }
        })
      .getOrElse(creationTime)
    val depositorId = Option(props.getString("depositor.userId"))
      .getOrElse {
        storeProp(props, "depositor.userId") {
          interactor.ask(s"Could not find the depositor for deposit $depositId. What value should this be?")
        }
      }

    Deposit(depositId, bagName, creationTimestamp, depositorId)
  }

  private def loadState(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): InputState = {
    val label = getEnumProp("state.label")(StateLabel)(props)
      .getOrElse {
        storeProp(props, "state.label") {
          interactor.ask(StateLabel)(s"Invalid state label found for deposit $depositId. What value should this be?")
        }.some
      }
      .getOrElse {
        storeProp(props, "state.label") {
          interactor.ask(StateLabel)(s"Could not find the state label for deposit $depositId. What value should this be?")
        }
      }
    val description = Option(props.getString("state.description"))
      .getOrElse {
        storeProp(props, "state.description") {
          interactor.ask(s"Could not find the state description for deposit $depositId. What value should this be?")
        }
      }

    InputState(label, description, timestamp)
  }

  private def loadIngestStep(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration, stateLabel: StateLabel): Option[InputIngestStep] = {
    getEnumProp("deposit.ingest.current-step")(IngestStepLabel)(props)
      .getOrElse {
        storeProp(props, "deposit.ingest.current-step") {
          interactor.ask(IngestStepLabel)(s"Invalid current-step label found for deposit $depositId. What value should this be?")
        }.some
      }
      .orElse {
        if (stateLabel == StateLabel.ARCHIVED)
          storeProp(props, "deposit.ingest.current-step")(IngestStepLabel.COMPLETED).some
        else none
      }
      .map(InputIngestStep(_, timestamp))
  }

  private def loadDoi(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): InputIdentifier = {
    val doi = Option(props.getString("identifier.doi"))
      .getOrElse {
        storeProp(props, "identifier.doi") {
          interactor.ask(s"Could not find DOI for deposit $depositId. What value should this be?")
        }
      }

    InputIdentifier(IdentifierType.DOI, doi, timestamp)
  }

  private def loadUrn(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): InputIdentifier = {
    val urn = Option(props.getString("identifier.urn"))
      .getOrElse {
        storeProp(props, "identifier.urn") {
          interactor.ask(s"Could not find URN for deposit $depositId. What value should this be?")
        }
      }

    InputIdentifier(IdentifierType.URN, urn, timestamp)
  }

  private def loadFedoraIdentifier(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): InputIdentifier = {
    val fedoraId = Option(props.getString("identifier.fedora"))
      .getOrElse {
        storeProp(props, "identifier.fedora") {
          interactor.ask(s"Could not find Fedora identifier for deposit $depositId. What value should this be?")
        }
      }

    InputIdentifier(IdentifierType.FEDORA, fedoraId, timestamp)
  }

  private def loadBagStoreIdentifier(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): InputIdentifier = {
    val bagId = Option(props.getString("bag-store.bag-id")).getOrElse {
      storeProp(props, "bag-store.bag-id")(depositId).toString
    }

    InputIdentifier(IdentifierType.BAG_STORE, bagId, timestamp)
  }

  private def loadDoiRegistered(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration, doi: String): DoiRegisteredEvent = {
    val registered = Option(props.getString("identifier.dans-doi.registered"))
      .flatMap(s => Option(BooleanUtils.toBoolean(s)))
      .getOrElse {
        storeProp[Boolean](props, "identifier.dans-doi.registered", (b: Boolean) => BooleanUtils.toStringYesNo(b)) {
          Either.catchOnly[DataciteServiceException] { datacite.doiExists(doi) }
            .getOrElse {
              interactor.ask(s => BooleanUtils.toBoolean(s))(s"Could not find whether doi '$doi' is registered for deposit $depositId, neither could DataCite be contacted. What value should this be?")
            }
        }
      }

    DoiRegisteredEvent(registered, timestamp)
  }

  private def loadDoiAction(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): DoiActionEvent = {
    val doiAction = getEnumProp("identifier.dans-doi.action")(DoiAction)(props)
      .getOrElse {
        storeProp(props, "identifier.dans-doi.action") {
          interactor.ask(DoiAction)(s"Invalid dans-doi action found for deposit $depositId. What value should this be?")
        }.some
      }
      .getOrElse {
        storeProp(props, "identifier.dans-doi.action")(DoiAction.CREATE) // if not set, use 'create' as default
      }

    DoiActionEvent(doiAction, timestamp)
  }

  private def loadCuration(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): Option[InputCuration] = {
    for {
      userId <- Option(props.getString("curation.datamanager.userId"))
      email <- Option(props.getString("curation.datamanager.email"))

      isNewVersionString <- Option(props.getString("curation.is-new-version"))
      isNewVersion <- Option(BooleanUtils.toBoolean(isNewVersionString))

      curationRequiredString <- Option(props.getString("curation.required"))
      curationRequired <- Option(BooleanUtils.toBoolean(curationRequiredString))

      curationPerformedString <- Option(props.getString("curation.performed"))
      curationPerformed <- Option(BooleanUtils.toBoolean(curationPerformedString))
    } yield InputCuration(isNewVersion, curationRequired, curationPerformed, userId, email, timestamp)
  }

  private def loadSpringfield(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): Option[InputSpringfield] = {
    for {
      domain <- Option(props.getString("springfield.domain"))
      user <- Option(props.getString("springfield.user"))
      collection <- Option(props.getString("springfield.collection"))
      playMode <- getEnumProp("springfield.playmode")(SpringfieldPlayMode)(props)
        .getOrElse {
          storeProp(props, "springfield.playmode") {
            interactor.ask(SpringfieldPlayMode)(s"Invalid play mode found for deposit $depositId. What value should this be?")
          }.some
        }
    } yield InputSpringfield(domain, user, collection, playMode, timestamp)
  }

  private def loadContentType(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): Option[InputContentType] = {
    getEnumProp("easy-sword2.client-message.content-type")(ContentTypeValue)(props)
      .getOrElse {
        storeProp(props, "easy-sword2.client-message.content-type") {
          interactor.ask(ContentTypeValue)(s"Invalid content type found for deposit $depositId. What value should this be?")
        }.some
      }
      .map(InputContentType(_, timestamp))
  }

  private def getEnumProp(key: String)(enum: Enumeration)(props: PropertiesConfiguration): LoadPropsErrorOr[Option[enum.Value]] = {
    Option(props.getString(key)).traverse(s => parseEnumValue(enum)(s))
  }

  private def parseEnumValue(enum: Enumeration)(s: String): LoadPropsErrorOr[enum.Value] = {
    Either.catchOnly[NoSuchElementException] { enum.withName(s) }
      .leftMap(_ => IllegalValueError(s, enum))
  }

  private def savePropertiesIfChanged(props: PropertiesConfiguration): Unit = {
    if (newPropertiesProvided)
      props.save()
  }
}
