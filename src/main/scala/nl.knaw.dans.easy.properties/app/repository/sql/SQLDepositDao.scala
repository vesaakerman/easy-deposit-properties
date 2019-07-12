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

import cats.instances.either._
import cats.instances.vector._
import cats.syntax.functor._
import cats.syntax.traverse._
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositFilter, DepositId, SeriesFilter, Timestamp }
import nl.knaw.dans.easy.properties.app.repository.{ DepositDao, DepositFilters, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import resource.managed

class SQLDepositDao(implicit connection: Connection) extends DepositDao with DebugEnhancedLogging {

  def getAll: QueryErrorOr[Seq[Deposit]] = {
    /*
     * SELECT *
     * FROM Deposit;
     */

    ???
  }

  def find(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Deposit])]] = {
    /*
     * SELECT *
     * FROM Deposit
     * WHERE depositId IN (?*);
     */

    ???
  }

  // TODO extract in separate class, such that it can be tested
  private def createSearchQuery(filters: DepositFilters): (String, Seq[String]) = {
    /*
     * latest state with label:
     *   SELECT depositId
     *   FROM (
     *     SELECT *, max(timestamp) over (partition by depositId) as max_timestamp
     *     FROM State
     *     WHERE label = ?
     *   ) AS t
     *   WHERE timestamp = max_timestamp
     * 
     * all states with label:
     * 
     *   SELECT DISTINCT depositId
     *   FROM State
     *   WHERE label = ?
     */
    type TableName = String
    type LabelName = String
    type Query = String

    def createSubQuery[T <: DepositFilter](filter: T)(tableName: TableName, labelName: LabelName, labelValue: T => String): (TableName, Query, String) = {
      val query = filter.filter match {
        case SeriesFilter.ALL =>
          s"SELECT DISTINCT depositId FROM $tableName WHERE $labelName = ?"
        case SeriesFilter.LATEST =>
          s"SELECT depositId FROM ( SELECT *, max(timestamp) over (partition by depositId) as max_timestamp FROM $tableName WHERE $labelName = ?) AS ${ tableName }WithMaxTimestamp WHERE timestamp = max_timestamp"
      }

      (tableName, query, labelValue(filter))
    }

    val (queryWherePart, whereValues) = List(
      filters.depositorId.map("depositorId" -> _),
      filters.bagName.map("bagName" -> _),
    )
      .flatten
      .map {
        case (labelName, value) => s"$labelName = ?" -> value
      }
      .foldLeft(("", List.empty[String])) {
        case (("", vs), (subQuery, value)) =>
          subQuery -> (value :: vs)
        case ((q, vs), (subQuery, value)) =>
          s"$q AND $subQuery" -> (value :: vs)
      }
    val (queryJoinPart, joinValues) = List(
      filters.stateFilter.map(createSubQuery(_)("State", "label", _.label.toString)),
      filters.ingestStepFilter.map(createSubQuery(_)("SimpleProperties", "value", _.label.toString)),
      filters.doiRegisteredFilter.map(createSubQuery(_)("SimpleProperties", "value", _.value.toString)),
      filters.doiActionFilter.map(createSubQuery(_)("SimpleProperties", "value", _.value.toString)),
      filters.curatorFilter.map(createSubQuery(_)("Curation", "datamanagerUserId", _.curator)),
      filters.isNewVersionFilter.map(createSubQuery(_)("Curation", "isNewVersion", _.isNewVersion.toString)),
      filters.curationRequiredFilter.map(createSubQuery(_)("Curation", "isRequired", _.curationRequired.toString)),
      filters.curationPerformedFilter.map(createSubQuery(_)("Curation", "isPerformed", _.curationPerformed.toString)),
      filters.contentTypeFilter.map(createSubQuery(_)("SimpleProperties", "value", _.value.toString)),
    )
      .flatten
      .map {
        case (tableName, q, value) => s"INNER JOIN ($q) AS ${ tableName }SearchResult USING (depositId)" -> value
      }
      .foldLeft(("", List.empty[String])) {
        case (("", vs), (subQuery, value)) => subQuery -> (value :: vs)
        case ((q, vs), (subQuery, value)) => s"$q $subQuery" -> (value :: vs)
      }
    
    val depositQueryWherePart = if (queryWherePart.isEmpty) ""
                                else s"WHERE $queryWherePart"
    val depositTableQuery = s"(SELECT * FROM Deposit $depositQueryWherePart) AS SelectedDeposits"

    /*
     * example:
     *   SELECT *
     *   FROM (
     *     SELECT * FROM Deposit WHERE depositorId = ?
     *   ) AS SelectedDeposits INNER JOIN (
     *     SELECT depositId
     *     FROM (
     *       SELECT *, max(timestamp) over (partition by depositId) as max_timestamp
     *       FROM State
     *       WHERE label = ?
     *     ) AS StateWithMaxTimestamp
     *     WHERE timestamp = max_timestamp
     *   ) AS StateSearchResult USING (depositId);
     */
    val query = s"SELECT * FROM $depositTableQuery $queryJoinPart;"
    val values = whereValues.reverse ::: joinValues.reverse 

    query -> values
  }

  private def search(filters: DepositFilters): QueryErrorOr[Seq[Deposit]] = {
    val (query, values) = createSearchQuery(filters)

    val resultSet = for {
      prepStatement <- managed(connection.prepareStatement(query))
      _ = values.zipWithIndex.foreach { case (v, i) => prepStatement.setString(i + 1, v) } // 'i + 1' because these indices start at 1
      resultSet <- managed(prepStatement.executeQuery())
    } yield resultSet

    ???
  }

  def search(filters: Seq[DepositFilters]): QueryErrorOr[Seq[(DepositFilters, Seq[Deposit])]] = {
    // ideally this would be implemented with one query,
    // but that would be quite difficult (if not impossible) to do.
    // Also, 'filters' will usually have one element and can only get larger with deep nesting.
    filters.toVector.traverse(fs => search(fs).tupleLeft(fs))
  }

  def store(deposit: Deposit): MutationErrorOr[Deposit] = {
    /*
     * INSERT INTO Deposit (depositId, bagName, creationTimestamp, depositorId)
     * VALUES (?, ?, ?, ?);
     */

    ???
  }

  def lastModified(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Timestamp])]] = {
    /*
     * SELECT depositId, MAX(max)
     * FROM (
     *   ( SELECT depositId, MAX(creationTimestamp) FROM Deposit WHERE depositId IN (?*) GROUP BY depositId ) UNION ALL
     *   ( SELECT depositId, MAX(timestamp) FROM State WHERE depositId IN (?*) GROUP BY depositId ) UNION ALL
     *   ( SELECT depositId, MAX(timestamp) FROM Identifier WHERE depositId IN (?*) GROUP BY depositId ) UNION ALL
     *   ( SELECT depositId, MAX(timestamp) FROM Curation WHERE depositId IN (?*) GROUP BY depositId ) UNION ALL
     *   ( SELECT depositId, MAX(timestamp) FROM Springfield WHERE depositId IN (?*) GROUP BY depositId ) UNION ALL
     *   ( SELECT depositId, MAX(timestamp) FROM SimpleProperties WHERE depositId IN (?*) GROUP BY depositId )
     * ) AS max_timestamps
     * GROUP BY depositId;
     */

    ???
  }
}
