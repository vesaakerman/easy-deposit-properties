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

import java.sql.{ Connection, ResultSet, Statement }

import cats.data.NonEmptyList
import cats.instances.either._
import cats.instances.option._
import cats.instances.string._
import cats.instances.uuid._
import cats.syntax.either._
import cats.syntax.functor._
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State, StateLabel }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.repository.{ InvalidValueError, MutationErrorOr, NoSuchDepositError, QueryErrorOr, StateDao }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import resource.managed

class SQLStateDao(implicit connection: Connection) extends StateDao with CommonResultSetParsers with DebugEnhancedLogging {

  private[sql] def parseState(resultSet: ResultSet): Either[InvalidValueError, State] = {
    for {
      label <- parseEnumValue(StateLabel, "state label")(resultSet.getString("label"))
      stateId = resultSet.getString("stateId")
      description = resultSet.getString("description")
      timestamp <- parseDateTime(resultSet.getTimestamp("timestamp", timeZone), timeZone)
    } yield State(stateId, label, description, timestamp)
  }

  private[sql] def parseDepositIdAndState(resultSet: ResultSet): Either[InvalidValueError, (DepositId, State)] = {
    for {
      depositId <- parseDepositId(resultSet.getString("depositId"))
      state <- parseState(resultSet)
    } yield depositId -> state
  }

  private[sql] def parseStateIdAndDeposit(resultSet: ResultSet): Either[InvalidValueError, (String, Deposit)] = {
    for {
      deposit <- parseDeposit(resultSet)
      stateId = resultSet.getString("stateId")
    } yield stateId -> deposit
  }

  override def getById(ids: Seq[String]): QueryErrorOr[Seq[(String, Option[State])]] = {
    trace(ids)

    def collectResults(stream: Stream[State]): Seq[(String, Option[State])] = {
      val results = stream.toList
        .groupBy(_.id)
        .flatMap {
          case (id, ss) => ss.headOption.tupleLeft(id)
        }
      ids.map(id => id -> results.get(id))
    }

    NonEmptyList.fromList(ids.toList)
      .map(_
        .traverse[Either[InvalidValueError, ?], String](validateId)
        .map(QueryGenerator.getElementsById("State", "stateId"))
        .flatMap(executeQuery(extractResults(parseState)(collectResults))(ids))
      )
      .getOrElse(Seq.empty.asRight)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[State])]] = {
    trace(ids)

    def collectResults(stream: Stream[(DepositId, State)]): Seq[(DepositId, Option[State])] = {
      val results = stream.toList
        .groupBy { case (depositId, _) => depositId }
        .flatMap {
          case (id, ss) => ss.headOption.map { case (_, state) => state }.tupleLeft(id)
        }
      ids.map(id => id -> results.get(id))
    }

    NonEmptyList.fromList(ids.toList)
      .map(QueryGenerator.getCurrentElementByDepositId("State"))
      .map(executeQuery(extractResults(parseDepositIdAndState)(collectResults))(ids))
      .getOrElse(Seq.empty.asRight)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[State])]] = {
    trace(ids)

    def collectResults(stream: Stream[(DepositId, State)]): Seq[(DepositId, Seq[State])] = {
      val results = stream.toList
        .groupBy { case (depositId, _) => depositId }
        .map {
          case (id, ss) => id -> ss.map { case (_, state) => state }
        }
      ids.map(id => id -> results.getOrElse(id, Seq.empty))
    }

    NonEmptyList.fromList(ids.toList)
      .map(QueryGenerator.getAllElementsByDepositId("State"))
      .map(executeQuery(extractResults(parseDepositIdAndState)(collectResults))(ids))
      .getOrElse(Seq.empty.asRight)
  }

  override def store(id: DepositId, state: InputState): MutationErrorOr[State] = {
    trace(id, state)
    val query = QueryGenerator.storeState()

    val managedResultSet = for {
      prepStatement <- managed(connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
      _ = prepStatement.setString(1, id.toString)
      _ = prepStatement.setString(2, state.label.toString)
      _ = prepStatement.setString(3, state.description)
      _ = prepStatement.setTimestamp(4, state.timestamp, timeZone)
      _ = prepStatement.executeUpdate()
      resultSetForKey <- managed(prepStatement.getGeneratedKeys)
    } yield resultSetForKey

    managedResultSet
      .map {
        case resultSet if resultSet.next() => resultSet.getLong(1).toString.asRight
        case _ => throw new Exception(s"not able to insert state (${ state.label }, ${ state.description }, ${ state.timestamp })")
      }
      .either
      .either
      .leftMap(_ => NoSuchDepositError(id))
      .flatMap(identity)
      .map(stateId => State(stateId, state.label, state.description, state.timestamp))
  }

  override def getDepositsById(ids: Seq[String]): QueryErrorOr[Seq[(String, Option[Deposit])]] = {
    trace(ids)

    def collectResults(stream: Stream[(String, Deposit)]): Seq[(String, Option[Deposit])] = {
      val results = stream.toList
        .groupBy { case (stateId, _) => stateId }
        .flatMap {
          case (id, ss) => ss.headOption.map { case (_, deposit) => deposit }.tupleLeft(id)
        }
      ids.map(id => id -> results.get(id))
    }

    NonEmptyList.fromList(ids.toList)
      .map(_
        .traverse[Either[InvalidValueError, ?], String](validateId)
        .map(QueryGenerator.getDepositsById("State", "stateId"))
        .flatMap(executeQuery(extractResults(parseStateIdAndDeposit)(collectResults))(ids))
      )
      .getOrElse(Seq.empty.asRight)
  }
}
