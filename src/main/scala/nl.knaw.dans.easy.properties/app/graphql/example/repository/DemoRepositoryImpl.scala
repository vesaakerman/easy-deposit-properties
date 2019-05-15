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

  private val deposit1 = Deposit(depositId1, new DateTime(2019, 1, 1, 0, 0, timeZone), user1)
  private val deposit2 = Deposit(depositId2, new DateTime(2019, 2, 2, 0, 0, timeZone), user1)
  private val deposit3 = Deposit(depositId3, new DateTime(2019, 3, 3, 0, 0, timeZone), user2)
  private val deposit4 = Deposit(depositId4, new DateTime(2019, 4, 4, 0, 0, timeZone), user1)
  private val deposit5 = Deposit(depositId5, new DateTime(2019, 5, 5, 0, 0, timeZone), user2)

  private val state10 = State("10", StateLabel.DRAFT, "draft with continued deposit", new DateTime(2019, 1, 1, 0, 0, timeZone))
  private val state11 = State("11", StateLabel.DRAFT, "draft with continued deposit", new DateTime(2019, 1, 1, 1, 1, timeZone))
  private val state12 = State("12", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 1, 1, 2, 2, timeZone))
  private val state13 = State("13", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 1, 1, 3, 3, timeZone))
  private val state14 = State("14", StateLabel.SUBMITTED, "deposit is processing", new DateTime(2019, 1, 1, 4, 4, timeZone))
  private val state15 = State("15", StateLabel.ARCHIVED, "deposit is archived", new DateTime(2019, 1, 1, 5, 5, timeZone))

  private val state20 = State("20", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 2, 2, 0, 0, timeZone))
  private val state21 = State("21", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 2, 2, 1, 1, timeZone))
  private val state22 = State("22", StateLabel.SUBMITTED, "deposit is processing", new DateTime(2019, 2, 2, 2, 2, timeZone))
  private val state23 = State("23", StateLabel.ARCHIVED, "deposit is archived", new DateTime(2019, 2, 2, 3, 3, timeZone))

  private val state30 = State("30", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 3, 3, 0, 0, timeZone))
  private val state31 = State("31", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 3, 3, 1, 1, timeZone))
  private val state32 = State("32", StateLabel.INVALID, "deposit is invalid", new DateTime(2019, 3, 3, 2, 2, timeZone))

  private val state40 = State("40", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 4, 4, 0, 0, timeZone))
  private val state41 = State("41", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 4, 4, 1, 1, timeZone))
  private val state42 = State("42", StateLabel.ARCHIVED, "deposit is archived", new DateTime(2019, 4, 4, 2, 2, timeZone))

  private val state50 = State("50", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 5, 5, 0, 0, timeZone))
  private val state51 = State("51", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 5, 5, 1, 1, timeZone))
  private val state52 = State("52", StateLabel.SUBMITTED, "deposit is processing", new DateTime(2019, 5, 5, 2, 2, timeZone))
  private val state53 = State("53", StateLabel.REJECTED, "deposit is rejected", new DateTime(2019, 5, 5, 3, 3, timeZone))

  override val depositRepo: mutable.Map[DepositId, Deposit] = mutable.Map(
    depositId1 -> deposit1,
    depositId2 -> deposit2,
    depositId3 -> deposit3,
    depositId4 -> deposit4,
    depositId5 -> deposit5,
  )

  override val stateRepo: mutable.Map[DepositId, Seq[State]] = mutable.Map(
    depositId1 -> Seq(state10, state11, state12, state13, state14, state15),
    depositId2 -> Seq(state20, state21, state22, state23),
    depositId3 -> Seq(state30, state31, state32),
    depositId4 -> Seq(state40, state41, state42),
    depositId5 -> Seq(state50, state51, state52, state53),
  )
}
