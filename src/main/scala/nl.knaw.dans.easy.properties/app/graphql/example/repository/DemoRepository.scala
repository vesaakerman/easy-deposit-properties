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
import nl.knaw.dans.easy.properties.app.model.IngestStep.StepLabel.StepLabel
import nl.knaw.dans.easy.properties.app.model.State.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.collection.mutable

trait DemoRepository extends DepositRepository with DebugEnhancedLogging {

  val depositRepo: mutable.Map[DepositId, Deposit]
  val stateRepo: mutable.Map[DepositId, Seq[State]]
  val stepRepo: mutable.Map[DepositId, Seq[IngestStep]]

  override def getAllDeposits: Seq[Deposit] = {
    trace(())
    depositRepo.values.toSeq
  }

  override def getDeposit(id: DepositId): Option[Deposit] = {
    trace(id)
    depositRepo.get(id)
  }

  override def getDepositsByDepositor(depositorId: DepositorId): Seq[Deposit] = {
    trace(depositorId)
    depositRepo.values.filter(_.depositorId == depositorId).toSeq
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
      .collectFirst {
        case (depositId, states) if states.exists(_.id == id) => depositId
      }
      .flatMap(depositRepo.get)
  }

  override def getDepositsByCurrentState(label: StateLabel): Seq[Deposit] = {
    trace(label)
    stateRepo
      .collect {
        case (depositId, states) if states.maxByOption(_.timestamp).exists(_.label == label) => depositId
      }
      .toSeq
      .flatMap(depositRepo.get)
  }

  override def getDepositsByAllStates(label: StateLabel): Seq[Deposit] = {
    trace(label)
    stateRepo
      .collect {
        case (depositId, states) if states.exists(_.label == label) => depositId
      }
      .toSeq
      .flatMap(depositRepo.get)
  }

  override def getDepositsByDepositorAndCurrentState(depositorId: DepositorId, label: StateLabel): Seq[Deposit] = {
    trace(depositorId, label)

    val deposits = depositRepo.filter { case (_, deposit) => deposit.depositorId == depositorId }
    getCurrentStates(deposits.keys.toSeq)
      .collect { case (depositId, Some(State(_, `label`, _, _))) => deposits.get(depositId) }
      .flatten
  }

  override def getDepositsByDepositorAndAllStates(depositorId: DepositorId, label: StateLabel): Seq[Deposit] = {
    trace(depositorId, label)

    val deposits = depositRepo.filter { case (_, deposit) => deposit.depositorId == depositorId }
    getAllStates(deposits.keys.toSeq)
      .collect { case (depositId, states) if states.exists(_.label == label) => deposits.get(depositId) }
      .flatten
  }

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
      .collectFirst {
        case (depositId, steps) if steps.exists(_.id == id) => depositId
      }
      .flatMap(depositRepo.get)
  }

  override def getDepositsByCurrentIngestStep(label: StepLabel): Seq[Deposit] = {
    trace(label)
    stepRepo
      .collect {
        case (depositId, steps) if steps.maxByOption(_.timestamp).exists(_.step == label) => depositId
      }
      .toSeq
      .flatMap(depositRepo.get)
  }

  override def getDepositsByAllIngestSteps(label: StepLabel): Seq[Deposit] = {
    trace(label)
    stepRepo
      .collect {
        case (depositId, steps) if steps.exists(_.step == label) => depositId
      }
      .toSeq
      .flatMap(depositRepo.get)
  }
}
