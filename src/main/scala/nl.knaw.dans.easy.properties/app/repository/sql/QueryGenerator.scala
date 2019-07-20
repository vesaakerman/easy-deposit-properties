package nl.knaw.dans.easy.properties.app.repository.sql

import cats.data.NonEmptyList
import nl.knaw.dans.easy.properties.app.model.{ DepositFilter, DepositId, SeriesFilter }
import nl.knaw.dans.easy.properties.app.repository.DepositFilters

object QueryGenerator {

  def getAllDeposits: String = "SELECT * FROM Deposit;"

  def findDeposits(ids: NonEmptyList[DepositId]): String = {
    s"SELECT * FROM Deposit WHERE depositId IN (${ ids.toList.map(_ => "?").mkString(", ") });"
  }

  def searchDeposits(filters: DepositFilters): (String, Seq[String]) = {
    type TableName = String
    type KeyName = String
    type LabelName = String
    type Query = String

    def createSubQuery[T <: DepositFilter](filter: T)(tableName: TableName, labelName: LabelName, labelValue: T => String): (TableName, Query, List[String]) = {
      val query = filter.filter match {
        case SeriesFilter.ALL =>
          s"SELECT DISTINCT depositId FROM $tableName WHERE $labelName = ?"
        case SeriesFilter.LATEST =>
          s"SELECT $tableName.depositId FROM $tableName INNER JOIN (SELECT depositId, max(timestamp) AS max_timestamp FROM $tableName GROUP BY depositId) AS ${ tableName }WithMaxTimestamp ON $tableName.timestamp = ${ tableName }WithMaxTimestamp.max_timestamp WHERE $labelName = ?"
      }

      (tableName, query, labelValue(filter) :: Nil)
    }

    def createSimplePropertiesSubQuery[T <: DepositFilter](filter: T)(keyValue: String, labelValue: T => String): (TableName, Query, List[String]) = {
      val tableName = "SimpleProperties"
      val query = filter.filter match {
        case SeriesFilter.ALL =>
          s"SELECT DISTINCT depositId FROM $tableName WHERE key = ? AND value = ?"
        case SeriesFilter.LATEST =>
          s"SELECT $tableName.depositId FROM $tableName INNER JOIN (SELECT depositId, max(timestamp) AS max_timestamp FROM $tableName WHERE key = ? GROUP BY depositId) AS ${ tableName }WithMaxTimestamp ON $tableName.timestamp = ${ tableName }WithMaxTimestamp.max_timestamp WHERE value = ?"
      }

      (tableName, query, labelValue(filter) :: keyValue :: Nil)
    }

    val (queryWherePart, whereValues) = List(
      filters.depositorId.map("depositorId" -> _),
      filters.bagName.map("bagName" -> _),
    )
      .collect {
        case Some((labelName, null)) => s"$labelName IS NULL" -> Nil
        case Some((labelName, value)) => s"$labelName = ?" -> List(value)
      }
      .foldLeft(("", List.empty[String])) {
        case (("", vs), (subQuery, values)) => subQuery -> (values ::: vs)
        case ((q, vs), (subQuery, values)) => s"$q AND $subQuery" -> (values ::: vs)
      }
    val (queryJoinPart, joinValues) = List(
      filters.stateFilter.map(createSubQuery(_)("State", "label", _.label.toString)),
      filters.ingestStepFilter.map(createSimplePropertiesSubQuery(_)("ingest-step", _.label.toString)),
      filters.doiRegisteredFilter.map(createSimplePropertiesSubQuery(_)("doi-registered", _.value.toString)),
      filters.doiActionFilter.map(createSimplePropertiesSubQuery(_)("doi-action", _.value.toString)),
      filters.curatorFilter.map(createSubQuery(_)("Curation", "datamanagerUserId", _.curator)),
      filters.isNewVersionFilter.map(createSubQuery(_)("Curation", "isNewVersion", _.isNewVersion.toString)),
      filters.curationRequiredFilter.map(createSubQuery(_)("Curation", "isRequired", _.curationRequired.toString)),
      filters.curationPerformedFilter.map(createSubQuery(_)("Curation", "isPerformed", _.curationPerformed.toString)),
      filters.contentTypeFilter.map(createSimplePropertiesSubQuery(_)("content-type", _.value.toString)),
    )
      .collect {
        case Some((tableName, q, values)) if queryWherePart.isEmpty => s"INNER JOIN ($q) AS ${ tableName }SearchResult ON Deposit.depositId = ${ tableName }SearchResult.depositId" -> values
        case Some((tableName, q, values)) => s"INNER JOIN ($q) AS ${ tableName }SearchResult ON SelectedDeposits.depositId = ${ tableName }SearchResult.depositId" -> values
      }
      .foldLeft(("", List.empty[String])) {
        case (("", vs), (subQuery, values)) => subQuery -> (values ::: vs)
        case ((q, vs), (subQuery, values)) => s"$q $subQuery" -> (values ::: vs)
      }

    (queryJoinPart, queryWherePart) match {
      case ("", "") =>
        val query = s"SELECT * FROM Deposit;"
        query -> Nil
      case ("", _) =>
        val query = s"SELECT * FROM Deposit WHERE $queryWherePart;"
        query -> whereValues.reverse
      case (_, "") =>
        val query = s"SELECT * FROM Deposit $queryJoinPart;"
        query -> joinValues.reverse
      case (_, _) =>
        val query = s"SELECT * FROM (SELECT * FROM Deposit WHERE $queryWherePart) AS SelectedDeposits $queryJoinPart;"
        query -> (whereValues.reverse ::: joinValues.reverse)
    }
  }

  def getLastModifiedDate(ids: NonEmptyList[DepositId]): (String, NonEmptyList[String]) = {
    val whereClause = ids.toList.map(_ => "?").mkString("WHERE depositId IN (", ", ", ")")
    val tablesAndMaxFields = List(
      "Deposit" -> "creationTimestamp",
      "State" -> "timestamp",
      "Identifier" -> "timestamp",
      "Curation" -> "timestamp",
      "Springfield" -> "timestamp",
      "SimpleProperties" -> "timestamp",
    )
    val query = tablesAndMaxFields
      .map { case (table, maxField) => s"(SELECT depositId, MAX($maxField) AS max FROM $table $whereClause GROUP BY depositId)" }
      .mkString("SELECT depositId, MAX(max) AS max_timestamp FROM (", " UNION ALL ", ") AS max_timestamps GROUP BY depositId;")

    val stringIds = ids.map(_.toString)
    val values = tablesAndMaxFields.map(_ => stringIds).reduce(_ ::: _)

    query -> values
  }

  def getElementsById(tableName: String, idColumnName: String)(ids: NonEmptyList[String]): String = {
    s"SELECT * FROM $tableName WHERE $idColumnName IN (${ ids.toList.map(_ => "?").mkString(", ") });"
  }

  def getCurrentElementByDepositId(tableName: String)(ids: NonEmptyList[DepositId]): String = {
    s"""SELECT *
       |FROM $tableName
       |INNER JOIN (
       |  SELECT depositId, max(timestamp) AS max_timestamp
       |  FROM $tableName
       |  WHERE depositId IN (${ ids.toList.map(_ => "?").mkString(", ") })
       |  GROUP BY depositId
       |) AS deposit_with_max_timestamp USING (depositId)
       |WHERE timestamp = max_timestamp;""".stripMargin
  }

  def getAllElementsByDepositId(tableName: String)(ids: NonEmptyList[DepositId]): String = {
    s"SELECT * FROM $tableName WHERE depositId IN (${ ids.toList.map(_ => "?").mkString(", ") });"
  }

  def getDepositsById(tableName: String, idColumnName: String)(ids: NonEmptyList[String]): String = {
    s"SELECT $idColumnName, depositId, bagName, creationTimestamp, depositorId FROM Deposit INNER JOIN $tableName ON Deposit.depositId = $tableName.depositId WHERE $idColumnName IN (${ ids.toList.map(_ => "?").mkString(", ") });"
  }

  def storeDeposit(): String = {
    "INSERT INTO Deposit (depositId, bagName, creationTimestamp, depositorId) VALUES (?, ?, ?, ?);"
  }

  def storeCuration(): String = {
    "INSERT INTO Curation (depositId, isNewVersion, isRequired, isPerformed, datamanagerUserId, datamanagerEmail, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?);"
  }

  def storeSpringfield(): String = {
    "INSERT INTO Springfield (depositId, domain, springfield_user, collection, playmode, timestamp) VALUES (?, ?, ?, ?, ?, ?);"
  }

  def storeState(): String = {
    "INSERT INTO State (depositId, label, description, timestamp) VALUES (?, ?, ?, ?);"
  }
}
