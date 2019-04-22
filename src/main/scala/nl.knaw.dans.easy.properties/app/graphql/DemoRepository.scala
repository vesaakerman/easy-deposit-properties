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

import java.util.{ TimeZone, UUID }

import nl.knaw.dans.easy.properties.app.model.State.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, State }
import org.joda.time.{ DateTime, DateTimeZone }

import scala.collection.mutable

trait DemoRepository {

  private val timeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"))

  private val depositId1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
  private val depositId2 = UUID.fromString("00000000-0000-0000-0000-000000000002")

  private val depositRepo = mutable.Map(
    depositId1 -> Deposit(
      depositId1,
      new DateTime(2019, 4, 22, 15, 58, timeZone),
      "user001",
    ),
    depositId2 -> Deposit(
      depositId2,
      new DateTime(2019, 1, 1, 0, 0, timeZone),
      "user001",
    ),
  )

  private val stateRepo = mutable.Map(
    depositId1 -> State(StateLabel.SUBMITTED, "await processing"),
    depositId2 -> State(StateLabel.FAILED, "I did something wrong"),
  )

  def getAllDeposits: Seq[Deposit] = depositRepo.values.toSeq

  def getDeposit(id: UUID): Option[Deposit] = depositRepo.get(id)

  def registerDeposit(deposit: Deposit): Option[Deposit] = {
    if (depositRepo contains deposit.id)
      Option.empty
    else {
      depositRepo += (deposit.id -> deposit)
      Option(deposit)
    }
  }

  def getState(id: UUID): Option[State] = stateRepo.get(id)

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
}
