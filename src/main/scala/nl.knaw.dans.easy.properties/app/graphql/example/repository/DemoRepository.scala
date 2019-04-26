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

import java.util.UUID

import nl.knaw.dans.easy.properties.app.graphql.DepositRepository
import nl.knaw.dans.easy.properties.app.model.State.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DepositorId, State }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.collection.mutable

trait DemoRepository extends DepositRepository with DebugEnhancedLogging {

  val depositRepo: mutable.Map[DepositId, Deposit]

  val stateRepo: mutable.Map[DepositId, State]

  def getAllDeposits: Seq[Deposit] = {
    trace(())
    depositRepo.values.toSeq
  }

  def getDeposit(id: UUID): Option[Deposit] = {
    trace(id)
    depositRepo.get(id)
  }

  def getDepositByUserId(depositorId: DepositorId): Seq[Deposit] = {
    trace(depositorId)
    depositRepo.values.filter(_.depositorId == depositorId).toSeq
  }

  def registerDeposit(deposit: Deposit): Option[Deposit] = {
    if (depositRepo contains deposit.id)
      Option.empty
    else {
      depositRepo += (deposit.id -> deposit)
      Option(deposit)
    }
  }

  def getState(id: UUID): Option[State] = {
    trace(id)
    stateRepo.get(id)
  }

  def setState(id: UUID, state: State): Option[Deposit] = {
    if (depositRepo contains id) {
      if (stateRepo contains id)
        stateRepo.update(id, state)
      else
        stateRepo += (id -> state)

      depositRepo.get(id)
    }
    else Option.empty
  }

  def getDepositByState(label: StateLabel): Seq[Deposit] = {
    trace(label)
    for {
      (depositId, State(stateLabel, _)) <- stateRepo.toSeq
      if stateLabel == label
      deposit <- depositRepo.get(depositId)
      _ = debug(deposit.id.toString)
    } yield deposit
  }
}
