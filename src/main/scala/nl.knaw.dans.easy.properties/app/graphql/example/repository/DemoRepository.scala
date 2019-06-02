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

import nl.knaw.dans.easy.properties.app.graphql.DepositRepository
import nl.knaw.dans.easy.properties.app.model._
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, DepositContentTypeFilter, InputContentType }
import nl.knaw.dans.easy.properties.app.model.curator.{ Curator, DepositCuratorFilter, InputCurator }
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ DepositIngestStepFilter, IngestStep, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield }
import nl.knaw.dans.easy.properties.app.model.state.{ DepositStateFilter, InputState, State }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.collection.mutable

trait DemoRepository extends DepositRepository with DebugEnhancedLogging {

  val depositRepo: mutable.Map[DepositId, Deposit]
  val stateRepo: mutable.Map[DepositId, Seq[State]]
  val stepRepo: mutable.Map[DepositId, Seq[IngestStep]]
  val identifierRepo: mutable.Map[(DepositId, IdentifierType), Identifier]
  val doiRegisteredRepo: mutable.Map[DepositId, Seq[DoiRegisteredEvent]]
  val doiActionRepo: mutable.Map[DepositId, Seq[DoiActionEvent]]
  val curatorRepo: mutable.Map[DepositId, Seq[Curator]]
  val isNewVersionRepo: mutable.Map[DepositId, Seq[IsNewVersionEvent]]
  val curationRequiredRepo: mutable.Map[DepositId, Seq[CurationRequiredEvent]]
  val curationPerformedRepo: mutable.Map[DepositId, Seq[CurationPerformedEvent]]
  val springfieldRepo: mutable.Map[DepositId, Seq[Springfield]]
  val contentTypeRepo: mutable.Map[DepositId, Seq[ContentType]]

  override def getAllDeposits: Seq[Deposit] = {
    trace(())
    depositRepo.values.toSeq
  }

  override def getDeposits(depositorId: Option[DepositorId] = Option.empty,
                           stateFilter: Option[DepositStateFilter] = Option.empty,
                           ingestStepFilter: Option[DepositIngestStepFilter] = Option.empty,
                           doiRegisteredFilter: Option[DepositDoiRegisteredFilter] = Option.empty,
                           doiActionFilter: Option[DepositDoiActionFilter] = Option.empty,
                           curatorFilter: Option[DepositCuratorFilter] = Option.empty,
                           isNewVersionFilter: Option[DepositIsNewVersionFilter] = Option.empty,
                           curationRequiredFilter: Option[DepositCurationRequiredFilter] = Option.empty,
                           curationPerformedFilter: Option[DepositCurationPerformedFilter] = Option.empty,
                           contentTypeFilter: Option[DepositContentTypeFilter] = Option.empty,
                          ): Seq[Deposit] = {
    trace(depositorId, stateFilter, ingestStepFilter)

    val deposits = getAllDeposits

    val fromDepositor = depositorId match {
      case Some(depositor) => deposits.withFilter(_.depositorId == depositor)
      case None => deposits.withFilter(_ => true)
    }

    val withState = stateFilter match {
      case Some(DepositStateFilter(label, filter)) =>
        fromDepositor.withFilter(d => {
          val states = stateRepo.getOrElse(d.id, Seq.empty)
          val selectedStates = filter match {
            case SeriesFilter.LATEST => states.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => states
          }
          selectedStates.exists(_.label == label)
        })
      case None => fromDepositor
    }

    val withIngestStep = ingestStepFilter match {
      case Some(DepositIngestStepFilter(step, filter)) =>
        withState.withFilter(d => {
          val steps = stepRepo.getOrElse(d.id, Seq.empty)
          val selectedSteps = filter match {
            case SeriesFilter.LATEST => steps.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => steps
          }
          selectedSteps.exists(_.step == step)
        })
      case None => withState
    }

    val withDoiRegistered = doiRegisteredFilter match {
      case Some(DepositDoiRegisteredFilter(value, filter)) =>
        withIngestStep.withFilter(d => {
          val doiRegisteredEvents = doiRegisteredRepo.getOrElse(d.id, Seq.empty)
          val selectedEvents = filter match {
            case SeriesFilter.LATEST => doiRegisteredEvents.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => doiRegisteredEvents
          }
          selectedEvents.exists(_.value == value)
        })
      case None => withIngestStep
    }

    val withDoiAction = doiActionFilter match {
      case Some(DepositDoiActionFilter(value, filter)) =>
        withDoiRegistered.withFilter(d => {
          val doiActionEvents = doiActionRepo.getOrElse(d.id, Seq.empty)
          val selectedEvents = filter match {
            case SeriesFilter.LATEST => doiActionEvents.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => doiActionEvents
          }
          selectedEvents.exists(_.value == value)
        })
      case None => withDoiRegistered
    }

    val withCurator = curatorFilter match {
      case Some(DepositCuratorFilter(curator, filter)) =>
        withDoiAction.withFilter(d => {
          val curators = curatorRepo.getOrElse(d.id, Seq.empty)
          val selectedCurators = filter match {
            case SeriesFilter.LATEST => curators.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => curators
          }
          selectedCurators.exists(_.userId == curator)
        })
      case None => withDoiAction
    }

    val withIsNewVersion = isNewVersionFilter match {
      case Some(DepositIsNewVersionFilter(isNewVersion, filter)) =>
        withCurator.withFilter(d => {
          val inv = isNewVersionRepo.getOrElse(d.id, Seq.empty)
          val selectedIsNewVersion = filter match {
            case SeriesFilter.LATEST => inv.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => inv
          }
          selectedIsNewVersion.exists(_.isNewVersion == isNewVersion)
        })
      case None => withCurator
    }

    val withCurationRequired = curationRequiredFilter match {
      case Some(DepositCurationRequiredFilter(curationRequired, filter)) =>
        withIsNewVersion.withFilter(d => {
          val cr = curationRequiredRepo.getOrElse(d.id, Seq.empty)
          val selectedCurationRequired = filter match {
            case SeriesFilter.LATEST => cr.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => cr
          }
          selectedCurationRequired.exists(_.curationRequired == curationRequired)
        })
      case None => withIsNewVersion
    }

    val withCurationPerformed = curationPerformedFilter match {
      case Some(DepositCurationPerformedFilter(curationPerformed, filter)) =>
        withCurationRequired.withFilter(d => {
          val cp = curationPerformedRepo.getOrElse(d.id, Seq.empty)
          val selectedCurationPerformed = filter match {
            case SeriesFilter.LATEST => cp.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => cp
          }
          selectedCurationPerformed.exists(_.curationPerformed == curationPerformed)
        })
      case None => withCurationRequired
    }

    val withContentType = contentTypeFilter match {
      case Some(DepositContentTypeFilter(contentType, filter)) =>
        withCurationPerformed.withFilter(d => {
          val ct = contentTypeRepo.getOrElse(d.id, Seq.empty)
          val selectedContentType = filter match {
            case SeriesFilter.LATEST => ct.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => ct
          }
          selectedContentType.exists(_.value == contentType)
        })
      case None => withCurationPerformed
    }

    withContentType.map(identity)
  }

  override def getDeposit(id: DepositId): Option[Deposit] = {
    trace(id)
    depositRepo.get(id)
  }

  override def addDeposit(deposit: Deposit): Option[Deposit] = {
    trace(deposit)
    if (depositRepo contains deposit.id)
      Option.empty
    else {
      depositRepo += (deposit.id -> deposit)
      Option(deposit)
    }
  }

  //

  override def getStateById(id: String): Option[State] = {
    trace(id)
    stateRepo.values.toStream.flatten.find(_.id == id)
  }

  override def getCurrentState(id: DepositId): Option[State] = {
    trace(id)
    stateRepo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  override def getAllStates(id: DepositId): Seq[State] = {
    trace(id)
    stateRepo.getOrElse(id, Seq.empty)
  }

  override def getCurrentStates(ids: Seq[DepositId]): Seq[(DepositId, Option[State])] = {
    trace(ids)
    ids.map(id => id -> stateRepo.get(id).flatMap(_.maxByOption(_.timestamp)))
  }

  override def getAllStates(ids: Seq[DepositId]): Seq[(DepositId, Seq[State])] = {
    trace(ids)
    ids.map(id => id -> stateRepo.getOrElse(id, Seq.empty))
  }

  override def setState(id: DepositId, state: InputState): Option[State] = {
    trace(id, state)
    if (depositRepo contains id) {
      val InputState(label, description, timestamp) = state

      val stateId = id.toString.last + stateRepo
        .collectFirst { case (`id`, states) => states }
        .fold(0)(_.maxByOption(_.id).fold(0)(_.id.last.toInt + 1))
        .toString
      val newState = State(stateId, label, description, timestamp)

      if (stateRepo contains id)
        stateRepo.update(id, stateRepo(id) :+ newState)
      else
        stateRepo += (id -> Seq(newState))

      Some(newState)
    }
    else Option.empty
  }

  override def getDepositByStateId(id: String): Option[Deposit] = {
    trace(id)
    stateRepo
      .collectFirst { case (depositId, states) if states.exists(_.id == id) => depositId }
      .flatMap(depositRepo.get)
  }

  //

  override def getIngestStepById(id: String): Option[IngestStep] = {
    trace(id)
    stepRepo.values.toStream.flatten.find(_.id == id)
  }

  override def getCurrentIngestStep(id: DepositId): Option[IngestStep] = {
    trace(id)
    stepRepo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  override def getAllIngestSteps(id: DepositId): Seq[IngestStep] = {
    trace(id)
    stepRepo.getOrElse(id, Seq.empty)
  }

  override def getCurrentIngestSteps(ids: Seq[DepositId]): Seq[(DepositId, Option[IngestStep])] = {
    trace(ids)
    ids.map(id => id -> stepRepo.get(id).flatMap(_.maxByOption(_.timestamp)))
  }

  override def getAllIngestSteps(ids: Seq[DepositId]): Seq[(DepositId, Seq[IngestStep])] = {
    trace(ids)
    ids.map(id => id -> stepRepo.getOrElse(id, Seq.empty))
  }

  override def setIngestStep(id: DepositId, inputStep: InputIngestStep): Option[IngestStep] = {
    trace(id, inputStep)
    if (depositRepo contains id) {
      val InputIngestStep(step, timestamp) = inputStep

      val stepId = id.toString.last + stepRepo
        .collectFirst { case (`id`, steps) => steps }
        .fold(0)(_.maxByOption(_.id).fold(0)(_.id.last.toInt + 1))
        .toString
      val newStep = IngestStep(stepId, step, timestamp)

      if (stepRepo contains id)
        stepRepo.update(id, stepRepo(id) :+ newStep)
      else
        stepRepo += (id -> Seq(newStep))

      Some(newStep)
    }
    else Option.empty
  }

  override def getDepositByIngestStepId(id: String): Option[Deposit] = {
    trace(id)
    stepRepo
      .collectFirst { case (depositId, steps) if steps.exists(_.id == id) => depositId }
      .flatMap(depositRepo.get)
  }

  //

  override def getIdentifierById(id: String): Option[Identifier] = {
    trace(id)
    identifierRepo.values.toStream.find(_.id == id)
  }

  override def getIdentifier(id: DepositId, idType: IdentifierType): Option[Identifier] = {
    trace(id)
    identifierRepo.get(id -> idType)
  }

  override def getIdentifier(idType: IdentifierType, idValue: String): Option[Identifier] = {
    trace(idType, idValue)
    identifierRepo.values.toStream.find(identifier => identifier.idType == idType && identifier.idValue == idValue)
  }

  override def getIdentifiers(id: DepositId): Seq[Identifier] = {
    trace(id)
    identifierRepo.collect { case ((`id`, _), identifiers) => identifiers }.toSeq
  }

  override def getIdentifiersForTypes(ids: Seq[(DepositId, IdentifierType)]): Seq[((DepositId, IdentifierType), Option[Identifier])] = {
    trace(ids)
    ids.map {
      case key @ (depositId, idType) => key -> identifierRepo.get(depositId -> idType)
    }
  }

  override def getIdentifiers(ids: Seq[DepositId]): Seq[(DepositId, Seq[Identifier])] = {
    trace(ids)
    ids.map(depositId => depositId -> identifierRepo.collect { case ((`depositId`, _), identifier) => identifier }.toSeq)
  }

  override def addIdentifier(id: DepositId, identifier: InputIdentifier): Option[Identifier] = {
    trace(id, identifier)
    if (depositRepo contains id) {
      val InputIdentifier(idType, idValue, timestamp) = identifier

      val identifierId = id.toString.last + identifierRepo
        .collect { case ((`id`, _), ident) => ident }
        .maxByOption(_.id).fold(0)(_.id.last.toInt + 1)
        .toString
      val newIdentifier = Identifier(identifierId, idType, idValue, timestamp)

      if (identifierRepo contains(id, idType))
        Option.empty
      else {
        identifierRepo += ((id, idType) -> newIdentifier)
        Some(newIdentifier)
      }
    }
    else Option.empty
  }

  override def getDepositByIdentifierId(id: String): Option[Deposit] = {
    trace(id)
    identifierRepo
      .collectFirst { case ((depositId, _), identifier) if identifier.id == id => depositId }
      .flatMap(depositRepo.get)
  }

  //

  override def getCurrentDoiRegistered(id: DepositId): Option[DoiRegisteredEvent] = {
    trace(id)
    doiRegisteredRepo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  override def getCurrentDoisRegistered(ids: Seq[DepositId]): Seq[(DepositId, Option[DoiRegisteredEvent])] = {
    trace(ids)
    ids.map(id => id -> doiRegisteredRepo.get(id).flatMap(_.maxByOption(_.timestamp)))
  }

  override def getAllDoiRegistered(id: DepositId): Seq[DoiRegisteredEvent] = {
    trace(id)
    doiRegisteredRepo.getOrElse(id, Seq.empty)
  }

  override def getAllDoisRegistered(ids: Seq[DepositId]): Seq[(DepositId, Seq[DoiRegisteredEvent])] = {
    trace(ids)
    ids.map(id => id -> doiRegisteredRepo.getOrElse(id, Seq.empty))
  }

  override def setDoiRegistered(id: DepositId, registered: DoiRegisteredEvent): Option[DoiRegisteredEvent] = {
    trace(id, registered)
    if (depositRepo contains id) {
      if (doiRegisteredRepo contains id)
        doiRegisteredRepo.update(id, doiRegisteredRepo(id) :+ registered)
      else
        doiRegisteredRepo += (id -> Seq(registered))

      Some(registered)
    }
    else Option.empty
  }

  //

  override def getCurrentDoiAction(id: DepositId): Option[DoiActionEvent] = {
    trace(id)
    doiActionRepo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  override def getCurrentDoisAction(ids: Seq[DepositId]): Seq[(DepositId, Option[DoiActionEvent])] = {
    trace(ids)
    ids.map(id => id -> doiActionRepo.get(id).flatMap(_.maxByOption(_.timestamp)))
  }

  override def getAllDoiAction(id: DepositId): Seq[DoiActionEvent] = {
    trace(id)
    doiActionRepo.getOrElse(id, Seq.empty)
  }

  override def getAllDoisAction(ids: Seq[DepositId]): Seq[(DepositId, Seq[DoiActionEvent])] = {
    trace(ids)
    ids.map(id => id -> doiActionRepo.getOrElse(id, Seq.empty))
  }

  override def setDoiAction(id: DepositId, action: DoiActionEvent): Option[DoiActionEvent] = {
    trace(id, action)
    if (depositRepo contains id) {
      if (doiActionRepo contains id)
        doiActionRepo.update(id, doiActionRepo(id) :+ action)
      else
        doiActionRepo += (id -> Seq(action))

      Some(action)
    }
    else Option.empty
  }

  //

  override def getCuratorById(id: String): Option[Curator] = {
    trace(id)
    curatorRepo.values.toStream.flatten.find(_.id == id)
  }

  override def getCurrentCurator(id: DepositId): Option[Curator] = {
    trace(id)
    curatorRepo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  override def getAllCurators(id: DepositId): Seq[Curator] = {
    trace(id)
    curatorRepo.getOrElse(id, Seq.empty)
  }

  override def getCurrentCurators(ids: Seq[DepositId]): Seq[(DepositId, Option[Curator])] = {
    trace(ids)
    ids.map(id => id -> curatorRepo.get(id).flatMap(_.maxByOption(_.timestamp)))
  }

  override def getAllCurators(ids: Seq[DepositId]): Seq[(DepositId, Seq[Curator])] = {
    trace(ids)
    ids.map(id => id -> curatorRepo.getOrElse(id, Seq.empty))
  }

  override def setCurator(id: DepositId, curator: InputCurator): Option[Curator] = {
    trace(id, curator)
    if (depositRepo contains id) {
      val InputCurator(userId, email, timestamp) = curator

      val curatorId = id.toString.last + stepRepo
        .collectFirst { case (`id`, steps) => steps }
        .fold(0)(_.maxByOption(_.id).fold(0)(_.id.last.toInt + 1))
        .toString
      val newCurator = Curator(curatorId, userId, email, timestamp)

      if (curatorRepo contains id)
        curatorRepo.update(id, curatorRepo(id) :+ newCurator)
      else
        curatorRepo += (id -> Seq(newCurator))

      Some(newCurator)
    }
    else Option.empty
  }

  override def getDepositByCuratorId(id: String): Option[Deposit] = {
    trace(id)
    curatorRepo
      .collectFirst { case (depositId, curators) if curators.exists(_.id == id) => depositId }
      .flatMap(depositRepo.get)
  }

  //

  override def getCurrentIsNewVersionAction(id: DepositId): Option[IsNewVersionEvent] = {
    isNewVersionRepo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  override def getCurrentIsNewVersionActions(ids: Seq[DepositId]): Seq[(DepositId, Option[IsNewVersionEvent])] = {
    ids.map(id => id -> isNewVersionRepo.get(id).flatMap(_.maxByOption(_.timestamp)))
  }

  override def getAllIsNewVersionAction(id: DepositId): Seq[IsNewVersionEvent] = {
    isNewVersionRepo.getOrElse(id, Seq.empty)
  }

  override def getAllIsNewVersionActions(ids: Seq[DepositId]): Seq[(DepositId, Seq[IsNewVersionEvent])] = {
    ids.map(id => id -> isNewVersionRepo.getOrElse(id, Seq.empty))
  }

  override def setIsNewVersionAction(id: DepositId, action: IsNewVersionEvent): Option[IsNewVersionEvent] = {
    trace(id, action)
    if (depositRepo contains id) {
      if (isNewVersionRepo contains id)
        isNewVersionRepo.update(id, isNewVersionRepo(id) :+ action)
      else
        isNewVersionRepo += (id -> Seq(action))

      Some(action)
    }
    else Option.empty
  }

  //

  override def getCurrentCurationRequiredAction(id: DepositId): Option[CurationRequiredEvent] = {
    curationRequiredRepo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  override def getCurrentCurationRequiredActions(ids: Seq[DepositId]): Seq[(DepositId, Option[CurationRequiredEvent])] = {
    ids.map(id => id -> curationRequiredRepo.get(id).flatMap(_.maxByOption(_.timestamp)))
  }

  override def getAllCurationRequiredAction(id: DepositId): Seq[CurationRequiredEvent] = {
    curationRequiredRepo.getOrElse(id, Seq.empty)
  }

  override def getAllCurationRequiredActions(ids: Seq[DepositId]): Seq[(DepositId, Seq[CurationRequiredEvent])] = {
    ids.map(id => id -> curationRequiredRepo.getOrElse(id, Seq.empty))
  }

  override def setCurationRequiredAction(id: DepositId, action: CurationRequiredEvent): Option[CurationRequiredEvent] = {
    trace(id, action)
    if (depositRepo contains id) {
      if (curationRequiredRepo contains id)
        curationRequiredRepo.update(id, curationRequiredRepo(id) :+ action)
      else
        curationRequiredRepo += (id -> Seq(action))

      Some(action)
    }
    else Option.empty
  }

  //

  override def getCurrentCurationPerformedAction(id: DepositId): Option[CurationPerformedEvent] = {
    curationPerformedRepo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  override def getCurrentCurationPerformedActions(ids: Seq[DepositId]): Seq[(DepositId, Option[CurationPerformedEvent])] = {
    ids.map(id => id -> curationPerformedRepo.get(id).flatMap(_.maxByOption(_.timestamp)))
  }

  override def getAllCurationPerformedAction(id: DepositId): Seq[CurationPerformedEvent] = {
    curationPerformedRepo.getOrElse(id, Seq.empty)
  }

  override def getAllCurationPerformedActions(ids: Seq[DepositId]): Seq[(DepositId, Seq[CurationPerformedEvent])] = {
    ids.map(id => id -> curationPerformedRepo.getOrElse(id, Seq.empty))
  }

  override def setCurationPerformedAction(id: DepositId, action: CurationPerformedEvent): Option[CurationPerformedEvent] = {
    trace(id, action)
    if (depositRepo contains id) {
      if (curationPerformedRepo contains id)
        curationPerformedRepo.update(id, curationPerformedRepo(id) :+ action)
      else
        curationPerformedRepo += (id -> Seq(action))

      Some(action)
    }
    else Option.empty
  }

  //

  def getSpringfieldById(id: String): Option[Springfield] = {
    trace(id)
    springfieldRepo.values.toStream.flatten.find(_.id == id)
  }

  def getCurrentSpringfield(id: DepositId): Option[Springfield] = {
    trace(id)
    springfieldRepo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  def getCurrentSpringfields(ids: Seq[DepositId]): Seq[(DepositId, Option[Springfield])] = {
    trace(ids)
    ids.map(id => id -> springfieldRepo.get(id).flatMap(_.maxByOption(_.timestamp)))
  }

  def getAllSpringfields(id: DepositId): Seq[Springfield] = {
    trace(id)
    springfieldRepo.getOrElse(id, Seq.empty)
  }

  def getAllSpringfields(ids: Seq[DepositId]): Seq[(DepositId, Seq[Springfield])] = {
    trace(ids)
    ids.map(id => id -> springfieldRepo.getOrElse(id, Seq.empty))
  }

  def setSpringfield(id: DepositId, springfield: InputSpringfield): Option[Springfield] = {
    trace(id, springfield)
    if (depositRepo contains id) {
      val InputSpringfield(domain, user, collection, playmode, timestamp) = springfield

      val springfieldId = id.toString.last + stepRepo
        .collectFirst { case (`id`, steps) => steps }
        .fold(0)(_.maxByOption(_.id).fold(0)(_.id.last.toInt + 1))
        .toString
      val newSpringfield = Springfield(springfieldId, domain, user, collection, playmode, timestamp)

      if (springfieldRepo contains id)
        springfieldRepo.update(id, springfieldRepo(id) :+ newSpringfield)
      else
        springfieldRepo += (id -> Seq(newSpringfield))

      Some(newSpringfield)
    }
    else Option.empty
  }

  def getDepositBySpringfieldId(id: String): Option[Deposit] = {
    trace(id)
    springfieldRepo
      .collectFirst { case (depositId, springfields) if springfields.exists(_.id == id) => depositId }
      .flatMap(depositRepo.get)
  }

  //

  def getContentTypeById(id: String): Option[ContentType] = {
    trace(id)
    contentTypeRepo.values.toStream.flatten.find(_.id == id)
  }

  def getCurrentContentType(id: DepositId): Option[ContentType] = {
    trace(id)
    contentTypeRepo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  def getCurrentContentTypes(ids: Seq[DepositId]): Seq[(DepositId, Option[ContentType])] = {
    trace(ids)
    ids.map(id => id -> contentTypeRepo.get(id).flatMap(_.maxByOption(_.timestamp)))
  }

  def getAllContentTypes(id: DepositId): Seq[ContentType] = {
    trace(id)
    contentTypeRepo.getOrElse(id, Seq.empty)
  }

  def getAllContentTypes(ids: Seq[DepositId]): Seq[(DepositId, Seq[ContentType])] = {
    trace(ids)
    ids.map(id => id -> contentTypeRepo.getOrElse(id, Seq.empty))
  }

  def setContentType(id: DepositId, contentType: InputContentType): Option[ContentType] = {
    trace(id, contentType)
    if (depositRepo contains id) {
      val InputContentType(value, timestamp) = contentType

      val contentTypeId = id.toString.last + stepRepo
        .collectFirst { case (`id`, steps) => steps }
        .fold(0)(_.maxByOption(_.id).fold(0)(_.id.last.toInt + 1))
        .toString
      val newContentType = ContentType(contentTypeId, value, timestamp)

      if (contentTypeRepo contains id)
        contentTypeRepo.update(id, contentTypeRepo(id) :+ newContentType)
      else
        contentTypeRepo += (id -> Seq(newContentType))

      Some(newContentType)
    }
    else Option.empty
  }

  def getDepositByContentTypeId(id: String): Option[Deposit] = {
    trace(id)
    contentTypeRepo
      .collectFirst { case (depositId, contentTypes) if contentTypes.exists(_.id == id) => depositId }
      .flatMap(depositRepo.get)
  }
}
