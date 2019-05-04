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

  override def getDeposits(ids: Seq[DepositId]): Seq[(DepositId, Option[Deposit])] = {
    trace(ids)
    ids.map(id => id -> depositRepo.get(id))
  }

  override def getDeposit(id: DepositId, depositorId: DepositorId): Option[Deposit] = {
    trace(id, depositorId)
    depositRepo.get(id).filter(_.depositorId == depositorId)
  }

  override def getDepositsByDepositor(depositorId: DepositorId): Seq[Deposit] = {
    trace(depositorId)
    depositRepo.values.filter(_.depositorId == depositorId).toSeq
  }

  override def registerDeposit(deposit: Deposit): Option[Deposit] = {
    if (depositRepo contains deposit.id)
      Option.empty
    else {
      depositRepo += (deposit.id -> deposit)
      Option(deposit)
    }
  }

  override def getState(id: DepositId): Option[State] = {
    trace(id)
    stateRepo.get(id).map(_.maxBy(_.timestamp))
  }

  override def getStates(ids: Seq[DepositId]): Seq[(DepositId, Option[State])] = {
    ids.map(id => id -> stateRepo.get(id).map(_.maxBy(_.timestamp)))
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

  override def getDepositsByState(label: StateLabel): Seq[Deposit] = {
    trace(label)
    val depositsWithState = stateRepo.collect { case (depositId, states) if states.maxBy(_.timestamp).label == label => depositId }.toSeq
    getDeposits(depositsWithState).flatMap { case (_, deposit) => deposit }
  }

  override def getDepositsByDepositorAndState(depositorId: DepositorId, label: StateLabel): Seq[Deposit] = {
    trace(depositorId, label)

    val deposits = depositRepo.filter { case (_, deposit) => deposit.depositorId == depositorId }
    getStates(deposits.keys.toSeq)
      .collect { case (depositId, Some(State(`label`, _, _))) => deposits.get(depositId) }
      .flatten
  }
}
