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
package nl.knaw.dans.easy.properties.server

import javax.servlet.http.HttpServletRequest
import nl.knaw.dans.easy.properties.server.graphql.DemoRepository
import nl.knaw.dans.easy.properties.server.graphql.GraphqlTypes.{ SchemaType, schema }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import nl.knaw.dans.lib.logging.servlet._
import org.json4s.JsonAST.{ JArray, JInt, JObject, JString }
import org.json4s.ext.UUIDSerializer
import org.json4s.native.{ JsonMethods, Serialization }
import org.json4s.{ DefaultFormats, Formats, JValue }
import org.scalatra._
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.json4s.native._
import sangria.parser._

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps
import scala.util.{ Failure, Success }

case class InputQuery(query: String, variables: Option[String], operationName: Option[String])

class GraphQLServlet extends ScalatraServlet with FutureSupport
  with ServletLogger
  with MaskedLogFormatter
  with LogResponseBodyOnError
  with DebugEnhancedLogging {

  private val ctx = SchemaType(new DemoRepository {})

  implicit protected def executor: ExecutionContext = ExecutionContext.global

  private implicit val jsonFormats: Formats = new DefaultFormats {} + UUIDSerializer

  private def executeQuery(query: String, variables: Option[String], operation: Option[String], tracing: Boolean): Future[ActionResult] = {
    QueryParser.parse(query) match {
      // query parsed successfully, time to execute it!
      case Success(queryAst) => execute(queryAst, variables, operation)

      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) => Future.successful(syntaxError(error))
      case Failure(error) => Future.failed(error)
    }
  }

  private def syntaxError(error: SyntaxError): ActionResult = {
    BadRequest(JObject(
      "syntaxError" -> JString(error.getMessage),
      "locations" -> JArray(List(
        JObject(
          "line" -> JInt(error.originalError.position.line),
          "column" -> JInt(error.originalError.position.column),
        )
      ))
    ))
  }

  private def execute(queryAst: Document, variables: Option[String], operation: Option[String]): Future[ActionResult] = {
    Executor.execute(schema, queryAst, ctx,
      operationName = operation,
      variables = variables map parseVariables getOrElse JObject()
    )
      .map(Serialization.writePretty(_))
      .map(Ok(_))
      .recover {
        case error: QueryAnalysisError => BadRequest(error.resolveError)
        case error: ErrorWithResolver => InternalServerError(error.resolveError)
      }
  }

  private def parseVariables(s: String): JValue = {
    if (s.trim == "" || s.trim == "null") JObject()
    else JsonMethods.parse(s)
  }

  private def isTracingEnabled(request: HttpServletRequest): Boolean = {
    request.headers.get("X-Apollo-Tracing").isDefined
  }

  post("/") {
    contentType = "application/json"
    new AsyncResult {
      override val is: Future[ActionResult] = {
        val InputQuery(query, variables, operation) = Serialization.read[InputQuery](request.body)
        executeQuery(query, variables, operation, isTracingEnabled(request))
      }
    }
  }
}
