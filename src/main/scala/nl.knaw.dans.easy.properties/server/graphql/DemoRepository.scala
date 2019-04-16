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
package nl.knaw.dans.easy.properties.server.graphql

import java.util.UUID

import nl.knaw.dans.easy.properties.app.model.State.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, State }

import scala.collection.mutable.ListBuffer

trait DemoRepository {

  private val deposits = ListBuffer(
    Deposit(UUID.fromString("00000000-0000-0000-0000-000000000001"), State(StateLabel.SUBMITTED, "await processing")),
    Deposit(UUID.fromString("00000000-0000-0000-0000-000000000002"), State(StateLabel.FAILED, "I did something wrong")),
  )

  def getDeposit(id: UUID): Option[Deposit] = deposits.find(_.id == id)

  def getAllDeposits: Seq[Deposit] = deposits

  def setState(id: UUID, state: State): Option[Deposit] = {
    for {
      deposit <- getDeposit(id)
      index = deposits.indexOf(deposit)
      newDeposit = deposit.copy(state = state)
      _ = deposits.update(index, newDeposit)
    } yield newDeposit
  }
}
