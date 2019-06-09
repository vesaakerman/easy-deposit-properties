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
package nl.knaw.dans.easy.properties

import java.net.URL
import java.util.{ TimeZone, UUID }

import better.files.File
import cats.scalatest.{ EitherMatchers, EitherValues }
import cats.syntax.either._
import cats.syntax.option._
import nl.knaw.dans.easy.properties.app.legacyImport.{ ImportProps, Interactor, NoDepositIdError, NoSuchPropertiesFileError }
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, ContentTypeValue, InputContentType }
import nl.knaw.dans.easy.properties.app.model.curator.{ Curator, InputCurator }
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStep, IngestStepLabel, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield, SpringfieldPlayMode }
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State, StateLabel }
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, Deposit, DepositId, DoiAction, DoiActionEvent, DoiRegisteredEvent, IsNewVersionEvent }
import nl.knaw.dans.easy.properties.app.repository.DepositRepository
import nl.knaw.dans.easy.properties.fixture.{ FileSystemSupport, TestSupportFixture }
import nl.knaw.dans.easy.{ DataciteService, DataciteServiceConfiguration, DataciteServiceException }
import org.apache.commons.configuration.PropertiesConfiguration
import org.joda.time.{ DateTime, DateTimeZone }
import org.scalamock.scalatest.MockFactory

class ImportPropsSpec extends TestSupportFixture
  with FileSystemSupport
  with MockFactory
  with EitherMatchers with EitherValues {

  private val dataciteConfig = {
    val config = new DataciteServiceConfiguration()
    config.setDatasetResolver(new URL("http://does.not.exist.dans.knaw.nl"))
    config
  }
  private class MockDataciteService extends DataciteService(dataciteConfig)

  private val repo = mock[DepositRepository]
  private val interactor = mock[Interactor]
  private val datacite = mock[MockDataciteService]
  private val timeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"))
  private val time = new DateTime(2019, 1, 1, 0, 0, timeZone)
  private val importProps = new ImportProps(repo, interactor, datacite)

  "loadDepositProperties" should "read the given deposit.properties file and call the repository on it" in {
    val file = writeProps()(defaultProps())
    val depositId = UUID.fromString(file.parent.name)
    val lastModified = new DateTime(file.attributes.lastModifiedTime().toMillis)

    inSequence {
      repo.addDeposit _ expects where(isDeposit(Deposit(depositId, "bag".some, time, "user001"))) returning Deposit(depositId, "bag".some, time, "user001").asRight
      repo.setState _ expects(depositId, InputState(StateLabel.SUBMITTED, "my description", lastModified)) returning State("my-id", StateLabel.SUBMITTED, "my description", lastModified).asRight
      repo.setIngestStep _ expects(depositId, InputIngestStep(IngestStepLabel.BAGSTORE, lastModified)) returning IngestStep("my-id", IngestStepLabel.BAGSTORE, lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.DOI, "my-doi-value", lastModified)) returning Identifier("my-id", IdentifierType.DOI, "my-doi-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.URN, "my-urn-value", lastModified)) returning Identifier("my-id", IdentifierType.URN, "my-urn-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.FEDORA, "my-fedora-value", lastModified)) returning Identifier("my-id", IdentifierType.FEDORA, "my-fedora-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.BAG_STORE, "my-bag-store-value", lastModified)) returning Identifier("my-id", IdentifierType.BAG_STORE, "my-bag-store-value", lastModified).asRight
      repo.setDoiRegistered _ expects(depositId, DoiRegisteredEvent(value = true, lastModified)) returning DoiRegisteredEvent(value = true, lastModified).asRight
      repo.setDoiAction _ expects(depositId, DoiActionEvent(DoiAction.UPDATE, lastModified)) returning DoiActionEvent(DoiAction.UPDATE, lastModified).asRight
      repo.setCurator _ expects(depositId, InputCurator("archie001", "does.not.exists@dans.knaw.nl", lastModified)) returning Curator("my-id", "archie001", "does.not.exists@dans.knaw.nl", lastModified).asRight
      repo.setIsNewVersionAction _ expects(depositId, IsNewVersionEvent(isNewVersion = true, lastModified)) returning IsNewVersionEvent(isNewVersion = true, lastModified).asRight
      repo.setCurationRequiredAction _ expects(depositId, CurationRequiredEvent(curationRequired = false, lastModified)) returning CurationRequiredEvent(curationRequired = false, lastModified).asRight
      repo.setCurationPerformedAction _ expects(depositId, CurationPerformedEvent(curationPerformed = false, lastModified)) returning CurationPerformedEvent(curationPerformed = false, lastModified).asRight
      repo.setSpringfield _ expects(depositId, InputSpringfield("domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified)) returning Springfield("my-id", "domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified).asRight
      repo.setContentType _ expects(depositId, InputContentType(ContentTypeValue.ZIP, lastModified)) returning ContentType("my-id", ContentTypeValue.ZIP, lastModified).asRight
    }

    importProps.loadDepositProperties(file) shouldBe right
  }

  it should "interact with the user when necessary values don't exist" in {
    val file = writeProps()(defaultProps(cfg => {
      // deposit
      cfg.clearProperty("bag-store.bag-name") // no bag-store.bag-name, not set
      cfg.clearProperty("creation.timestamp") // no creation.timestamp, defaults to creationTime
      cfg.clearProperty("depositor.userId") // no depositor.userId, asked via interactor

      // state
      cfg.clearProperty("state.label") // no state.label, asked via interactor
      cfg.clearProperty("state.description") // no state.description, asked via interactor

      // ingest step
      cfg.clearProperty("deposit.ingest.current-step") // no deposit.ingest.current-step, not set

      // identifiers
      cfg.clearProperty("identifier.doi") // no identifier.doi, asked via interactor
      cfg.clearProperty("identifier.urn") // no identifier.urn, asked via interactor
      cfg.clearProperty("identifier.fedora") // no identifier.fedora, asked via interactor
      cfg.clearProperty("bag-store.bag-id") // no bag-store.bag-id, defaults to depositId

      // doi events
      cfg.setProperty("identifier.dans-doi.registered", "yes")
      cfg.clearProperty("identifier.dans-doi.action") // no identifier.dans-doi.action, defaults to CREATE

      // curator
      cfg.clearProperty("curation.datamanager.userId") // no curation.datamanager.userId, not set
      cfg.clearProperty("curation.datamanager.email") // no curation.datamanager.email, not set

      // curation events
      cfg.clearProperty("curation.is-new-version") // no curation.is-new-version, not set
      cfg.clearProperty("curation.required") // no curation.required, not set
      cfg.clearProperty("curation.performed") // no curation.performed, not set

      // springfield
      cfg.clearProperty("springfield.domain") // no springfield.domain, not set
      cfg.clearProperty("springfield.user") // no springfield.user, not set
      cfg.clearProperty("springfield.collection") // no springfield.collection, not set
      cfg.clearProperty("springfield.playmode") // no springfield.playmode, not set

      // content-type
      cfg.clearProperty("easy-sword2.client-message.content-type") // no easy-sword2.client-message.content-type, not set
    }))
    val depositId = UUID.fromString(file.parent.name)
    val creationTime = new DateTime(file.attributes.creationTime().toMillis)
    val lastModified = new DateTime(file.attributes.lastModifiedTime().toMillis)

    inSequence {
      (interactor.ask(_: String)) expects * returning "user001"
      repo.addDeposit _ expects where(isDeposit(Deposit(depositId, none, creationTime, "user001"))) returning Deposit(depositId, none, creationTime, "user001").asRight
      (interactor.ask(_: Enumeration)(_: String)) expects(StateLabel, *) returning StateLabel.SUBMITTED.asRight
      (interactor.ask(_: String)) expects * returning "my description"
      repo.setState _ expects(depositId, InputState(StateLabel.SUBMITTED, "my description", lastModified)) returning State("my-id", StateLabel.SUBMITTED, "my-description", lastModified).asRight
      // no repo.setIngestStep
      (interactor.ask(_: String)) expects * returning "my-doi-value"
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.DOI, "my-doi-value", lastModified)) returning Identifier("my-id", IdentifierType.DOI, "my-doi-value", lastModified).asRight
      (interactor.ask(_: String)) expects * returning "my-urn-value"
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.URN, "my-urn-value", lastModified)) returning Identifier("my-id", IdentifierType.URN, "my-urn-value", lastModified).asRight
      (interactor.ask(_: String)) expects * returning "my-fedora-value"
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.FEDORA, "my-fedora-value", lastModified)) returning Identifier("my-id", IdentifierType.FEDORA, "my-fedora-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.BAG_STORE, depositId.toString, lastModified)) returning Identifier("my-id", IdentifierType.BAG_STORE, depositId.toString, lastModified).asRight
      repo.setDoiRegistered _ expects(depositId, DoiRegisteredEvent(value = true, lastModified)) returning DoiRegisteredEvent(value = true, lastModified).asRight
      repo.setDoiAction _ expects(depositId, DoiActionEvent(DoiAction.CREATE, lastModified)) returning DoiActionEvent(DoiAction.CREATE, lastModified).asRight
      // no repo.setCurator
      // no repo.setIsNewVersionAction
      // no repo.setCurationRequiredAction
      // no repo.setCurationPerformedAction
      // no repo.setSpringfield
      // no repo.setContentType
    }

    importProps.loadDepositProperties(file) shouldBe right
  }

  it should "interact with the user when the state label is invalid" in {
    val time = new DateTime(2019, 1, 1, 0, 0, timeZone)
    val file = writeProps()(defaultProps(cfg => {
      // state
      cfg.setProperty("state.label", "INVALID_VALUE") // invalid, asked via interactor
    }))
    val depositId = UUID.fromString(file.parent.name)
    val lastModified = new DateTime(file.attributes.lastModifiedTime().toMillis)

    inSequence {
      repo.addDeposit _ expects where(isDeposit(Deposit(depositId, "bag".some, time, "user001"))) returning Deposit(depositId, "bag".some, time, "user001").asRight
      (interactor.ask(_: Enumeration)(_: String)) expects(StateLabel, *) returning StateLabel.SUBMITTED.asRight
      repo.setState _ expects(depositId, InputState(StateLabel.SUBMITTED, "my description", lastModified)) returning State("my-id", StateLabel.SUBMITTED, "my description", lastModified).asRight
      repo.setIngestStep _ expects(depositId, InputIngestStep(IngestStepLabel.BAGSTORE, lastModified)) returning IngestStep("my-id", IngestStepLabel.BAGSTORE, lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.DOI, "my-doi-value", lastModified)) returning Identifier("my-id", IdentifierType.DOI, "my-doi-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.URN, "my-urn-value", lastModified)) returning Identifier("my-id", IdentifierType.URN, "my-urn-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.FEDORA, "my-fedora-value", lastModified)) returning Identifier("my-id", IdentifierType.FEDORA, "my-fedora-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.BAG_STORE, "my-bag-store-value", lastModified)) returning Identifier("my-id", IdentifierType.BAG_STORE, "my-bag-store-value", lastModified).asRight
      repo.setDoiRegistered _ expects(depositId, DoiRegisteredEvent(value = true, lastModified)) returning DoiRegisteredEvent(value = true, lastModified).asRight
      repo.setDoiAction _ expects(depositId, DoiActionEvent(DoiAction.UPDATE, lastModified)) returning DoiActionEvent(DoiAction.UPDATE, lastModified).asRight
      repo.setCurator _ expects(depositId, InputCurator("archie001", "does.not.exists@dans.knaw.nl", lastModified)) returning Curator("my-id", "archie001", "does.not.exists@dans.knaw.nl", lastModified).asRight
      repo.setIsNewVersionAction _ expects(depositId, IsNewVersionEvent(isNewVersion = true, lastModified)) returning IsNewVersionEvent(isNewVersion = true, lastModified).asRight
      repo.setCurationRequiredAction _ expects(depositId, CurationRequiredEvent(curationRequired = false, lastModified)) returning CurationRequiredEvent(curationRequired = false, lastModified).asRight
      repo.setCurationPerformedAction _ expects(depositId, CurationPerformedEvent(curationPerformed = false, lastModified)) returning CurationPerformedEvent(curationPerformed = false, lastModified).asRight
      repo.setSpringfield _ expects(depositId, InputSpringfield("domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified)) returning Springfield("my-id", "domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified).asRight
      repo.setContentType _ expects(depositId, InputContentType(ContentTypeValue.ZIP, lastModified)) returning ContentType("my-id", ContentTypeValue.ZIP, lastModified).asRight
    }

    importProps.loadDepositProperties(file) shouldBe right
  }

  it should "choose ingest step COMPLETED when it is not provided and state = ARCHIVED" in {
    val time = new DateTime(2019, 1, 1, 0, 0, timeZone)
    val file = writeProps()(defaultProps(cfg => {
      // state
      cfg.setProperty("state.label", "ARCHIVED")

      // ingest step
      cfg.clearProperty("deposit.ingest.current-step") // no deposit.ingest.current-step, COMPLETED due to state.label = ARCHIVED
    }))
    val depositId = UUID.fromString(file.parent.name)
    val lastModified = new DateTime(file.attributes.lastModifiedTime().toMillis)

    inSequence {
      repo.addDeposit _ expects where(isDeposit(Deposit(depositId, "bag".some, time, "user001"))) returning Deposit(depositId, "bag".some, time, "user001").asRight
      repo.setState _ expects(depositId, InputState(StateLabel.ARCHIVED, "my description", lastModified)) returning State("my-id", StateLabel.ARCHIVED, "my description", lastModified).asRight
      repo.setIngestStep _ expects(depositId, InputIngestStep(IngestStepLabel.COMPLETED, lastModified)) returning IngestStep("my-id", IngestStepLabel.COMPLETED, lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.DOI, "my-doi-value", lastModified)) returning Identifier("my-id", IdentifierType.DOI, "my-doi-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.URN, "my-urn-value", lastModified)) returning Identifier("my-id", IdentifierType.URN, "my-urn-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.FEDORA, "my-fedora-value", lastModified)) returning Identifier("my-id", IdentifierType.FEDORA, "my-fedora-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.BAG_STORE, "my-bag-store-value", lastModified)) returning Identifier("my-id", IdentifierType.BAG_STORE, "my-bag-store-value", lastModified).asRight
      repo.setDoiRegistered _ expects(depositId, DoiRegisteredEvent(value = true, lastModified)) returning DoiRegisteredEvent(value = true, lastModified).asRight
      repo.setDoiAction _ expects(depositId, DoiActionEvent(DoiAction.UPDATE, lastModified)) returning DoiActionEvent(DoiAction.UPDATE, lastModified).asRight
      repo.setCurator _ expects(depositId, InputCurator("archie001", "does.not.exists@dans.knaw.nl", lastModified)) returning Curator("my-id", "archie001", "does.not.exists@dans.knaw.nl", lastModified).asRight
      repo.setIsNewVersionAction _ expects(depositId, IsNewVersionEvent(isNewVersion = true, lastModified)) returning IsNewVersionEvent(isNewVersion = true, lastModified).asRight
      repo.setCurationRequiredAction _ expects(depositId, CurationRequiredEvent(curationRequired = false, lastModified)) returning CurationRequiredEvent(curationRequired = false, lastModified).asRight
      repo.setCurationPerformedAction _ expects(depositId, CurationPerformedEvent(curationPerformed = false, lastModified)) returning CurationPerformedEvent(curationPerformed = false, lastModified).asRight
      repo.setSpringfield _ expects(depositId, InputSpringfield("domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified)) returning Springfield("my-id", "domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified).asRight
      repo.setContentType _ expects(depositId, InputContentType(ContentTypeValue.ZIP, lastModified)) returning ContentType("my-id", ContentTypeValue.ZIP, lastModified).asRight
    }

    importProps.loadDepositProperties(file) shouldBe right
  }

  it should "interact with DataCite when the identifier.dans-doi.registered property is not set" in {
    val time = new DateTime(2019, 1, 1, 0, 0, timeZone)
    val file = writeProps()(defaultProps(cfg => {
      // doi events
      cfg.clearProperty("identifier.dans-doi.registered") // no identifier.dans-doi.registered, interaction with DataCite expected
    }))
    val depositId = UUID.fromString(file.parent.name)
    val lastModified = new DateTime(file.attributes.lastModifiedTime().toMillis)

    inSequence {
      repo.addDeposit _ expects where(isDeposit(Deposit(depositId, "bag".some, time, "user001"))) returning Deposit(depositId, "bag".some, time, "user001").asRight
      repo.setState _ expects(depositId, InputState(StateLabel.SUBMITTED, "my description", lastModified)) returning State("my-id", StateLabel.SUBMITTED, "my description", lastModified).asRight
      repo.setIngestStep _ expects(depositId, InputIngestStep(IngestStepLabel.BAGSTORE, lastModified)) returning IngestStep("my-id", IngestStepLabel.BAGSTORE, lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.DOI, "my-doi-value", lastModified)) returning Identifier("my-id", IdentifierType.DOI, "my-doi-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.URN, "my-urn-value", lastModified)) returning Identifier("my-id", IdentifierType.URN, "my-urn-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.FEDORA, "my-fedora-value", lastModified)) returning Identifier("my-id", IdentifierType.FEDORA, "my-fedora-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.BAG_STORE, "my-bag-store-value", lastModified)) returning Identifier("my-id", IdentifierType.BAG_STORE, "my-bag-store-value", lastModified).asRight
      datacite.doiExists _ expects "my-doi-value" returning true
      repo.setDoiRegistered _ expects(depositId, DoiRegisteredEvent(value = true, lastModified)) returning DoiRegisteredEvent(value = true, lastModified).asRight
      repo.setDoiAction _ expects(depositId, DoiActionEvent(DoiAction.UPDATE, lastModified)) returning DoiActionEvent(DoiAction.UPDATE, lastModified).asRight
      repo.setCurator _ expects(depositId, InputCurator("archie001", "does.not.exists@dans.knaw.nl", lastModified)) returning Curator("my-id", "archie001", "does.not.exists@dans.knaw.nl", lastModified).asRight
      repo.setIsNewVersionAction _ expects(depositId, IsNewVersionEvent(isNewVersion = true, lastModified)) returning IsNewVersionEvent(isNewVersion = true, lastModified).asRight
      repo.setCurationRequiredAction _ expects(depositId, CurationRequiredEvent(curationRequired = false, lastModified)) returning CurationRequiredEvent(curationRequired = false, lastModified).asRight
      repo.setCurationPerformedAction _ expects(depositId, CurationPerformedEvent(curationPerformed = false, lastModified)) returning CurationPerformedEvent(curationPerformed = false, lastModified).asRight
      repo.setSpringfield _ expects(depositId, InputSpringfield("domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified)) returning Springfield("my-id", "domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified).asRight
      repo.setContentType _ expects(depositId, InputContentType(ContentTypeValue.ZIP, lastModified)) returning ContentType("my-id", ContentTypeValue.ZIP, lastModified).asRight
    }

    importProps.loadDepositProperties(file) shouldBe right
  }

  it should "interact with the user when interaction with DataCite fails" in {
    val time = new DateTime(2019, 1, 1, 0, 0, timeZone)
    val file = writeProps()(defaultProps(cfg => {
      // doi events
      cfg.clearProperty("identifier.dans-doi.registered") // no identifier.dans-doi.registered, interaction with DataCite fails, interaction with user instead
    }))
    val depositId = UUID.fromString(file.parent.name)
    val lastModified = new DateTime(file.attributes.lastModifiedTime().toMillis)

    inSequence {
      repo.addDeposit _ expects where(isDeposit(Deposit(depositId, "bag".some, time, "user001"))) returning Deposit(depositId, "bag".some, time, "user001").asRight
      repo.setState _ expects(depositId, InputState(StateLabel.SUBMITTED, "my description", lastModified)) returning State("my-id", StateLabel.SUBMITTED, "my description", lastModified).asRight
      repo.setIngestStep _ expects(depositId, InputIngestStep(IngestStepLabel.BAGSTORE, lastModified)) returning IngestStep("my-id", IngestStepLabel.BAGSTORE, lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.DOI, "my-doi-value", lastModified)) returning Identifier("my-id", IdentifierType.DOI, "my-doi-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.URN, "my-urn-value", lastModified)) returning Identifier("my-id", IdentifierType.URN, "my-urn-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.FEDORA, "my-fedora-value", lastModified)) returning Identifier("my-id", IdentifierType.FEDORA, "my-fedora-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.BAG_STORE, "my-bag-store-value", lastModified)) returning Identifier("my-id", IdentifierType.BAG_STORE, "my-bag-store-value", lastModified).asRight
      datacite.doiExists _ expects "my-doi-value" throws new DataciteServiceException("FAIL!!!", 418)
      (interactor.ask(_: String => Boolean)(_: String)) expects(*, *) returning true
      repo.setDoiRegistered _ expects(depositId, DoiRegisteredEvent(value = true, lastModified)) returning DoiRegisteredEvent(value = true, lastModified).asRight
      repo.setDoiAction _ expects(depositId, DoiActionEvent(DoiAction.UPDATE, lastModified)) returning DoiActionEvent(DoiAction.UPDATE, lastModified).asRight
      repo.setCurator _ expects(depositId, InputCurator("archie001", "does.not.exists@dans.knaw.nl", lastModified)) returning Curator("my-id", "archie001", "does.not.exists@dans.knaw.nl", lastModified).asRight
      repo.setIsNewVersionAction _ expects(depositId, IsNewVersionEvent(isNewVersion = true, lastModified)) returning IsNewVersionEvent(isNewVersion = true, lastModified).asRight
      repo.setCurationRequiredAction _ expects(depositId, CurationRequiredEvent(curationRequired = false, lastModified)) returning CurationRequiredEvent(curationRequired = false, lastModified).asRight
      repo.setCurationPerformedAction _ expects(depositId, CurationPerformedEvent(curationPerformed = false, lastModified)) returning CurationPerformedEvent(curationPerformed = false, lastModified).asRight
      repo.setSpringfield _ expects(depositId, InputSpringfield("domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified)) returning Springfield("my-id", "domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified).asRight
      repo.setContentType _ expects(depositId, InputContentType(ContentTypeValue.ZIP, lastModified)) returning ContentType("my-id", ContentTypeValue.ZIP, lastModified).asRight
    }

    importProps.loadDepositProperties(file) shouldBe right
  }

  it should "interact with the user when an invalid value is given for springfield.playmode" in {
    val time = new DateTime(2019, 1, 1, 0, 0, timeZone)
    val file = writeProps()(defaultProps(cfg => {
      // springfield
      cfg.setProperty("springfield.playmode", "invalid-value") // invalid value for springfield.playmode, interaction expected
    }))
    val depositId = UUID.fromString(file.parent.name)
    val lastModified = new DateTime(file.attributes.lastModifiedTime().toMillis)

    inSequence {
      repo.addDeposit _ expects where(isDeposit(Deposit(depositId, "bag".some, time, "user001"))) returning Deposit(depositId, "bag".some, time, "user001").asRight
      repo.setState _ expects(depositId, InputState(StateLabel.SUBMITTED, "my description", lastModified)) returning State("my-id", StateLabel.SUBMITTED, "my description", lastModified).asRight
      repo.setIngestStep _ expects(depositId, InputIngestStep(IngestStepLabel.BAGSTORE, lastModified)) returning IngestStep("my-id", IngestStepLabel.BAGSTORE, lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.DOI, "my-doi-value", lastModified)) returning Identifier("my-id", IdentifierType.DOI, "my-doi-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.URN, "my-urn-value", lastModified)) returning Identifier("my-id", IdentifierType.URN, "my-urn-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.FEDORA, "my-fedora-value", lastModified)) returning Identifier("my-id", IdentifierType.FEDORA, "my-fedora-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.BAG_STORE, "my-bag-store-value", lastModified)) returning Identifier("my-id", IdentifierType.BAG_STORE, "my-bag-store-value", lastModified).asRight
      repo.setDoiRegistered _ expects(depositId, DoiRegisteredEvent(value = true, lastModified)) returning DoiRegisteredEvent(value = true, lastModified).asRight
      repo.setDoiAction _ expects(depositId, DoiActionEvent(DoiAction.UPDATE, lastModified)) returning DoiActionEvent(DoiAction.UPDATE, lastModified).asRight
      repo.setCurator _ expects(depositId, InputCurator("archie001", "does.not.exists@dans.knaw.nl", lastModified)) returning Curator("my-id", "archie001", "does.not.exists@dans.knaw.nl", lastModified).asRight
      repo.setIsNewVersionAction _ expects(depositId, IsNewVersionEvent(isNewVersion = true, lastModified)) returning IsNewVersionEvent(isNewVersion = true, lastModified).asRight
      repo.setCurationRequiredAction _ expects(depositId, CurationRequiredEvent(curationRequired = false, lastModified)) returning CurationRequiredEvent(curationRequired = false, lastModified).asRight
      repo.setCurationPerformedAction _ expects(depositId, CurationPerformedEvent(curationPerformed = false, lastModified)) returning CurationPerformedEvent(curationPerformed = false, lastModified).asRight
      (interactor.ask(_: Enumeration)(_: String)) expects(SpringfieldPlayMode, *) returning SpringfieldPlayMode.MENU.asRight
      repo.setSpringfield _ expects(depositId, InputSpringfield("domain", "user", "collection", SpringfieldPlayMode.MENU, lastModified)) returning Springfield("my-id", "domain", "user", "collection", SpringfieldPlayMode.MENU, lastModified).asRight
      repo.setContentType _ expects(depositId, InputContentType(ContentTypeValue.ZIP, lastModified)) returning ContentType("my-id", ContentTypeValue.ZIP, lastModified).asRight
    }

    importProps.loadDepositProperties(file) shouldBe right
  }

  it should "interact with the user when an invalid value is given for easy-sword2.client-message.content-type" in {
    val time = new DateTime(2019, 1, 1, 0, 0, timeZone)
    val file = writeProps()(defaultProps(cfg => {
      // springfield
      cfg.setProperty("easy-sword2.client-message.content-type", "invalid") // invalid value for easy-sword2.client-message.content-type, interaction expected
    }))
    val depositId = UUID.fromString(file.parent.name)
    val lastModified = new DateTime(file.attributes.lastModifiedTime().toMillis)

    inSequence {
      repo.addDeposit _ expects where(isDeposit(Deposit(depositId, "bag".some, time, "user001"))) returning Deposit(depositId, "bag".some, time, "user001").asRight
      repo.setState _ expects(depositId, InputState(StateLabel.SUBMITTED, "my description", lastModified)) returning State("my-id", StateLabel.SUBMITTED, "my description", lastModified).asRight
      repo.setIngestStep _ expects(depositId, InputIngestStep(IngestStepLabel.BAGSTORE, lastModified)) returning IngestStep("my-id", IngestStepLabel.BAGSTORE, lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.DOI, "my-doi-value", lastModified)) returning Identifier("my-id", IdentifierType.DOI, "my-doi-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.URN, "my-urn-value", lastModified)) returning Identifier("my-id", IdentifierType.URN, "my-urn-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.FEDORA, "my-fedora-value", lastModified)) returning Identifier("my-id", IdentifierType.FEDORA, "my-fedora-value", lastModified).asRight
      repo.addIdentifier _ expects(depositId, InputIdentifier(IdentifierType.BAG_STORE, "my-bag-store-value", lastModified)) returning Identifier("my-id", IdentifierType.BAG_STORE, "my-bag-store-value", lastModified).asRight
      repo.setDoiRegistered _ expects(depositId, DoiRegisteredEvent(value = true, lastModified)) returning DoiRegisteredEvent(value = true, lastModified).asRight
      repo.setDoiAction _ expects(depositId, DoiActionEvent(DoiAction.UPDATE, lastModified)) returning DoiActionEvent(DoiAction.UPDATE, lastModified).asRight
      repo.setCurator _ expects(depositId, InputCurator("archie001", "does.not.exists@dans.knaw.nl", lastModified)) returning Curator("my-id", "archie001", "does.not.exists@dans.knaw.nl", lastModified).asRight
      repo.setIsNewVersionAction _ expects(depositId, IsNewVersionEvent(isNewVersion = true, lastModified)) returning IsNewVersionEvent(isNewVersion = true, lastModified).asRight
      repo.setCurationRequiredAction _ expects(depositId, CurationRequiredEvent(curationRequired = false, lastModified)) returning CurationRequiredEvent(curationRequired = false, lastModified).asRight
      repo.setCurationPerformedAction _ expects(depositId, CurationPerformedEvent(curationPerformed = false, lastModified)) returning CurationPerformedEvent(curationPerformed = false, lastModified).asRight
      repo.setSpringfield _ expects(depositId, InputSpringfield("domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified)) returning Springfield("my-id", "domain", "user", "collection", SpringfieldPlayMode.CONTINUOUS, lastModified).asRight
      (interactor.ask(_: Enumeration)(_: String)) expects(ContentTypeValue, *) returning ContentTypeValue.ZIP.asRight
      repo.setContentType _ expects(depositId, InputContentType(ContentTypeValue.ZIP, lastModified)) returning ContentType("my-id", ContentTypeValue.ZIP, lastModified).asRight
    }

    importProps.loadDepositProperties(file) shouldBe right
  }

  it should "fail when the properties file doesn't exist" in {
    val file = writeProps("invalid-depositId")(_ => ())
    file.delete()
    file shouldNot exist

    importProps.loadDepositProperties(file).leftValue shouldBe NoSuchPropertiesFileError(file)
  }

  it should "fail when the parent directory is not a valid depositId" in {
    val file = writeProps("invalid-depositId")(_ => ())

    importProps.loadDepositProperties(file).leftValue shouldBe NoDepositIdError("invalid-depositId")
  }

  private def writeProps(depositId: String)(initProps: PropertiesConfiguration => Unit): File = {
    val file = (testDir / depositId.toString / "deposit.properties").createFileIfNotExists(createParents = true)

    val props = new PropertiesConfiguration() {
      setDelimiterParsingDisabled(true)
    }
    initProps(props)

    props.save(file.toJava)

    file
  }

  private def writeProps(depositId: DepositId = UUID.randomUUID())(initProps: PropertiesConfiguration => Unit): File = {
    writeProps(depositId.toString)(initProps)
  }

  private def defaultProps(initProps: PropertiesConfiguration => Unit = _ => ())(cfg: PropertiesConfiguration): Unit = {
    // deposit
    cfg.setProperty("bag-store.bag-name", "bag")
    cfg.setProperty("creation.timestamp", time.toString)
    cfg.setProperty("depositor.userId", "user001")

    // state
    cfg.setProperty("state.label", "SUBMITTED")
    cfg.setProperty("state.description", "my description")

    // ingest step
    cfg.setProperty("deposit.ingest.current-step", "BAGSTORE")

    // identifiers
    cfg.setProperty("identifier.doi", "my-doi-value")
    cfg.setProperty("identifier.urn", "my-urn-value")
    cfg.setProperty("identifier.fedora", "my-fedora-value")
    cfg.setProperty("bag-store.bag-id", "my-bag-store-value")

    // doi events
    cfg.setProperty("identifier.dans-doi.registered", "yes")
    cfg.setProperty("identifier.dans-doi.action", "update")

    // curator
    cfg.setProperty("curation.datamanager.userId", "archie001")
    cfg.setProperty("curation.datamanager.email", "does.not.exists@dans.knaw.nl")

    // curation events
    cfg.setProperty("curation.is-new-version", "yes")
    cfg.setProperty("curation.required", "no")
    cfg.setProperty("curation.performed", "no")

    // springfield
    cfg.setProperty("springfield.domain", "domain")
    cfg.setProperty("springfield.user", "user")
    cfg.setProperty("springfield.collection", "collection")
    cfg.setProperty("springfield.playmode", "continuous")

    // content-type
    cfg.setProperty("easy-sword2.client-message.content-type", "application/zip")

    initProps(cfg)
  }

  private def isDeposit(deposit: Deposit): Deposit => Boolean = d => {
    d.id == deposit.id &&
      d.depositorId == deposit.depositorId &&
      d.bagName == deposit.bagName &&
      d.creationTimestamp.getMillis == deposit.creationTimestamp.getMillis
  }
}
