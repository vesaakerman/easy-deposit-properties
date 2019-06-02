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
package nl.knaw.dans.easy.properties.app.graphql.example.repository

import java.util.{ TimeZone, UUID }

import nl.knaw.dans.easy.properties.app.model._
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, ContentTypeValue }
import nl.knaw.dans.easy.properties.app.model.curator.Curator
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStep, IngestStepLabel }
import nl.knaw.dans.easy.properties.app.model.springfield.{ Springfield, SpringfieldPlayMode }
import nl.knaw.dans.easy.properties.app.model.state.{ State, StateLabel }
import org.joda.time.{ DateTime, DateTimeZone }

import scala.collection.mutable

class DemoRepositoryImpl extends DemoRepository {

  private val timeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"))

  private val depositId1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
  private val depositId2 = UUID.fromString("00000000-0000-0000-0000-000000000002")
  private val depositId3 = UUID.fromString("00000000-0000-0000-0000-000000000003")
  private val depositId4 = UUID.fromString("00000000-0000-0000-0000-000000000004")
  private val depositId5 = UUID.fromString("00000000-0000-0000-0000-000000000005")

  private val user1 = "user001"
  private val user2 = "user002"

  private val deposit1 = Deposit(depositId1, new DateTime(2019, 1, 1, 0, 0, timeZone), user1)
  private val deposit2 = Deposit(depositId2, new DateTime(2019, 2, 2, 0, 0, timeZone), user1)
  private val deposit3 = Deposit(depositId3, new DateTime(2019, 3, 3, 0, 0, timeZone), user2)
  private val deposit4 = Deposit(depositId4, new DateTime(2019, 4, 4, 0, 0, timeZone), user1)
  private val deposit5 = Deposit(depositId5, new DateTime(2019, 5, 5, 0, 0, timeZone), user2)

  private val state10 = State("10", StateLabel.DRAFT, "draft with continued deposit", new DateTime(2019, 1, 1, 0, 0, timeZone))
  private val state11 = State("11", StateLabel.DRAFT, "draft with continued deposit", new DateTime(2019, 1, 1, 1, 1, timeZone))
  private val state12 = State("12", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 1, 1, 2, 2, timeZone))
  private val state13 = State("13", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 1, 1, 3, 3, timeZone))
  private val state14 = State("14", StateLabel.SUBMITTED, "deposit is processing", new DateTime(2019, 1, 1, 4, 4, timeZone))
  private val state15 = State("15", StateLabel.ARCHIVED, "deposit is archived", new DateTime(2019, 1, 1, 5, 5, timeZone))

  private val state20 = State("20", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val state21 = State("21", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 2, 2, 1, 1, timeZone))
  private val state22 = State("22", StateLabel.SUBMITTED, "deposit is processing", new DateTime(2019, 2, 2, 2, 2, timeZone))
  private val state23 = State("23", StateLabel.ARCHIVED, "deposit is archived", new DateTime(2019, 2, 2, 3, 3, timeZone))

  private val state30 = State("30", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 3, 3, 0, 0, timeZone))
  private val state31 = State("31", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 3, 3, 1, 1, timeZone))
  private val state32 = State("32", StateLabel.INVALID, "deposit is invalid", new DateTime(2019, 3, 3, 2, 2, timeZone))

  private val state40 = State("40", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 4, 4, 0, 0, timeZone))
  private val state41 = State("41", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 4, 4, 1, 1, timeZone))
  private val state42 = State("42", StateLabel.ARCHIVED, "deposit is archived", new DateTime(2019, 4, 4, 2, 2, timeZone))

  private val state50 = State("50", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 5, 5, 0, 0, timeZone))
  private val state51 = State("51", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 5, 5, 1, 1, timeZone))
  private val state52 = State("52", StateLabel.SUBMITTED, "deposit is processing", new DateTime(2019, 5, 5, 2, 2, timeZone))
  private val state53 = State("53", StateLabel.REJECTED, "deposit is rejected", new DateTime(2019, 5, 5, 3, 3, timeZone))

  private val step10 = IngestStep("10", IngestStepLabel.VALIDATE, new DateTime(2019, 1, 1, 4, 5, timeZone))
  private val step11 = IngestStep("11", IngestStepLabel.PID_GENERATOR, new DateTime(2019, 1, 1, 4, 6, timeZone))
  private val step12 = IngestStep("12", IngestStepLabel.FEDORA, new DateTime(2019, 1, 1, 4, 7, timeZone))
  private val step13 = IngestStep("13", IngestStepLabel.SPRINGFIELD, new DateTime(2019, 1, 1, 4, 8, timeZone))
  private val step14 = IngestStep("14", IngestStepLabel.BAGSTORE, new DateTime(2019, 1, 1, 4, 9, timeZone))
  private val step15 = IngestStep("15", IngestStepLabel.SOLR4FILES, new DateTime(2019, 1, 1, 4, 10, timeZone))
  private val step16 = IngestStep("16", IngestStepLabel.COMPLETED, new DateTime(2019, 1, 1, 4, 11, timeZone))

  private val step20 = IngestStep("20", IngestStepLabel.VALIDATE, new DateTime(2019, 2, 2, 2, 5, timeZone))
  private val step21 = IngestStep("21", IngestStepLabel.PID_GENERATOR, new DateTime(2019, 2, 2, 2, 6, timeZone))
  private val step22 = IngestStep("22", IngestStepLabel.FEDORA, new DateTime(2019, 2, 2, 2, 7, timeZone))
  private val step23 = IngestStep("23", IngestStepLabel.SPRINGFIELD, new DateTime(2019, 2, 2, 2, 8, timeZone))
  private val step24 = IngestStep("24", IngestStepLabel.BAGSTORE, new DateTime(2019, 2, 2, 2, 9, timeZone))
  private val step25 = IngestStep("25", IngestStepLabel.SOLR4FILES, new DateTime(2019, 2, 2, 2, 10, timeZone))
  private val step26 = IngestStep("26", IngestStepLabel.COMPLETED, new DateTime(2019, 2, 2, 2, 11, timeZone))

  private val step50 = IngestStep("50", IngestStepLabel.VALIDATE, new DateTime(2019, 5, 5, 4, 5, timeZone))

  private val identifier11 = Identifier("11", IdentifierType.BAG_STORE, depositId1.toString, new DateTime(2019, 1, 1, 0, 0, timeZone))
  private val identifier12 = Identifier("12", IdentifierType.DOI, "10.5072/dans-a1b-cde2", new DateTime(2019, 1, 1, 0, 1, timeZone))
  private val identifier13 = Identifier("13", IdentifierType.URN, "urn:nbn:123456", new DateTime(2019, 1, 1, 0, 2, timeZone))
  private val identifier14 = Identifier("14", IdentifierType.FEDORA, "easy-dataset:1", new DateTime(2019, 1, 1, 0, 3, timeZone))

  private val identifier21 = Identifier("21", IdentifierType.BAG_STORE, depositId2.toString, new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val identifier22 = Identifier("22", IdentifierType.DOI, "10.5072/dans-f3g-hij4", new DateTime(2019, 2, 2, 0, 1, timeZone))
  private val identifier23 = Identifier("23", IdentifierType.URN, "urn:nbn:789012", new DateTime(2019, 2, 2, 0, 2, timeZone))
  private val identifier24 = Identifier("24", IdentifierType.FEDORA, "easy-dataset:2", new DateTime(2019, 2, 2, 0, 3, timeZone))

  private val identifier31 = Identifier("31", IdentifierType.BAG_STORE, depositId3.toString, new DateTime(2019, 3, 3, 0, 0, timeZone))

  private val identifier41 = Identifier("41", IdentifierType.BAG_STORE, depositId4.toString, new DateTime(2019, 4, 4, 0, 0, timeZone))
  private val identifier42 = Identifier("42", IdentifierType.DOI, "10.5072/dans-p7q-rst8", new DateTime(2019, 4, 4, 0, 1, timeZone))
  private val identifier43 = Identifier("43", IdentifierType.URN, "urn:nbn:901234", new DateTime(2019, 4, 4, 0, 2, timeZone))
  private val identifier44 = Identifier("44", IdentifierType.FEDORA, "easy-dataset:4", new DateTime(2019, 4, 4, 0, 3, timeZone))

  private val identifier51 = Identifier("51", IdentifierType.BAG_STORE, depositId5.toString, new DateTime(2019, 5, 5, 0, 0, timeZone))

  private val doiRegistered10 = DoiRegisteredEvent(value = false, new DateTime(2019, 1, 1, 0, 0, timeZone))
  private val doiRegistered11 = DoiRegisteredEvent(value = true, new DateTime(2019, 1, 1, 4, 7, timeZone))
  private val doiRegistered20 = DoiRegisteredEvent(value = false, new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val doiRegistered21 = DoiRegisteredEvent(value = true, new DateTime(2019, 2, 2, 2, 7, timeZone))
  private val doiRegistered50 = DoiRegisteredEvent(value = false, new DateTime(2019, 5, 5, 0, 0, timeZone))

  private val doiAction10 = DoiActionEvent(DoiAction.UPDATE, new DateTime(2019, 1, 1, 0, 0, timeZone))
  private val doiAction11 = DoiActionEvent(DoiAction.NONE, new DateTime(2019, 1, 1, 4, 5, timeZone))
  private val doiAction20 = DoiActionEvent(DoiAction.CREATE, new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val doiAction30 = DoiActionEvent(DoiAction.CREATE, new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val doiAction40 = DoiActionEvent(DoiAction.CREATE, new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val doiAction50 = DoiActionEvent(DoiAction.UPDATE, new DateTime(2019, 5, 5, 0, 0, timeZone))

  private val curator11 = Curator("11", "archie002", "does.not.exists2@dans.knaw.nl", new DateTime(2019, 1, 1, 0, 0, timeZone))
  private val curator12 = Curator("12", "archie001", "does.not.exists1@dans.knaw.nl", new DateTime(2019, 1, 1, 3, 3, timeZone))

  private val curator31 = Curator("31", "archie001", "does.not.exists1@dans.knaw.nl", new DateTime(2019, 3, 3, 0, 0, timeZone))
  private val curator32 = Curator("32", "archie002", "does.not.exists2@dans.knaw.nl", new DateTime(2019, 3, 3, 4, 6, timeZone))

  private val curator41 = Curator("41", "archie001", "does.not.exists1@dans.knaw.nl", new DateTime(2019, 4, 4, 0, 0, timeZone))

  private val curator51 = Curator("51", "archie001", "does.not.exists1@dans.knaw.nl", new DateTime(2019, 4, 4, 0, 0, timeZone))

  private val isNewVersion10 = IsNewVersionEvent(isNewVersion = false, new DateTime(2019, 1, 1, 0, 0, timeZone))
  private val isNewVersion20 = IsNewVersionEvent(isNewVersion = false, new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val isNewVersion30 = IsNewVersionEvent(isNewVersion = true, new DateTime(2019, 3, 3, 0, 0, timeZone))
  private val isNewVersion40 = IsNewVersionEvent(isNewVersion = true, new DateTime(2019, 4, 4, 0, 0, timeZone))
  private val isNewVersion50 = IsNewVersionEvent(isNewVersion = false, new DateTime(2019, 5, 5, 0, 0, timeZone))

  private val curationRequired10 = CurationRequiredEvent(curationRequired = true, new DateTime(2019, 1, 1, 0, 0, timeZone))
  private val curationRequired20 = CurationRequiredEvent(curationRequired = false, new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val curationRequired30 = CurationRequiredEvent(curationRequired = true, new DateTime(2019, 3, 3, 0, 0, timeZone))
  private val curationRequired40 = CurationRequiredEvent(curationRequired = true, new DateTime(2019, 4, 4, 0, 0, timeZone))
  private val curationRequired50 = CurationRequiredEvent(curationRequired = true, new DateTime(2019, 5, 5, 0, 0, timeZone))

  private val curationPerformed10 = CurationPerformedEvent(curationPerformed = false, new DateTime(2019, 1, 1, 0, 0, timeZone))
  private val curationPerformed11 = CurationPerformedEvent(curationPerformed = true, new DateTime(2019, 1, 1, 4, 4, timeZone))
  private val curationPerformed20 = CurationPerformedEvent(curationPerformed = false, new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val curationPerformed30 = CurationPerformedEvent(curationPerformed = false, new DateTime(2019, 3, 3, 0, 0, timeZone))
  private val curationPerformed31 = CurationPerformedEvent(curationPerformed = true, new DateTime(2019, 3, 3, 4, 4, timeZone))
  private val curationPerformed40 = CurationPerformedEvent(curationPerformed = false, new DateTime(2019, 4, 4, 0, 0, timeZone))
  private val curationPerformed41 = CurationPerformedEvent(curationPerformed = true, new DateTime(2019, 4, 4, 4, 4, timeZone))
  private val curationPerformed50 = CurationPerformedEvent(curationPerformed = false, new DateTime(2019, 5, 5, 0, 0, timeZone))
  private val curationPerformed51 = CurationPerformedEvent(curationPerformed = true, new DateTime(2019, 5, 5, 4, 4, timeZone))

  private val springfield10 = Springfield("10", "domain1", "user1", "collection1", SpringfieldPlayMode.CONTINUOUS, new DateTime(2019, 1, 1, 0, 0, timeZone))
  private val springfield20 = Springfield("20", "domain1", "user1", "collection1", SpringfieldPlayMode.CONTINUOUS, new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val springfield21 = Springfield("21", "domain2", "user2", "collection2", SpringfieldPlayMode.CONTINUOUS, new DateTime(2019, 2, 2, 2, 2, timeZone))

  private val contentType10 = ContentType("10", ContentTypeValue.ZIP, new DateTime(2019, 1, 1, 0, 5, timeZone))
  private val contentType11 = ContentType("11", ContentTypeValue.OCTET, new DateTime(2019, 1, 1, 0, 10, timeZone))
  private val contentType20 = ContentType("20", ContentTypeValue.ZIP, new DateTime(2019, 2, 2, 0, 5, timeZone))
  private val contentType30 = ContentType("30", ContentTypeValue.ZIP, new DateTime(2019, 3, 3, 0, 5, timeZone))
  private val contentType40 = ContentType("40", ContentTypeValue.ZIP, new DateTime(2019, 4, 4, 0, 5, timeZone))
  private val contentType50 = ContentType("50", ContentTypeValue.ZIP, new DateTime(2019, 5, 5, 0, 5, timeZone))

  override val depositRepo: mutable.Map[DepositId, Deposit] = mutable.Map.empty
  override val stateRepo: mutable.Map[DepositId, Seq[State]] = mutable.Map.empty
  override val stepRepo: mutable.Map[DepositId, Seq[IngestStep]] = mutable.Map.empty
  override val identifierRepo: mutable.Map[(DepositId, IdentifierType), Identifier] = mutable.Map.empty
  override val doiRegisteredRepo: mutable.Map[DepositId, Seq[DoiRegisteredEvent]] = mutable.Map.empty
  override val doiActionRepo: mutable.Map[DepositId, Seq[DoiActionEvent]] = mutable.Map.empty
  override val curatorRepo: mutable.Map[DepositId, Seq[Curator]] = mutable.Map.empty
  override val isNewVersionRepo: mutable.Map[DepositId, Seq[IsNewVersionEvent]] = mutable.Map.empty
  override val curationRequiredRepo: mutable.Map[DepositId, Seq[CurationRequiredEvent]] = mutable.Map.empty
  override val curationPerformedRepo: mutable.Map[DepositId, Seq[CurationPerformedEvent]] = mutable.Map.empty
  override val springfieldRepo: mutable.Map[DepositId, Seq[Springfield]] = mutable.Map.empty
  override val contentTypeRepo: mutable.Map[DepositId, Seq[ContentType]] = mutable.Map.empty

  resetRepository()

  def resetRepository(): Unit = {
    resetDepositRepo()
    resetStateRepo()
    resetStepRepo()
    resetIdentifierRepo()
    resetDoiRegisteredRepo()
    resetDoiActionRepo()
    resetCuratorRepo()
    resetIsNewVersionRepo()
    resetCurationRequiredRepo()
    resetCurationPerformedRepo()
    resetSpringfieldRepo()
    resetContentTypeRepo()
  }

  def resetDepositRepo(): mutable.Map[DepositId, Deposit] = {
    depositRepo.clear()
    depositRepo ++= Map(
      depositId1 -> deposit1,
      depositId2 -> deposit2,
      depositId3 -> deposit3,
      depositId4 -> deposit4,
      depositId5 -> deposit5,
    )
  }

  def resetStateRepo(): mutable.Map[DepositId, Seq[State]] = {
    stateRepo.clear()
    stateRepo ++= Map(
      depositId1 -> Seq(state10, state11, state12, state13, state14, state15),
      depositId2 -> Seq(state20, state21, state22, state23),
      depositId3 -> Seq(state30, state31, state32),
      depositId4 -> Seq(state40, state41, state42),
      depositId5 -> Seq(state50, state51, state52, state53),
    )
  }

  def resetStepRepo(): mutable.Map[DepositId, Seq[IngestStep]] = {
    stepRepo.clear()
    stepRepo ++= Map(
      depositId1 -> Seq(step10, step11, step12, step13, step14, step15, step16),
      depositId2 -> Seq(step20, step21, step22, step23, step24, step25, step26),
      depositId3 -> Seq.empty,
      depositId4 -> Seq.empty,
      depositId5 -> Seq(step50),
    )
  }

  def resetIdentifierRepo(): mutable.Map[(DepositId, IdentifierType), Identifier] = {
    identifierRepo.clear()
    identifierRepo ++= Map(
      (depositId1 -> IdentifierType.BAG_STORE) -> identifier11,
      (depositId1 -> IdentifierType.DOI) -> identifier12,
      (depositId1 -> IdentifierType.URN) -> identifier13,
      (depositId1 -> IdentifierType.FEDORA) -> identifier14,

      (depositId2 -> IdentifierType.BAG_STORE) -> identifier21,
      (depositId2 -> IdentifierType.DOI) -> identifier22,
      (depositId2 -> IdentifierType.URN) -> identifier23,
      (depositId2 -> IdentifierType.FEDORA) -> identifier24,

      (depositId3 -> IdentifierType.BAG_STORE) -> identifier31,

      (depositId4 -> IdentifierType.BAG_STORE) -> identifier41,
      (depositId4 -> IdentifierType.DOI) -> identifier42,
      (depositId4 -> IdentifierType.URN) -> identifier43,
      (depositId4 -> IdentifierType.FEDORA) -> identifier44,

      (depositId5 -> IdentifierType.BAG_STORE) -> identifier51,
    )
  }

  def resetDoiRegisteredRepo(): mutable.Map[DepositId, Seq[DoiRegisteredEvent]] = {
    doiRegisteredRepo.clear()
    doiRegisteredRepo ++= Map(
      depositId1 -> Seq(doiRegistered10, doiRegistered11),
      depositId2 -> Seq(doiRegistered20, doiRegistered21),
      depositId5 -> Seq(doiRegistered50),
    )
  }

  def resetDoiActionRepo(): mutable.Map[DepositId, Seq[DoiActionEvent]] = {
    doiActionRepo.clear()
    doiActionRepo ++= Map(
      depositId1 -> Seq(doiAction10, doiAction11),
      depositId2 -> Seq(doiAction20),
      depositId3 -> Seq(doiAction30),
      depositId4 -> Seq(doiAction40),
      depositId5 -> Seq(doiAction50),
    )
  }

  def resetCuratorRepo(): mutable.Map[DepositId, Seq[Curator]] = {
    curatorRepo.clear()
    curatorRepo ++= Map(
      depositId1 -> Seq(curator11, curator12),
      depositId3 -> Seq(curator31, curator32),
      depositId4 -> Seq(curator41),
      depositId5 -> Seq(curator51),
    )
  }

  def resetIsNewVersionRepo(): mutable.Map[DepositId, Seq[IsNewVersionEvent]] = {
    isNewVersionRepo.clear()
    isNewVersionRepo ++= Map(
      depositId1 -> Seq(isNewVersion10),
      depositId2 -> Seq(isNewVersion20),
      depositId3 -> Seq(isNewVersion30),
      depositId4 -> Seq(isNewVersion40),
      depositId5 -> Seq(isNewVersion50),
    )
  }

  def resetCurationRequiredRepo(): mutable.Map[DepositId, Seq[CurationRequiredEvent]] = {
    curationRequiredRepo.clear()
    curationRequiredRepo ++= Map(
      depositId1 -> Seq(curationRequired10),
      depositId2 -> Seq(curationRequired20),
      depositId3 -> Seq(curationRequired30),
      depositId4 -> Seq(curationRequired40),
      depositId5 -> Seq(curationRequired50),
    )
  }

  def resetCurationPerformedRepo(): mutable.Map[DepositId, Seq[CurationPerformedEvent]] = {
    curationPerformedRepo.clear()
    curationPerformedRepo ++= Map(
      depositId1 -> Seq(curationPerformed10, curationPerformed11),
      depositId2 -> Seq(curationPerformed20),
      depositId3 -> Seq(curationPerformed30, curationPerformed31),
      depositId4 -> Seq(curationPerformed40, curationPerformed41),
      depositId5 -> Seq(curationPerformed50, curationPerformed51),
    )
  }

  def resetSpringfieldRepo(): mutable.Map[DepositId, Seq[Springfield]] = {
    springfieldRepo.clear()
    springfieldRepo ++= Map(
      depositId1 -> Seq(springfield10),
      depositId2 -> Seq(springfield20, springfield21),
    )
  }

  def resetContentTypeRepo(): mutable.Map[DepositId, Seq[ContentType]] = {
    contentTypeRepo.clear()
    contentTypeRepo ++= Map(
      depositId1 -> Seq(contentType10, contentType11),
      depositId2 -> Seq(contentType20),
      depositId3 -> Seq(contentType30),
      depositId4 -> Seq(contentType40),
      depositId5 -> Seq(contentType50),
    )
  }
}
