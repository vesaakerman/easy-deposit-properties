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
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ DepositIngestStepFilter, IngestStep, IngestStepFilter, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.state.{ DepositStateFilter, InputState, State, StateFilter }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.collection.mutable

trait DemoRepository extends DepositRepository with DebugEnhancedLogging {

  val depositRepo: mutable.Map[DepositId, Deposit]
  val stateRepo: mutable.Map[DepositId, Seq[State]]
  val stepRepo: mutable.Map[DepositId, Seq[IngestStep]]
  val identifierRepo: mutable.Map[(DepositId, IdentifierType), Identifier]
  val doiRegisteredRepo: mutable.Map[DepositId, Seq[DoiRegisteredEvent]]
  val doiActionRepo: mutable.Map[DepositId, Seq[DoiActionEvent]]

  override def getAllDeposits: Seq[Deposit] = {
    trace(())
    depositRepo.values.toSeq
  }

  def getDeposits(depositorId: Option[DepositorId] = Option.empty,
                  stateFilter: Option[DepositStateFilter] = Option.empty,
                  ingestStepFilter: Option[DepositIngestStepFilter] = Option.empty,
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
            case StateFilter.LATEST => states.maxByOption(_.timestamp).toSeq
            case StateFilter.ALL => states
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
            case IngestStepFilter.LATEST => steps.maxByOption(_.timestamp).toSeq
            case IngestStepFilter.ALL => steps
          }
          selectedSteps.exists(_.step == step)
        })
      case None => withState
    }

    withIngestStep.map(identity)
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
}
