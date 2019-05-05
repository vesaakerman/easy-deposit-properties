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
package nl.knaw.dans.easy.properties.app.graphql

import nl.knaw.dans.easy.properties.app.model.State.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DepositorId, State }

trait DepositRepository {

  def getAllDeposits: Seq[Deposit]

  def getDeposit(id: DepositId): Option[Deposit]

  def getDeposits(ids: Seq[DepositId]): Seq[(DepositId, Option[Deposit])]

  def getDeposit(id: DepositId, depositorId: DepositorId): Option[Deposit]

  def getDepositsByDepositor(depositorId: DepositorId): Seq[Deposit]

  def registerDeposit(deposit: Deposit): Option[Deposit]

  def getCurrentState(id: DepositId): Option[State]

  def getAllStates(id: DepositId): Seq[State]

  def getCurrentStates(ids: Seq[DepositId]): Seq[(DepositId, Option[State])]

  def getAllStates(ids: Seq[DepositId]): Seq[(DepositId, Seq[State])]

  def setState(id: DepositId, state: State): Option[Deposit]

  def getDepositsByCurrentState(state: StateLabel): Seq[Deposit]

  def getDepositsByAllStates(state: StateLabel): Seq[Deposit]

  def getDepositsByDepositorAndCurrentState(depositorId: DepositorId, state: StateLabel): Seq[Deposit]

  def getDepositsByDepositorAndAllStates(depositorId: DepositorId, state: StateLabel): Seq[Deposit]
}
