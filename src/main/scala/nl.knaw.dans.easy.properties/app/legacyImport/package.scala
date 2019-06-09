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
package nl.knaw.dans.easy.properties.app

import better.files.File
import nl.knaw.dans.easy.properties.ApplicationError

package object legacyImport {

  type LoadPropsErrorOr[T] = Either[LoadPropsError, T]

  abstract class LoadPropsError(val msg: String) extends ApplicationError
  case class NoSuchPropertiesFileError(file: File) extends LoadPropsError(s"Properties file '$file' does not exist.")
  case class NoSuchParentDirError(file: File) extends LoadPropsError(s"Could not find the parent directory for file '$file'.")
  case class NoDepositIdError(s: String) extends LoadPropsError(s"String '$s' is not a valid depositId.")
  case class IllegalValueError(s: String, enum: Enumeration) extends LoadPropsError(s"Value '$s' is not an element in enum ${ enum.getClass.getSimpleName }. Valid values include: ${ enum.values.mkString("{", ", ", "}") }.")
}
