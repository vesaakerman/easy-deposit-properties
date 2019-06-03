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
package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, InputContentType }
import nl.knaw.dans.easy.properties.app.model.curator.{ Curator, InputCurator }
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStep, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield }
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State }
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, Deposit, DepositFilter, DepositId, DoiActionEvent, DoiRegisteredEvent, IsNewVersionEvent, SeriesFilter, Timestamped, timestampOrdering }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import sangria.relay.Node

import scala.collection.generic.FilterMonadic
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

  override def getDeposits(filters: DepositFilters): Seq[Deposit] = {
    trace(filters)

    def filter[T <: Timestamped, F <: DepositFilter, V](collection: FilterMonadic[Deposit, Seq[Deposit]])
                                                       (filter: Option[F], repo: mutable.Map[DepositId, Seq[T]])
                                                       (get: F => V, label: T => V): FilterMonadic[Deposit, Seq[Deposit]] = {
      filter.fold(collection)(depositFilter => {
        collection.withFilter(d => {
          val ts = repo.getOrElse(d.id, Seq.empty)
          val selectedTs = depositFilter.filter match {
            case SeriesFilter.LATEST => ts.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => ts
          }
          selectedTs.exists(t => label(t) == get(depositFilter))
        })
      })
    }

    val DepositFilters(depositorId, stateFilter, ingestStepFilter, doiRegisteredFilter, doiActionFilter, curatorFilter, isNewVersionFilter, curationRequiredFilter, curationPerformedFilter, contentTypeFilter) = filters

    val deposits = getAllDeposits

    val fromDepositor = depositorId match {
      case Some(depositor) => deposits.withFilter(_.depositorId == depositor)
      case None => deposits.withFilter(_ => true)
    }

    val withState = filter(fromDepositor)(stateFilter, stateRepo)(_.label, _.label)
    val withIngestStep = filter(withState)(ingestStepFilter, stepRepo)(_.label, _.step)
    val withDoiRegistered = filter(withIngestStep)(doiRegisteredFilter, doiRegisteredRepo)(_.value, _.value)
    val withDoiAction = filter(withDoiRegistered)(doiActionFilter, doiActionRepo)(_.value, _.value)
    val withCurator = filter(withDoiAction)(curatorFilter, curatorRepo)(_.curator, _.userId)
    val withIsNewVersion = filter(withCurator)(isNewVersionFilter, isNewVersionRepo)(_.isNewVersion, _.isNewVersion)
    val withCurationRequired = filter(withIsNewVersion)(curationRequiredFilter, curationRequiredRepo)(_.curationRequired, _.curationRequired)
    val withCurationPerformed = filter(withCurationRequired)(curationPerformedFilter, curationPerformedRepo)(_.curationPerformed, _.curationPerformed)
    val withContentType = filter(withCurationPerformed)(contentTypeFilter, contentTypeRepo)(_.value, _.value)

    withContentType.map(identity)
  }

  override def getDepositsAggregated(filters: Seq[DepositFilters]): Seq[(DepositFilters, Seq[Deposit])] = {
    trace(filters)
    filters.map(filter => filter -> getDeposits(filter))
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

  private def getObjectById[T <: Node](id: String)(repo: mutable.Map[_, Seq[T]]): Option[T] = {
    repo.values.toStream.flatten.find(_.id == id)
  }

  private def getCurrentObject[T <: Timestamped](id: DepositId)(repo: mutable.Map[DepositId, Seq[T]]): Option[T] = {
    repo.get(id).flatMap(_.maxByOption(_.timestamp))
  }

  private def getCurrentObjects[T <: Timestamped](ids: Seq[DepositId])(repo: mutable.Map[DepositId, Seq[T]]): Seq[(DepositId, Option[T])] = {
    ids.map(id => id -> getCurrentObject(id)(repo))
  }

  private def getAllObjects[T](id: DepositId)(repo: mutable.Map[DepositId, Seq[T]]): Seq[T] = {
    repo.getOrElse(id, Seq.empty)
  }

  private def getAllObjects[T](ids: Seq[DepositId])(repo: mutable.Map[DepositId, Seq[T]]): Seq[(DepositId, Seq[T])] = {
    ids.map(id => id -> getAllObjects(id)(repo))
  }

  private def setter[I, O <: Node](id: DepositId, input: I)(repo: mutable.Map[DepositId, Seq[O]])(conversion: (String, I) => O): Option[O] = {
    if (depositRepo contains id) {
      val newId = id.toString.last + repo
        .collectFirst { case (`id`, os) => os }
        .fold(0)(_.maxByOption(_.id).fold(0)(_.id.last.toInt + 1))
        .toString
      val newObject = conversion(newId, input)

      if (repo contains id)
        repo.update(id, repo(id) :+ newObject)
      else
        repo += (id -> Seq(newObject))

      Option(newObject)
    }
    else Option.empty
  }

  private def setter[T](id: DepositId, t: T, repo: mutable.Map[DepositId, Seq[T]]): Option[T] = {
    if (depositRepo contains id) {
      if (repo contains id)
        repo.update(id, repo(id) :+ t)
      else
        repo += (id -> Seq(t))

      Some(t)
    }
    else Option.empty
  }

  private def getDepositByObjectId[T <: Node](id: String)(repo: mutable.Map[DepositId, Seq[T]]): Option[Deposit] = {
    repo
      .collectFirst { case (depositId, ts) if ts.exists(_.id == id) => depositId }
      .flatMap(depositRepo.get)
  }

  //

  override def getStateById(id: String): Option[State] = {
    trace(id)
    getObjectById(id)(stateRepo)
  }

  override def getCurrentState(id: DepositId): Option[State] = {
    trace(id)
    getCurrentObject(id)(stateRepo)
  }

  override def getAllStates(id: DepositId): Seq[State] = {
    trace(id)
    getAllObjects(id)(stateRepo)
  }

  override def getCurrentStates(ids: Seq[DepositId]): Seq[(DepositId, Option[State])] = {
    trace(ids)
    getCurrentObjects(ids)(stateRepo)
  }

  override def getAllStates(ids: Seq[DepositId]): Seq[(DepositId, Seq[State])] = {
    trace(ids)
    getAllObjects(ids)(stateRepo)
  }

  override def setState(id: DepositId, state: InputState): Option[State] = {
    trace(id, state)
    setter(id, state)(stateRepo) {
      case (stateId, InputState(label, description, timestamp)) => State(stateId, label, description, timestamp)
    }
  }

  override def getDepositByStateId(id: String): Option[Deposit] = {
    trace(id)
    getDepositByObjectId(id)(stateRepo)
  }

  //

  override def getIngestStepById(id: String): Option[IngestStep] = {
    trace(id)
    getObjectById(id)(stepRepo)
  }

  override def getCurrentIngestStep(id: DepositId): Option[IngestStep] = {
    trace(id)
    getCurrentObject(id)(stepRepo)
  }

  override def getAllIngestSteps(id: DepositId): Seq[IngestStep] = {
    trace(id)
    getAllObjects(id)(stepRepo)
  }

  override def getCurrentIngestSteps(ids: Seq[DepositId]): Seq[(DepositId, Option[IngestStep])] = {
    trace(ids)
    getCurrentObjects(ids)(stepRepo)
  }

  override def getAllIngestSteps(ids: Seq[DepositId]): Seq[(DepositId, Seq[IngestStep])] = {
    trace(ids)
    getAllObjects(ids)(stepRepo)
  }

  override def setIngestStep(id: DepositId, inputStep: InputIngestStep): Option[IngestStep] = {
    trace(id, inputStep)
    setter(id, inputStep)(stepRepo) {
      case (stepId, InputIngestStep(step, timestamp)) => IngestStep(stepId, step, timestamp)
    }
  }

  override def getDepositByIngestStepId(id: String): Option[Deposit] = {
    trace(id)
    getDepositByObjectId(id)(stepRepo)
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
    getCurrentObject(id)(doiRegisteredRepo)
  }

  override def getCurrentDoisRegistered(ids: Seq[DepositId]): Seq[(DepositId, Option[DoiRegisteredEvent])] = {
    trace(ids)
    getCurrentObjects(ids)(doiRegisteredRepo)
  }

  override def getAllDoiRegistered(id: DepositId): Seq[DoiRegisteredEvent] = {
    trace(id)
    getAllObjects(id)(doiRegisteredRepo)
  }

  override def getAllDoisRegistered(ids: Seq[DepositId]): Seq[(DepositId, Seq[DoiRegisteredEvent])] = {
    trace(ids)
    getAllObjects(ids)(doiRegisteredRepo)
  }

  override def setDoiRegistered(id: DepositId, registered: DoiRegisteredEvent): Option[DoiRegisteredEvent] = {
    trace(id, registered)
    setter(id, registered, doiRegisteredRepo)
  }

  //

  override def getCurrentDoiAction(id: DepositId): Option[DoiActionEvent] = {
    trace(id)
    getCurrentObject(id)(doiActionRepo)
  }

  override def getCurrentDoisAction(ids: Seq[DepositId]): Seq[(DepositId, Option[DoiActionEvent])] = {
    trace(ids)
    getCurrentObjects(ids)(doiActionRepo)
  }

  override def getAllDoiAction(id: DepositId): Seq[DoiActionEvent] = {
    trace(id)
    getAllObjects(id)(doiActionRepo)
  }

  override def getAllDoisAction(ids: Seq[DepositId]): Seq[(DepositId, Seq[DoiActionEvent])] = {
    trace(ids)
    getAllObjects(ids)(doiActionRepo)
  }

  override def setDoiAction(id: DepositId, action: DoiActionEvent): Option[DoiActionEvent] = {
    trace(id, action)
    setter(id, action, doiActionRepo)
  }

  //

  override def getCuratorById(id: String): Option[Curator] = {
    trace(id)
    getObjectById(id)(curatorRepo)
  }

  override def getCurrentCurator(id: DepositId): Option[Curator] = {
    trace(id)
    getCurrentObject(id)(curatorRepo)
  }

  override def getAllCurators(id: DepositId): Seq[Curator] = {
    trace(id)
    getAllObjects(id)(curatorRepo)
  }

  override def getCurrentCurators(ids: Seq[DepositId]): Seq[(DepositId, Option[Curator])] = {
    trace(ids)
    getCurrentObjects(ids)(curatorRepo)
  }

  override def getAllCurators(ids: Seq[DepositId]): Seq[(DepositId, Seq[Curator])] = {
    trace(ids)
    getAllObjects(ids)(curatorRepo)
  }

  override def setCurator(id: DepositId, curator: InputCurator): Option[Curator] = {
    trace(id, curator)
    setter(id, curator)(curatorRepo) {
      case (curatorId, InputCurator(userId, email, timestamp)) => Curator(curatorId, userId, email, timestamp)
    }
  }

  override def getDepositByCuratorId(id: String): Option[Deposit] = {
    trace(id)
    getDepositByObjectId(id)(curatorRepo)
  }

  //

  override def getCurrentIsNewVersionAction(id: DepositId): Option[IsNewVersionEvent] = {
    getCurrentObject(id)(isNewVersionRepo)
  }

  override def getCurrentIsNewVersionActions(ids: Seq[DepositId]): Seq[(DepositId, Option[IsNewVersionEvent])] = {
    getCurrentObjects(ids)(isNewVersionRepo)
  }

  override def getAllIsNewVersionAction(id: DepositId): Seq[IsNewVersionEvent] = {
    getAllObjects(id)(isNewVersionRepo)
  }

  override def getAllIsNewVersionActions(ids: Seq[DepositId]): Seq[(DepositId, Seq[IsNewVersionEvent])] = {
    getAllObjects(ids)(isNewVersionRepo)
  }

  override def setIsNewVersionAction(id: DepositId, action: IsNewVersionEvent): Option[IsNewVersionEvent] = {
    trace(id, action)
    setter(id, action, isNewVersionRepo)
  }

  //

  override def getCurrentCurationRequiredAction(id: DepositId): Option[CurationRequiredEvent] = {
    getCurrentObject(id)(curationRequiredRepo)
  }

  override def getCurrentCurationRequiredActions(ids: Seq[DepositId]): Seq[(DepositId, Option[CurationRequiredEvent])] = {
    getCurrentObjects(ids)(curationRequiredRepo)
  }

  override def getAllCurationRequiredAction(id: DepositId): Seq[CurationRequiredEvent] = {
    getAllObjects(id)(curationRequiredRepo)
  }

  override def getAllCurationRequiredActions(ids: Seq[DepositId]): Seq[(DepositId, Seq[CurationRequiredEvent])] = {
    getAllObjects(ids)(curationRequiredRepo)
  }

  override def setCurationRequiredAction(id: DepositId, action: CurationRequiredEvent): Option[CurationRequiredEvent] = {
    trace(id, action)
    setter(id, action, curationRequiredRepo)
  }

  //

  override def getCurrentCurationPerformedAction(id: DepositId): Option[CurationPerformedEvent] = {
    getCurrentObject(id)(curationPerformedRepo)
  }

  override def getCurrentCurationPerformedActions(ids: Seq[DepositId]): Seq[(DepositId, Option[CurationPerformedEvent])] = {
    getCurrentObjects(ids)(curationPerformedRepo)
  }

  override def getAllCurationPerformedAction(id: DepositId): Seq[CurationPerformedEvent] = {
    getAllObjects(id)(curationPerformedRepo)
  }

  override def getAllCurationPerformedActions(ids: Seq[DepositId]): Seq[(DepositId, Seq[CurationPerformedEvent])] = {
    getAllObjects(ids)(curationPerformedRepo)
  }

  override def setCurationPerformedAction(id: DepositId, action: CurationPerformedEvent): Option[CurationPerformedEvent] = {
    trace(id, action)
    setter(id, action, curationPerformedRepo)
  }

  //

  def getSpringfieldById(id: String): Option[Springfield] = {
    trace(id)
    getObjectById(id)(springfieldRepo)
  }

  def getCurrentSpringfield(id: DepositId): Option[Springfield] = {
    trace(id)
    getCurrentObject(id)(springfieldRepo)
  }

  def getCurrentSpringfields(ids: Seq[DepositId]): Seq[(DepositId, Option[Springfield])] = {
    trace(ids)
    getCurrentObjects(ids)(springfieldRepo)
  }

  def getAllSpringfields(id: DepositId): Seq[Springfield] = {
    trace(id)
    getAllObjects(id)(springfieldRepo)
  }

  def getAllSpringfields(ids: Seq[DepositId]): Seq[(DepositId, Seq[Springfield])] = {
    trace(ids)
    getAllObjects(ids)(springfieldRepo)
  }

  def setSpringfield(id: DepositId, springfield: InputSpringfield): Option[Springfield] = {
    trace(id, springfield)
    setter(id, springfield)(springfieldRepo) {
      case (springfieldId, InputSpringfield(domain, user, collection, playmode, timestamp)) => Springfield(springfieldId, domain, user, collection, playmode, timestamp)
    }
  }

  def getDepositBySpringfieldId(id: String): Option[Deposit] = {
    trace(id)
    getDepositByObjectId(id)(springfieldRepo)
  }

  //

  def getContentTypeById(id: String): Option[ContentType] = {
    trace(id)
    getObjectById(id)(contentTypeRepo)
  }

  def getCurrentContentType(id: DepositId): Option[ContentType] = {
    trace(id)
    getCurrentObject(id)(contentTypeRepo)
  }

  def getCurrentContentTypes(ids: Seq[DepositId]): Seq[(DepositId, Option[ContentType])] = {
    trace(ids)
    getCurrentObjects(ids)(contentTypeRepo)
  }

  def getAllContentTypes(id: DepositId): Seq[ContentType] = {
    trace(id)
    getAllObjects(id)(contentTypeRepo)
  }

  def getAllContentTypes(ids: Seq[DepositId]): Seq[(DepositId, Seq[ContentType])] = {
    trace(ids)
    getAllObjects(ids)(contentTypeRepo)
  }

  def setContentType(id: DepositId, contentType: InputContentType): Option[ContentType] = {
    trace(id, contentType)
    setter(id, contentType)(contentTypeRepo) {
      case (contentTypeId, InputContentType(value, timestamp)) => ContentType(contentTypeId, value, timestamp)
    }
  }

  def getDepositByContentTypeId(id: String): Option[Deposit] = {
    trace(id)
    getDepositByObjectId(id)(contentTypeRepo)
  }
}
