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
package nl.knaw.dans.easy.properties

import sangria.execution.{ ExceptionHandler, HandledException }

package object server {

  case class GraphQLInput(query: String, variables: Option[String], operationName: Option[String])

  val defaultExceptionHandler = ExceptionHandler(
    onException = { case (_, e) => HandledException(e.getMessage) },
    onViolation = { case (_, e) => HandledException(e.errorMessage) },
    onUserFacingError = { case (_, e) => HandledException(e.getMessage) },
  )
}
