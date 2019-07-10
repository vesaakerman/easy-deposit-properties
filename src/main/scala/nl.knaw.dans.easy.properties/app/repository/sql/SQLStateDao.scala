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
package nl.knaw.dans.easy.properties.app.repository.sql

import java.sql.Connection

import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.repository.{ MutationErrorOr, QueryErrorOr, StateDao }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class SQLStateDao(implicit connection: Connection) extends StateDao with DebugEnhancedLogging {

  def getById(ids: Seq[String]): QueryErrorOr[Seq[(String, Option[State])]] = {
    /*
     * SELECT * FROM State WHERE stateId IN (?*);
     */

    ???
  }

  def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[State])]] = {
    /*
     * SELECT stateId, depositId, label, description, timestamp
     * FROM (
     *     SELECT *, max(timestamp) over (partition by depositId) as max_timestamp
     *     FROM State
     *     WHERE depositId IN (?*)
     * ) AS states_with_max_timestamp
     * WHERE timestamp = max_timestamp;
     */

    // FIXME this query assumes that at least the max_timestamp is a unique timestamp within the boundaries of its depositId.
    //   Should we design our tables such that (depositId, timestamp) is unique?
    //   Then SimpleProperties should have a constraint 'UNIQUE (depositId, key, timestamp)'.

    ???
  }

  def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[State])]] = {
    /*
     * SELECT *
     * FROM State
     * WHERE depositId IN (?*);
     */

    ???
  }

  def store(id: DepositId, state: InputState): MutationErrorOr[State] = {
    /*
     * INSERT INTO State (depositId, label, description, timestamp)
     * VALUES (?, ?, ?, ?);
     */

    ???
  }

  def getDepositsById(ids: Seq[String]): QueryErrorOr[Seq[(String, Option[Deposit])]] = {
    /*
     * SELECT stateId, depositId, bagname, creationTimestamp, depositorId
     * FROM Deposit
     * INNER JOIN (
     *     SELECT stateId, depositId FROM State WHERE stateId IN (?*)
     * ) AS StateSelection USING (depositId);
     */

    /*
     * SELECT stateId, depositId, bagName, creationTimestamp, depositorId
     * FROM Deposit
     * INNER JOIN State USING (depositId)
     * WHERE stateId IN (1, 7, 13);
     */

    ???
  }
}
