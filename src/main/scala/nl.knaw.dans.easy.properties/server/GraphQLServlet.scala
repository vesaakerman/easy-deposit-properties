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

import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import nl.knaw.dans.lib.logging.servlet._
import org.json4s.JsonDSL._
import org.json4s.ext.UUIDSerializer
import org.json4s.native.{ JsonMethods, Serialization }
import org.json4s.{ DefaultFormats, Formats, JValue }
import org.scalatra._
import sangria.ast.Document
import sangria.execution._
import sangria.execution.deferred.DeferredResolver
import sangria.marshalling.json4s.native._
import sangria.parser._
import sangria.schema.Schema

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps

class GraphQLServlet[Ctx](schema: Schema[Ctx, Unit],
                          ctx: Ctx,
                          deferredResolver: DeferredResolver[Ctx] = DeferredResolver.empty)
  extends ScalatraServlet
    with FutureSupport
    with ServletLogger
    with MaskedLogFormatter
    with LogResponseBodyOnError
    with DebugEnhancedLogging {

  override implicit protected def executor: ExecutionContext = ExecutionContext.global

  private implicit val jsonFormats: Formats = new DefaultFormats {} + UUIDSerializer

  post("/") {
    contentType = "application/json"

    val GraphQLInput(query, variables, operation) = Serialization.read[GraphQLInput](request.body)
    QueryParser.parse(query)(DeliveryScheme.Either)
      .fold({
        case e: SyntaxError => Future.successful(BadRequest(syntaxError(e)))
        case e => Future.failed(e)
      }, execute(variables, operation))
  }

  private def execute(variables: Option[String], operation: Option[String])(queryAst: Document): Future[ActionResult] = {
    Executor.execute(schema, queryAst, ctx,
      operationName = operation,
      variables = parseVariables(variables),
      deferredResolver = deferredResolver
    )
      .map(Serialization.writePretty(_))
      .map(Ok(_))
      .recover {
        case error: QueryAnalysisError => BadRequest(Serialization.write(error.resolveError))
        case error: ErrorWithResolver => InternalServerError(Serialization.write(error.resolveError))
      }
  }

  private def parseVariables(optS: Option[String]): JValue = {
    optS.filter(s => s.trim != "" && s.trim != "null")
      .map(JsonMethods.parse(_))
      .getOrElse(Nil)
  }

  private def syntaxError(error: SyntaxError): String = {
    Serialization.write {
      ("syntaxError" -> error.getMessage) ~
        ("locations" -> List(
          ("line" -> error.originalError.position.line) ~
            ("column" -> error.originalError.position.column)
        ))
    }
  }
}
