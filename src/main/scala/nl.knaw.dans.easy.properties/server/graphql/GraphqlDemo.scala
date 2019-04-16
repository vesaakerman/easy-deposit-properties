package nl.knaw.dans.easy.properties.server.graphql

import java.util.UUID

import nl.knaw.dans.easy.properties.server.graphql.GraphqlTypes._
import org.json4s.ext.UUIDSerializer
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats, JValue }
import sangria.execution._
import sangria.macros._
import sangria.marshalling.json4s.native._
import sangria.schema._

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
