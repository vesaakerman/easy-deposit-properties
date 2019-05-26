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

import nl.knaw.dans.easy.properties.app.model.IngestStep.StepLabel.StepLabel
import nl.knaw.dans.easy.properties.app.model._
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State }
import nl.knaw.dans.easy.properties.app.model.state.StateLabel.StateLabel

trait DepositRepository {

  def getAllDeposits: Seq[Deposit]

  def getDeposit(id: DepositId): Option[Deposit]

  def getDepositsByDepositor(depositorId: DepositorId): Seq[Deposit]

  def addDeposit(deposit: Deposit): Option[Deposit]

  def getStateById(id: String): Option[State]

  def getCurrentState(id: DepositId): Option[State]

  def getAllStates(id: DepositId): Seq[State]

  def getCurrentStates(ids: Seq[DepositId]): Seq[(DepositId, Option[State])]

  def getAllStates(ids: Seq[DepositId]): Seq[(DepositId, Seq[State])]

  def setState(id: DepositId, state: InputState): Option[State]

  def getDepositByStateId(id: String): Option[Deposit]

  def getDepositsByCurrentState(state: StateLabel): Seq[Deposit]

  def getDepositsByAllStates(state: StateLabel): Seq[Deposit]

  def getDepositsByDepositorAndCurrentState(depositorId: DepositorId, state: StateLabel): Seq[Deposit]

  def getDepositsByDepositorAndAllStates(depositorId: DepositorId, state: StateLabel): Seq[Deposit]

  def getIngestStepById(id: String): Option[IngestStep]

  def getCurrentIngestStep(id: DepositId): Option[IngestStep]

  def getAllIngestSteps(id: DepositId): Seq[IngestStep]

  def getCurrentIngestSteps(ids: Seq[DepositId]): Seq[(DepositId, Option[IngestStep])]

  def getAllIngestSteps(ids: Seq[DepositId]): Seq[(DepositId, Seq[IngestStep])]

  def setIngestStep(id: DepositId, step: InputIngestStep): Option[IngestStep]

  def getDepositByIngestStepId(id: String): Option[Deposit]

  def getDepositsByCurrentIngestStep(label: StepLabel): Seq[Deposit]

  def getDepositsByAllIngestSteps(label: StepLabel): Seq[Deposit]
}
