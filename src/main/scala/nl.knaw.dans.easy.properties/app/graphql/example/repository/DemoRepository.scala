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
import nl.knaw.dans.easy.properties.app.model.State.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.collection.mutable

trait DemoRepository extends DepositRepository with DebugEnhancedLogging {

  val depositRepo: mutable.Map[DepositId, Deposit]

  val stateRepo: mutable.Map[DepositId, Seq[State]]

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
    if (depositRepo contains deposit.id)
      Option.empty
    else {
      depositRepo += (deposit.id -> deposit)
      Option(deposit)
    }
  }

  override def getCurrentState(id: DepositId): Option[State] = {
    trace(id)
    stateRepo.get(id).map(_.maxBy(_.timestamp))
  }

  override def getAllStates(id: DepositId): Seq[State] = {
    trace(id)
    stateRepo.getOrElse(id, Seq.empty)
  }

  override def getCurrentStates(ids: Seq[DepositId]): Seq[(DepositId, Option[State])] = {
    trace(ids)
    ids.map(id => id -> stateRepo.get(id).map(_.maxBy(_.timestamp)))
  }

  override def getAllStates(ids: Seq[DepositId]): Seq[(DepositId, Seq[State])] = {
    trace(ids)
    ids.map(id => id -> stateRepo.getOrElse(id, Seq.empty))
  }

  override def setState(id: DepositId, state: State): Option[Deposit] = {
    if (depositRepo contains id) {
      if (stateRepo contains id)
        stateRepo.update(id, stateRepo(id) :+ state)
      else
        stateRepo += (id -> Seq(state))

      depositRepo.get(id)
    }
    else Option.empty
  }

  override def getDepositsByCurrentState(label: StateLabel): Seq[Deposit] = {
    trace(label)
    stateRepo
      .collect {
        case (depositId, states) if states.maxBy(_.timestamp).label == label => depositId
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
      .collect { case (depositId, Some(State(`label`, _, _))) => deposits.get(depositId) }
      .flatten
  }

  override def getDepositsByDepositorAndAllStates(depositorId: DepositorId, label: StateLabel): Seq[Deposit] = {
    trace(depositorId, label)

    val deposits = depositRepo.filter { case (_, deposit) => deposit.depositorId == depositorId }
    getAllStates(deposits.keys.toSeq)
      .collect { case (depositId, states) if states.exists(_.label == label) => deposits.get(depositId) }
      .flatten
  }
}
