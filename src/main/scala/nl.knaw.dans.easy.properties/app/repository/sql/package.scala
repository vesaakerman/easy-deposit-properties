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
