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

import java.sql.{ Connection, ResultSet }

import cats.data.NonEmptyList
import cats.instances.either._
import cats.instances.option._
import cats.instances.string._
import cats.instances.uuid._
import cats.instances.vector._
import cats.syntax.either._
import cats.syntax.functor._
import cats.syntax.traverse._
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, Timestamp }
import nl.knaw.dans.easy.properties.app.repository.{ DepositAlreadyExistsError, DepositDao, DepositFilters, InvalidValueError, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import resource.managed

class SQLDepositDao(implicit connection: Connection) extends DepositDao with CommonResultSetParsers with DebugEnhancedLogging {

  private def parseLastModifiedResponse(resultSet: ResultSet): Either[InvalidValueError, (DepositId, Timestamp)] = {
    for {
      depositId <- parseDepositId(resultSet.getString("depositId"))
      maxTimestamp <- parseDateTime(resultSet.getTimestamp("max_timestamp", timeZone), timeZone)
    } yield depositId -> maxTimestamp
  }

  override def getAll: QueryErrorOr[Seq[Deposit]] = {
    trace(())
    val query = QueryGenerator.getAllDeposits
    executeQuery(extractResults(parseDeposit)(_.toList))(Seq.empty[DepositId])(query)
  }

  override def find(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Deposit])]] = {
    trace(ids)

    def collectResults(stream: Stream[Deposit]): Seq[(DepositId, Option[Deposit])] = {
      val results = stream.toList
        .groupBy(_.id)
        .flatMap {
          case (id, ds) => ds.headOption.tupleLeft(id)
        }
      ids.map(id => id -> results.get(id))
    }

    NonEmptyList.fromList(ids.toList)
      .map(QueryGenerator.findDeposits)
      .map(executeQuery(extractResults(parseDeposit)(collectResults))(ids))
      .getOrElse(Seq.empty.asRight)
  }

  private def search(filters: DepositFilters): QueryErrorOr[Seq[Deposit]] = {
    val (query, values) = QueryGenerator.searchDeposits(filters)
    executeQuery(extractResults(parseDeposit)(_.toList))(values)(query)
  }

  override def search(filters: Seq[DepositFilters]): QueryErrorOr[Seq[(DepositFilters, Seq[Deposit])]] = {
    trace(filters)
    // ideally this would be implemented with one query,
    // but that would be quite difficult (if not impossible) to do.
    // Also, 'filters' will usually have one element and can only get larger with deep nesting.
    filters.toVector.traverse(fs => search(fs).tupleLeft(fs))
  }

  override def store(deposit: Deposit): MutationErrorOr[Deposit] = {
    trace(deposit)
    val query = QueryGenerator.storeDeposit()

    managed(connection.prepareStatement(query))
      .map(prepStatement => {
        prepStatement.setString(1, deposit.id.toString)
        prepStatement.setString(2, deposit.bagName.orNull)
        prepStatement.setTimestamp(3, deposit.creationTimestamp, timeZone)
        prepStatement.setString(4, deposit.depositorId)
        prepStatement.executeUpdate()
      })
      .either
      .either
      .leftMap(_ => DepositAlreadyExistsError(deposit.id))
      .map(_ => deposit)
  }

  override def lastModified(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Timestamp])]] = {
    trace(ids)

    def collectResults(stream: Stream[(DepositId, Timestamp)]): Seq[(DepositId, Option[Timestamp])] = {
      val results = stream.toList
        .groupBy { case (depositId, _) => depositId }
        .flatMap {
          case (id, rs) => rs.headOption.map { case (_, timestamp) => timestamp }.tupleLeft(id)
        }
      ids.map(id => id -> results.get(id))
    }

    NonEmptyList.fromList(ids.toList)
      .map(QueryGenerator.getLastModifiedDate)
      .map { case (query, values) => executeQuery(extractResults(parseLastModifiedResponse)(collectResults))(values.toList)(query) }
      .getOrElse(Seq.empty.asRight)
  }
}
