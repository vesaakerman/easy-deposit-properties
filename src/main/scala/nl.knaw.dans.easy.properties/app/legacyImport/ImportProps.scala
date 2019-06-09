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
import nl.knaw.dans.easy.{ DataciteService, DataciteServiceException }
import nl.knaw.dans.easy.properties.ApplicationErrorOr
import nl.knaw.dans.easy.properties.Command.FeedBackMessage
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentTypeValue, InputContentType }
import nl.knaw.dans.easy.properties.app.model.curator.InputCurator
import nl.knaw.dans.easy.properties.app.model.identifier.{ IdentifierType, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStepLabel, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, SpringfieldPlayMode }
import nl.knaw.dans.easy.properties.app.model.state.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, StateLabel }
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, Deposit, DepositId, DoiAction, DoiActionEvent, DoiRegisteredEvent, IsNewVersionEvent, Timestamp }
import nl.knaw.dans.easy.properties.app.repository.{ DepositRepository, MutationErrorOr }
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.commons.lang.BooleanUtils
import org.joda.time.DateTime

class ImportProps(repository: DepositRepository, interactor: Interactor, datacite: DataciteService) {

  def loadDepositProperties(file: File): ApplicationErrorOr[FeedBackMessage] = {
    for {
      _ <- propsFileExists(file)
      properties <- readDepositProperties(file)
      depositId <- getDepositId(file)
      creationTime = new DateTime(file.attributes.creationTime().toMillis)
      lastModifiedTime = new DateTime(file.attributes.lastModifiedTime().toMillis)
      _ <- importObject(loadDeposit(depositId, creationTime, properties))(repository.addDeposit)
      state <- importObject(loadState(depositId, lastModifiedTime, properties))(repository.setState(depositId, _))
      _ <- maybeImportObject(loadIngestStep(depositId, lastModifiedTime, properties, state.label))(repository.setIngestStep(depositId, _))
      doi <- importObject(loadDoi(depositId, lastModifiedTime, properties))(repository.addIdentifier(depositId, _))
      _ <- importObject(loadUrn(depositId, lastModifiedTime, properties))(repository.addIdentifier(depositId, _))
      _ <- importObject(loadFedoraIdentifier(depositId, lastModifiedTime, properties))(repository.addIdentifier(depositId, _))
      _ <- importObject(loadBagStoreIdentifier(depositId, lastModifiedTime, properties))(repository.addIdentifier(depositId, _))
      _ <- importObject(loadDoiRegistered(depositId, lastModifiedTime, properties, doi.idValue))(repository.setDoiRegistered(depositId, _))
      _ <- importObject(loadDoiAction(depositId, lastModifiedTime, properties))(repository.setDoiAction(depositId, _))
      _ <- maybeImportObject(loadCurator(depositId, lastModifiedTime, properties))(repository.setCurator(depositId, _))
      _ <- maybeImportObject(loadIsNewVersion(depositId, lastModifiedTime, properties))(repository.setIsNewVersionAction(depositId, _))
      _ <- maybeImportObject(loadCurationRequired(depositId, lastModifiedTime, properties))(repository.setCurationRequiredAction(depositId, _))
      _ <- maybeImportObject(loadCurationPerformed(depositId, lastModifiedTime, properties))(repository.setCurationPerformedAction(depositId, _))
      _ <- maybeImportObject(loadSpringfield(depositId, lastModifiedTime, properties))(repository.setSpringfield(depositId, _))
      _ <- maybeImportObject(loadContentType(depositId, lastModifiedTime, properties))(repository.setContentType(depositId, _))
    } yield s"Loading properties for deposit $depositId succeeded."
  }

  private def propsFileExists(file: File): LoadPropsErrorOr[Unit] = {
    Either.catchOnly[IllegalArgumentException] { require(file.exists) }
      .leftMap(_ => NoSuchPropertiesFileError(file))
  }

  private def readDepositProperties(file: File): LoadPropsErrorOr[PropertiesConfiguration] = {
    new PropertiesConfiguration() {
      setDelimiterParsingDisabled(true)
      load(file.toJava)
    }.asRight
  }

  private def getDepositId(file: File): LoadPropsErrorOr[DepositId] = {
    file.parentOption
      .map(_.name.asRight)
      .getOrElse { NoSuchParentDirError(file).asLeft }
      .flatMap(s => Either.catchOnly[IllegalArgumentException](UUID.fromString(s)).leftMap(_ => NoDepositIdError(s)))
  }

  private def loadDeposit(depositId: DepositId, creationTime: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[Deposit] = {
    val bagName = Option(props.getString("bag-store.bag-name"))
    val creationTimestamp = Option(props.getString("creation.timestamp")).fold(creationTime)(DateTime.parse)
    val depositorId = Option(props.getString("depositor.userId"))
        .getOrElse {
          interactor.ask(s"Could not find the depositor for deposit $depositId. What value should this be?")
        }

    Deposit(depositId, bagName, creationTimestamp, depositorId).asRight
  }

  private def loadState(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[InputState] = {
    val label = Option(props.getString("state.label"))
      .map(parseEnumValue(StateLabel)(_).recoverWith {
        case _ => interactor.ask(StateLabel)(s"Invalid state label found for deposit $depositId. What value should this be?")
      })
      .getOrElse {
        interactor.ask(StateLabel)(s"Could not find the state label for deposit $depositId. What value should this be?")
      }
    val description = Option(props.getString("state.description"))
      .getOrElse {
        interactor.ask(s"Could not find the state description for deposit $depositId. What value should this be?")
      }

    label.map(InputState(_, description, timestamp))
  }

  private def loadIngestStep(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration, stateLabel: StateLabel): LoadPropsErrorOr[Option[InputIngestStep]] = {
    Option(props.getString("deposit.ingest.current-step"))
      .traverse(parseEnumValue(IngestStepLabel))
      .map(_.orElse {
        if (stateLabel == StateLabel.ARCHIVED)
          IngestStepLabel.COMPLETED.some
        else none
      })
      .map(_.map(InputIngestStep(_, timestamp)))
  }

  private def loadDoi(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[InputIdentifier] = {
    val doi = Option(props.getString("identifier.doi"))
      .getOrElse {
        interactor.ask(s"Could not find DOI for deposit $depositId. What value should this be?")
      }

    InputIdentifier(IdentifierType.DOI, doi, timestamp).asRight
  }

  private def loadUrn(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[InputIdentifier] = {
    val urn = Option(props.getString("identifier.urn"))
      .getOrElse {
        interactor.ask(s"Could not find URN for deposit $depositId. What value should this be?")
      }

    InputIdentifier(IdentifierType.URN, urn, timestamp).asRight
  }

  private def loadFedoraIdentifier(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[InputIdentifier] = {
    val fedoraId = Option(props.getString("identifier.fedora"))
      .getOrElse {
        interactor.ask(s"Could not find Fedora identifier for deposit $depositId. What value should this be?")
      }

    InputIdentifier(IdentifierType.FEDORA, fedoraId, timestamp).asRight
  }

  private def loadBagStoreIdentifier(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[InputIdentifier] = {
    val bagId = Option(props.getString("bag-store.bag-id")).getOrElse(depositId.toString)

    InputIdentifier(IdentifierType.BAG_STORE, bagId, timestamp).asRight
  }

  private def loadDoiRegistered(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration, doi: String): LoadPropsErrorOr[DoiRegisteredEvent] = {
    val registered = Option(props.getString("identifier.dans-doi.registered"))
      .flatMap(s => Option(BooleanUtils.toBoolean(s)))
      .getOrElse {
        Either.catchOnly[DataciteServiceException] { datacite.doiExists(doi) }
          .getOrElse {
            interactor.ask(s => BooleanUtils.toBoolean(s))(s"Could not find whether doi '$doi' is registered for deposit $depositId, neither could DataCite be contacted. What value should this be?")
          }
      }

    DoiRegisteredEvent(registered, timestamp).asRight
  }

  private def loadDoiAction(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[DoiActionEvent] = {
    Option(props.getString("identifier.dans-doi.action"))
      .map(parseEnumValue(DoiAction))
      .getOrElse(DoiAction.CREATE.asRight) // if not set, use 'create' as default
      .map(DoiActionEvent(_, timestamp))
  }

  private def loadCurator(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[Option[InputCurator]] = {
    val curator = for {
      userId <- Option(props.getString("curation.datamanager.userId"))
      email <- Option(props.getString("curation.datamanager.email"))
    } yield InputCurator(userId, email, timestamp)

    curator.asRight
  }

  private def loadIsNewVersion(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[Option[IsNewVersionEvent]] = {
    Option(props.getString("curation.is-new-version"))
      .flatMap(s => Option(BooleanUtils.toBoolean(s)))
      .traverse[LoadPropsErrorOr, IsNewVersionEvent](IsNewVersionEvent(_, timestamp).asRight)
  }

  private def loadCurationRequired(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[Option[CurationRequiredEvent]] = {
    Option(props.getString("curation.required"))
      .flatMap(s => Option(BooleanUtils.toBoolean(s)))
      .traverse[LoadPropsErrorOr, CurationRequiredEvent](CurationRequiredEvent(_, timestamp).asRight)
  }

  private def loadCurationPerformed(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[Option[CurationPerformedEvent]] = {
    Option(props.getString("curation.performed"))
      .flatMap(s => Option(BooleanUtils.toBoolean(s)))
      .traverse[LoadPropsErrorOr, CurationPerformedEvent](CurationPerformedEvent(_, timestamp).asRight)
  }

  private def loadSpringfield(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[Option[InputSpringfield]] = {
    val optPlayMode = Option(props.getString("springfield.playmode"))
      .traverse(parseEnumValue(SpringfieldPlayMode))
      .getOrElse(none)

    val result = for {
      domain <- Option(props.getString("springfield.domain"))
      user <- Option(props.getString("springfield.user"))
      collection <- Option(props.getString("springfield.collection"))
      playMode <- optPlayMode.orElse {
        interactor.ask(SpringfieldPlayMode)(s"Invalid play mode found for deposit $depositId. What value should this be?")
          .fold(_ => none, _.some)
      }
    } yield InputSpringfield(domain, user, collection, playMode, timestamp)

    result.asRight
  }

  private def loadContentType(depositId: DepositId, timestamp: Timestamp, props: PropertiesConfiguration): LoadPropsErrorOr[Option[InputContentType]] = {
    Option(props.getString("easy-sword2.client-message.content-type"))
      .traverse[LoadPropsErrorOr, ContentTypeValue.Value](parseEnumValue(ContentTypeValue)(_).recoverWith {
        case _ => interactor.ask(ContentTypeValue)(s"Invalid content type found for deposit $depositId. What value should this be?")
      })
      .map(_.map(InputContentType(_, timestamp)))
  }

  private def importObject[T, S](obj: => LoadPropsErrorOr[T])(add: T => MutationErrorOr[S]): ApplicationErrorOr[S] = {
    obj.flatMap(add)
  }

  private def maybeImportObject[T, S](obj: => LoadPropsErrorOr[Option[T]])(add: T => MutationErrorOr[S]): ApplicationErrorOr[Option[S]] = {
    obj.flatMap(_.map(add(_).map(Option(_))).getOrElse(none.asRight))
  }

  private def parseEnumValue(enum: Enumeration)(s: String): LoadPropsErrorOr[enum.Value] = {
    Either.catchOnly[NoSuchElementException] { enum.withName(s) }
      .leftMap(_ => IllegalValueError(s, enum))
  }
}
