package nl.knaw.dans.easy.properties.app.repository.sql

import java.sql.{ Connection, ResultSet }
import java.util.UUID

import cats.Show
import cats.instances.either._
import cats.instances.stream._
import cats.syntax.either._
import cats.syntax.traverse._
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, Timestamp }
import nl.knaw.dans.easy.properties.app.repository.InvalidValueError
import org.joda.time.{ DateTime, DateTimeZone }
import resource.managed

import scala.language.higherKinds

private[sql] trait CommonResultSetParsers {

  private[sql] def validateId(s: String): Either[InvalidValueError, String] = {
    Either.catchOnly[NumberFormatException](s.toLong)
      .leftMap(_ => InvalidValueError(s"invalid id '$s'"))
      .map(_ => s)
  }

  private[sql] def parseDepositId(s: String): Either[InvalidValueError, DepositId] = {
    Either.catchOnly[IllegalArgumentException] { UUID.fromString(s) }
      .leftMap(_ => InvalidValueError(s"Invalid depositId value: '$s'"))
  }

  private[sql] def parseDateTime(t: java.sql.Timestamp, timeZone: DateTimeZone): Either[InvalidValueError, Timestamp] = {
    Either.catchOnly[IllegalArgumentException] { new DateTime(t, timeZone) }
      .leftMap(_ => InvalidValueError(s"Invalid timestamp value: '$t'"))
  }

  private[sql] def parseEnumValue[E <: Enumeration](enum: E, enumDescription: String)(s: String) = {
    Either.catchOnly[NoSuchElementException] { enum.withName(s) }
      .leftMap(_ => InvalidValueError(s"Invalid $enumDescription value: '$s'"))
  }

  private[sql] def parseDeposit(resultSet: ResultSet): Either[InvalidValueError, Deposit] = {
    for {
      depositId <- parseDepositId(resultSet.getString("depositId"))
      bagName = Option(resultSet.getString("bagName"))
      creationTimestamp <- parseDateTime(resultSet.getTimestamp("creationTimestamp", timeZone), timeZone)
      depositorId = resultSet.getString("depositorId")
    } yield Deposit(depositId, bagName, creationTimestamp, depositorId)
  }

  private[sql] def extractResults[T, X](parseResult: ResultSet => Either[InvalidValueError, T])
                                       (collectResults: Stream[T] => Seq[X])
                                       (result: ResultSet): Either[InvalidValueError, Seq[X]] = {
    Stream.continually(result.next())
      .takeWhile(b => b)
      .traverse[Either[InvalidValueError, ?], T](_ => parseResult(result))
      .map(collectResults)
  }

  private[sql] def executeQuery[Id, X](extract: ResultSet => Either[InvalidValueError, X])
                                      (ids: Seq[Id])
                                      (query: String)
                                      (implicit showId: Show[Id], connection: Connection) = {
    val resultSet = for {
      prepStatement <- managed(connection.prepareStatement(query))
      _ = ids.zipWithIndex.foreach { case (id, index) => prepStatement.setString(index + 1, showId.show(id)) }
      resultSet <- managed(prepStatement.executeQuery())
    } yield resultSet

    resultSet.map(extract)
      .either
      .either
      .leftMap(InvalidValueError(_))
      .flatMap(identity)
  }
}
