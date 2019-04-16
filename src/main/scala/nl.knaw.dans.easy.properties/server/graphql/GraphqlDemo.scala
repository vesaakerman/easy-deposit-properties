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
package nl.knaw.dans.easy.properties.server.graphql

import nl.knaw.dans.easy.properties.server.graphql.GraphqlTypes._
import org.json4s.ext.UUIDSerializer
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats, JValue }
import sangria.execution._
import sangria.macros._
import sangria.marshalling.json4s.native._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.language.postfixOps

object GraphqlDemo extends App {

  val query1 =
    graphql"""
      {
        deposit(id: "00000000-0000-0000-0000-000000000002") {
          id
          state {
            label
            description
          }
        }
      }
    """
  val query2 =
    graphql"""
      {
        deposits {
          id
          state {
            label
            description
          }
        }
      }
    """
  val mutation1 =
    graphql"""
      mutation {
        state(id: "00000000-0000-0000-0000-000000000002", label: INVALID, description: "your deposit is invalid") {
          id
          state {
            label
            description
          }
        }
      }
    """

  // STEP: Execute query against the schema

  private val ctx = SchemaType(new DemoRepository {})
  private implicit val jsonFormats: Formats = new DefaultFormats {} + UUIDSerializer

  val result0: Future[JValue] = Executor.execute(schema, query2, ctx)
  println(Serialization.writePretty(Await.result(result0, 1 second)))
  
  val result1: Future[JValue] = Executor.execute(schema, mutation1, ctx)
  println(Serialization.writePretty(Await.result(result1, 1 second)))

  val result2: Future[JValue] = Executor.execute(schema, query2, ctx)
  println(Serialization.writePretty(Await.result(result2, 1 second)))
}
