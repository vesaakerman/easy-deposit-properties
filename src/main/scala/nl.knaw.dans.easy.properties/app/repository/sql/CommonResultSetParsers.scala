package nl.knaw.dans.easy.properties.app.repository.sql

import java.sql.ResultSet
import java.util.UUID

import cats.syntax.either._
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, Timestamp }
import nl.knaw.dans.easy.properties.app.repository.InvalidValueError
import org.joda.time.{ DateTime, DateTimeZone }

trait CommonResultSetParsers {

  private[sql] def parseDepositId(s: String): Either[InvalidValueError, DepositId] = {
    Either.catchOnly[IllegalArgumentException] { UUID.fromString(s) }
      .leftMap(_ => InvalidValueError(s"Invalid depositId value: '$s'"))
  }

  private[sql] def parseDateTime(t: java.sql.Timestamp, timeZone: DateTimeZone): Either[InvalidValueError, Timestamp] = {
    Either.catchOnly[IllegalArgumentException] { new DateTime(t, timeZone) }
      .leftMap(_ => InvalidValueError(s"Invalid timestamp value: '$t'"))
  }

  private[sql] def parseDeposit(resultSet: ResultSet): Either[InvalidValueError, Deposit] = {
    for {
      depositId <- parseDepositId(resultSet.getString("depositId"))
      bagName = Option(resultSet.getString("bagName"))
      creationTimestamp <- parseDateTime(resultSet.getTimestamp("creationTimestamp", timeZone), timeZone)
      depositorId = resultSet.getString("depositorId")
    } yield Deposit(depositId, bagName, creationTimestamp, depositorId)
  }
}
