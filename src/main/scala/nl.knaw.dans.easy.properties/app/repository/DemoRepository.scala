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

import cats.instances.either._
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
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

  override def getAllDeposits: QueryErrorOr[Seq[Deposit]] = {
    trace(())
    depositRepo.values.toSeq.asRight
  }

  override def getDeposits(filters: DepositFilters): QueryErrorOr[Seq[Deposit]] = {
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

    getAllDeposits
      .map(deposits => {
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
      })
  }

  override def getDepositsAggregated(filters: Seq[DepositFilters]): QueryErrorOr[Seq[(DepositFilters, Seq[Deposit])]] = {
    trace(filters)

    filters.toList.traverse[QueryErrorOr, (DepositFilters, Seq[Deposit])](filter => {
      getDeposits(filter).map(filter -> _)
    })
  }

  override def getDeposit(id: DepositId): QueryErrorOr[Deposit] = {
    trace(id)
    depositRepo.get(id)
      .map(_.asRight)
      .getOrElse(DepositDoesNotExistError(id).asLeft)
  }

  override def addDeposit(deposit: Deposit): MutationErrorOr[Deposit] = {
    trace(deposit)
    if (depositRepo contains deposit.id)
      DepositAlreadyExistsError(deposit.id).asLeft
    else {
      depositRepo += (deposit.id -> deposit)
      deposit.asRight
    }
  }

  private def getObjectById[T <: Node](id: String)(repo: mutable.Map[_, Seq[T]]): QueryErrorOr[Option[T]] = {
    repo.values.toStream.flatten.find(_.id == id).asRight
  }

  private def getCurrentObject[T <: Timestamped](id: DepositId)(repo: mutable.Map[DepositId, Seq[T]]): QueryErrorOr[Option[T]] = {
    repo.get(id).flatMap(_.maxByOption(_.timestamp)).asRight
  }

  private def getCurrentObjects[T <: Timestamped](ids: Seq[DepositId])(repo: mutable.Map[DepositId, Seq[T]]): QueryErrorOr[Seq[(DepositId, Option[T])]] = {
    ids.toList.traverse[QueryErrorOr, (DepositId, Option[T])](id => getCurrentObject(id)(repo).map(id -> _))
  }

  private def getAllObjects[T](id: DepositId)(repo: mutable.Map[DepositId, Seq[T]]): QueryErrorOr[Seq[T]] = {
    repo.getOrElse(id, Seq.empty).asRight
  }

  private def getAllObjects[T](ids: Seq[DepositId])(repo: mutable.Map[DepositId, Seq[T]]): QueryErrorOr[Seq[(DepositId, Seq[T])]] = {
    ids.toList.traverse[QueryErrorOr, (DepositId, Seq[T])](id => getAllObjects(id)(repo).map(id -> _))
  }

  private def setter[I, O <: Node](id: DepositId, input: I)(repo: mutable.Map[DepositId, Seq[O]])(conversion: (String, I) => O): MutationErrorOr[O] = {
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

      newObject.asRight
    }
    else NoSuchDepositError(id).asLeft
  }

  private def setter[T](id: DepositId, t: T, repo: mutable.Map[DepositId, Seq[T]]): MutationErrorOr[T] = {
    if (depositRepo contains id) {
      if (repo contains id)
        repo.update(id, repo(id) :+ t)
      else
        repo += (id -> Seq(t))

      t.asRight
    }
    else NoSuchDepositError(id).asLeft
  }

  private def getDepositByObjectId[T <: Node](id: String)(repo: mutable.Map[DepositId, Seq[T]]): QueryErrorOr[Option[Deposit]] = {
    repo
      .collectFirst { case (depositId, ts) if ts.exists(_.id == id) => depositId }
      .flatMap(depositRepo.get)
      .asRight
  }

  //

  override def getStateById(id: String): QueryErrorOr[Option[State]] = {
    trace(id)
    getObjectById(id)(stateRepo)
  }

  override def getCurrentState(id: DepositId): QueryErrorOr[Option[State]] = {
    trace(id)
    getCurrentObject(id)(stateRepo)
  }

  override def getAllStates(id: DepositId): QueryErrorOr[Seq[State]] = {
    trace(id)
    getAllObjects(id)(stateRepo)
  }

  override def getCurrentStates(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[State])]] = {
    trace(ids)
    getCurrentObjects(ids)(stateRepo)
  }

  override def getAllStates(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[State])]] = {
    trace(ids)
    getAllObjects(ids)(stateRepo)
  }

  override def setState(id: DepositId, state: InputState): MutationErrorOr[State] = {
    trace(id, state)
    setter(id, state)(stateRepo) {
      case (stateId, InputState(label, description, timestamp)) => State(stateId, label, description, timestamp)
    }
  }

  override def getDepositByStateId(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    getDepositByObjectId(id)(stateRepo)
  }

  //

  override def getIngestStepById(id: String): QueryErrorOr[Option[IngestStep]] = {
    trace(id)
    getObjectById(id)(stepRepo)
  }

  override def getCurrentIngestStep(id: DepositId): QueryErrorOr[Option[IngestStep]] = {
    trace(id)
    getCurrentObject(id)(stepRepo)
  }

  override def getAllIngestSteps(id: DepositId): QueryErrorOr[Seq[IngestStep]] = {
    trace(id)
    getAllObjects(id)(stepRepo)
  }

  override def getCurrentIngestSteps(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[IngestStep])]] = {
    trace(ids)
    getCurrentObjects(ids)(stepRepo)
  }

  override def getAllIngestSteps(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[IngestStep])]] = {
    trace(ids)
    getAllObjects(ids)(stepRepo)
  }

  override def setIngestStep(id: DepositId, inputStep: InputIngestStep): MutationErrorOr[IngestStep] = {
    trace(id, inputStep)
    setter(id, inputStep)(stepRepo) {
      case (stepId, InputIngestStep(step, timestamp)) => IngestStep(stepId, step, timestamp)
    }
  }

  override def getDepositByIngestStepId(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    getDepositByObjectId(id)(stepRepo)
  }

  //

  override def getIdentifierById(id: String): QueryErrorOr[Option[Identifier]] = {
    trace(id)
    identifierRepo.values.toStream.find(_.id == id).asRight
  }

  override def getIdentifier(id: DepositId, idType: IdentifierType): QueryErrorOr[Option[Identifier]] = {
    trace(id)
    identifierRepo.get(id -> idType).asRight
  }

  override def getIdentifier(idType: IdentifierType, idValue: String): QueryErrorOr[Option[Identifier]] = {
    trace(idType, idValue)
    identifierRepo.values.toStream.find(identifier => identifier.idType == idType && identifier.idValue == idValue).asRight
  }

  override def getIdentifiers(id: DepositId): QueryErrorOr[Seq[Identifier]] = {
    trace(id)
    identifierRepo.find { case ((depId, _), _) => depId == id }
      .map(_ => identifierRepo.collect { case ((`id`, _), identifiers) => identifiers }.toSeq)
      .getOrElse(Seq.empty)
      .asRight
  }

  override def getIdentifiersForTypes(ids: Seq[(DepositId, IdentifierType)]): QueryErrorOr[Seq[((DepositId, IdentifierType), Option[Identifier])]] = {
    trace(ids)
    ids.map {
      case key @ (depositId, idType) => key -> identifierRepo.get(depositId -> idType)
    }.asRight
  }

  override def getIdentifiers(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Identifier])]] = {
    trace(ids)
    ids.map(depositId => depositId ->
      identifierRepo.find { case ((depId, _), _) => depId == depositId }
        .map(_ => identifierRepo.collect { case ((`depositId`, _), identifiers) => identifiers }.toSeq).getOrElse(Seq.empty))
      .asRight
  }

  override def addIdentifier(id: DepositId, identifier: InputIdentifier): MutationErrorOr[Identifier] = {
    trace(id, identifier)
    if (depositRepo contains id) {
      val InputIdentifier(idType, idValue, timestamp) = identifier

      val identifierId = id.toString.last + identifierRepo
        .collect { case ((`id`, _), ident) => ident }
        .maxByOption(_.id).fold(0)(_.id.last.toInt + 1)
        .toString
      val newIdentifier = Identifier(identifierId, idType, idValue, timestamp)

      if (identifierRepo contains(id, idType))
        IdentifierAlreadyExistsError(id, idType).asLeft
      else {
        identifierRepo += ((id, idType) -> newIdentifier)
        newIdentifier.asRight
      }
    }
    else NoSuchDepositError(id).asLeft
  }

  override def getDepositByIdentifierId(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    identifierRepo
      .collectFirst { case ((depositId, _), identifier) if identifier.id == id => depositId }
      .flatMap(depositRepo.get)
      .asRight
  }

  //

  override def getCurrentDoiRegistered(id: DepositId): QueryErrorOr[Option[DoiRegisteredEvent]] = {
    trace(id)
    getCurrentObject(id)(doiRegisteredRepo)
  }

  override def getCurrentDoisRegistered(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiRegisteredEvent])]] = {
    trace(ids)
    getCurrentObjects(ids)(doiRegisteredRepo)
  }

  override def getAllDoiRegistered(id: DepositId): QueryErrorOr[Seq[DoiRegisteredEvent]] = {
    trace(id)
    getAllObjects(id)(doiRegisteredRepo)
  }

  override def getAllDoisRegistered(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[DoiRegisteredEvent])]] = {
    trace(ids)
    getAllObjects(ids)(doiRegisteredRepo)
  }

  override def setDoiRegistered(id: DepositId, registered: DoiRegisteredEvent): MutationErrorOr[DoiRegisteredEvent] = {
    trace(id, registered)
    setter(id, registered, doiRegisteredRepo)
  }

  //

  override def getCurrentDoiAction(id: DepositId): QueryErrorOr[Option[DoiActionEvent]] = {
    trace(id)
    getCurrentObject(id)(doiActionRepo)
  }

  override def getCurrentDoisAction(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiActionEvent])]] = {
    trace(ids)
    getCurrentObjects(ids)(doiActionRepo)
  }

  override def getAllDoiAction(id: DepositId): QueryErrorOr[Seq[DoiActionEvent]] = {
    trace(id)
    getAllObjects(id)(doiActionRepo)
  }

  override def getAllDoisAction(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[DoiActionEvent])]] = {
    trace(ids)
    getAllObjects(ids)(doiActionRepo)
  }

  override def setDoiAction(id: DepositId, action: DoiActionEvent): MutationErrorOr[DoiActionEvent] = {
    trace(id, action)
    setter(id, action, doiActionRepo)
  }

  //

  override def getCuratorById(id: String): QueryErrorOr[Option[Curator]] = {
    trace(id)
    getObjectById(id)(curatorRepo)
  }

  override def getCurrentCurator(id: DepositId): QueryErrorOr[Option[Curator]] = {
    trace(id)
    getCurrentObject(id)(curatorRepo)
  }

  override def getAllCurators(id: DepositId): QueryErrorOr[Seq[Curator]] = {
    trace(id)
    getAllObjects(id)(curatorRepo)
  }

  override def getCurrentCurators(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Curator])]] = {
    trace(ids)
    getCurrentObjects(ids)(curatorRepo)
  }

  override def getAllCurators(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Curator])]] = {
    trace(ids)
    getAllObjects(ids)(curatorRepo)
  }

  override def setCurator(id: DepositId, curator: InputCurator): MutationErrorOr[Curator] = {
    trace(id, curator)
    setter(id, curator)(curatorRepo) {
      case (curatorId, InputCurator(userId, email, timestamp)) => Curator(curatorId, userId, email, timestamp)
    }
  }

  override def getDepositByCuratorId(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    getDepositByObjectId(id)(curatorRepo)
  }

  //

  override def getCurrentIsNewVersionAction(id: DepositId): QueryErrorOr[Option[IsNewVersionEvent]] = {
    getCurrentObject(id)(isNewVersionRepo)
  }

  override def getCurrentIsNewVersionActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[IsNewVersionEvent])]] = {
    getCurrentObjects(ids)(isNewVersionRepo)
  }

  override def getAllIsNewVersionAction(id: DepositId): QueryErrorOr[Seq[IsNewVersionEvent]] = {
    getAllObjects(id)(isNewVersionRepo)
  }

  override def getAllIsNewVersionActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[IsNewVersionEvent])]] = {
    getAllObjects(ids)(isNewVersionRepo)
  }

  override def setIsNewVersionAction(id: DepositId, action: IsNewVersionEvent): MutationErrorOr[IsNewVersionEvent] = {
    trace(id, action)
    setter(id, action, isNewVersionRepo)
  }

  //

  override def getCurrentCurationRequiredAction(id: DepositId): QueryErrorOr[Option[CurationRequiredEvent]] = {
    getCurrentObject(id)(curationRequiredRepo)
  }

  override def getCurrentCurationRequiredActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[CurationRequiredEvent])]] = {
    getCurrentObjects(ids)(curationRequiredRepo)
  }

  override def getAllCurationRequiredAction(id: DepositId): QueryErrorOr[Seq[CurationRequiredEvent]] = {
    getAllObjects(id)(curationRequiredRepo)
  }

  override def getAllCurationRequiredActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[CurationRequiredEvent])]] = {
    getAllObjects(ids)(curationRequiredRepo)
  }

  override def setCurationRequiredAction(id: DepositId, action: CurationRequiredEvent): MutationErrorOr[CurationRequiredEvent] = {
    trace(id, action)
    setter(id, action, curationRequiredRepo)
  }

  //

  override def getCurrentCurationPerformedAction(id: DepositId): QueryErrorOr[Option[CurationPerformedEvent]] = {
    getCurrentObject(id)(curationPerformedRepo)
  }

  override def getCurrentCurationPerformedActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[CurationPerformedEvent])]] = {
    getCurrentObjects(ids)(curationPerformedRepo)
  }

  override def getAllCurationPerformedAction(id: DepositId): QueryErrorOr[Seq[CurationPerformedEvent]] = {
    getAllObjects(id)(curationPerformedRepo)
  }

  override def getAllCurationPerformedActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[CurationPerformedEvent])]] = {
    getAllObjects(ids)(curationPerformedRepo)
  }

  override def setCurationPerformedAction(id: DepositId, action: CurationPerformedEvent): MutationErrorOr[CurationPerformedEvent] = {
    trace(id, action)
    setter(id, action, curationPerformedRepo)
  }

  //

  def getSpringfieldById(id: String): QueryErrorOr[Option[Springfield]] = {
    trace(id)
    getObjectById(id)(springfieldRepo)
  }

  def getCurrentSpringfield(id: DepositId): QueryErrorOr[Option[Springfield]] = {
    trace(id)
    getCurrentObject(id)(springfieldRepo)
  }

  def getCurrentSpringfields(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Springfield])]] = {
    trace(ids)
    getCurrentObjects(ids)(springfieldRepo)
  }

  def getAllSpringfields(id: DepositId): QueryErrorOr[Seq[Springfield]] = {
    trace(id)
    getAllObjects(id)(springfieldRepo)
  }

  def getAllSpringfields(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Springfield])]] = {
    trace(ids)
    getAllObjects(ids)(springfieldRepo)
  }

  def setSpringfield(id: DepositId, springfield: InputSpringfield): MutationErrorOr[Springfield] = {
    trace(id, springfield)
    setter(id, springfield)(springfieldRepo) {
      case (springfieldId, InputSpringfield(domain, user, collection, playmode, timestamp)) => Springfield(springfieldId, domain, user, collection, playmode, timestamp)
    }
  }

  def getDepositBySpringfieldId(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    getDepositByObjectId(id)(springfieldRepo)
  }

  //

  def getContentTypeById(id: String): QueryErrorOr[Option[ContentType]] = {
    trace(id)
    getObjectById(id)(contentTypeRepo)
  }

  def getCurrentContentType(id: DepositId): QueryErrorOr[Option[ContentType]] = {
    trace(id)
    getCurrentObject(id)(contentTypeRepo)
  }

  def getCurrentContentTypes(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[ContentType])]] = {
    trace(ids)
    getCurrentObjects(ids)(contentTypeRepo)
  }

  def getAllContentTypes(id: DepositId): QueryErrorOr[Seq[ContentType]] = {
    trace(id)
    getAllObjects(id)(contentTypeRepo)
  }

  def getAllContentTypes(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[ContentType])]] = {
    trace(ids)
    getAllObjects(ids)(contentTypeRepo)
  }

  def setContentType(id: DepositId, contentType: InputContentType): MutationErrorOr[ContentType] = {
    trace(id, contentType)
    setter(id, contentType)(contentTypeRepo) {
      case (contentTypeId, InputContentType(value, timestamp)) => ContentType(contentTypeId, value, timestamp)
    }
  }

  def getDepositByContentTypeId(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    getDepositByObjectId(id)(contentTypeRepo)
  }
}
