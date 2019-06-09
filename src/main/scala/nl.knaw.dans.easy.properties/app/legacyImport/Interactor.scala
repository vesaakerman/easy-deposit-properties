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
package nl.knaw.dans.easy.properties.app.legacyImport

import cats.syntax.either._

import scala.io.StdIn
import scala.util.Try

class Interactor {

  def ask(msg: String): String = {
    StdIn.readLine(msg)
  }

  def ask(enum: Enumeration)(msg: String): LoadPropsErrorOr[enum.Value] = {
    val input = ask(msg)

    Either.catchOnly[NoSuchElementException] { enum.withName(input) }
      .leftFlatMap(_ => ask(enum)(msg))
  }

  def ask[T](f: String => T)(msg: String): T = {
    val input = ask(msg)

    Try { f(input) }.getOrElse(ask(f)(msg))
  }
}
