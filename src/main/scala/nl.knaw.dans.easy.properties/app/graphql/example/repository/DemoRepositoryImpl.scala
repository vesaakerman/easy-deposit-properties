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

import java.util.{ TimeZone, UUID }

import nl.knaw.dans.easy.properties.app.model.State.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, State }
import org.joda.time.{ DateTime, DateTimeZone }

import scala.collection.mutable

class DemoRepositoryImpl extends DemoRepository {

  private val timeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"))

  private val depositId1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
  private val depositId2 = UUID.fromString("00000000-0000-0000-0000-000000000002")
  private val depositId3 = UUID.fromString("00000000-0000-0000-0000-000000000003")
  private val depositId4 = UUID.fromString("00000000-0000-0000-0000-000000000004")
  private val depositId5 = UUID.fromString("00000000-0000-0000-0000-000000000005")

  private val user1 = "user001"
  private val user2 = "user002"

  override val depositRepo: mutable.Map[DepositId, Deposit] = mutable.Map(
    depositId1 -> Deposit(depositId1, new DateTime(2019, 4, 22, 15, 58, timeZone), user1),
    depositId2 -> Deposit(depositId2, new DateTime(2019, 1, 1, 0, 0, timeZone), user1),
    depositId3 -> Deposit(depositId3, new DateTime(2019, 2, 2, 0, 0, timeZone), user2),
    depositId4 -> Deposit(depositId4, new DateTime(2019, 3, 3, 0, 0, timeZone), user1),
    depositId5 -> Deposit(depositId5, new DateTime(2019, 4, 4, 0, 0, timeZone), user2),
  )

  override val stateRepo: mutable.Map[DepositId, State] = mutable.Map(
    depositId1 -> State(StateLabel.SUBMITTED, "await processing"),
    depositId2 -> State(StateLabel.FAILED, "I did something wrong"),
    depositId3 -> State(StateLabel.ARCHIVED, "all is done"),
    depositId4 -> State(StateLabel.SUBMITTED, "await processing"),
    depositId5 -> State(StateLabel.SUBMITTED, "await processing"),
  )
}
