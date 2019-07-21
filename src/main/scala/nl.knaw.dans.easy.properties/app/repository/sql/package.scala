package nl.knaw.dans.easy.properties.app.repository

import java.sql.Timestamp
import java.util.Calendar

import org.joda.time.{ DateTime, DateTimeZone }
import org.joda.time.format.{ DateTimeFormatter, ISODateTimeFormat }

import scala.language.implicitConversions

package object sql {

  val dateTimeFormatter: DateTimeFormatter = ISODateTimeFormat.dateTime()
  val timeZone: DateTimeZone = DateTimeZone.UTC
  implicit def timeZoneToCalendar(timeZone: DateTimeZone): Calendar = {
    Calendar.getInstance(timeZone.toTimeZone)
  }
  implicit def dateTimeToTimestamp(dt: DateTime): Timestamp = new Timestamp(dt.getMillis)
}
