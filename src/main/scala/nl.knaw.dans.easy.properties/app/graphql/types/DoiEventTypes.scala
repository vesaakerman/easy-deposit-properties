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
package nl.knaw.dans.easy.properties.app.graphql.types

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.{ DepositId, DoiActionEvent, DoiRegisteredEvent, Timestamp }
import sangria.execution.deferred.{ Fetcher, HasId }
import sangria.macros.derive._
import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput, ResultMarshaller }
import sangria.schema.{ InputObjectType, ObjectType }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DoiEventTypes {
  this: Scalars =>

  implicit val doisCurrentRegisteredHasId: HasId[(DepositId, Option[DoiRegisteredEvent]), DepositId] = HasId { case (id, _) => id }
  implicit val doisAllRegisteredHasId: HasId[(DepositId, Seq[DoiRegisteredEvent]), DepositId] = HasId { case (id, _) => id }
  implicit val doisCurrentActionHasId: HasId[(DepositId, Option[DoiActionEvent]), DepositId] = HasId { case (id, _) => id }
  implicit val doisAllActionHasId: HasId[(DepositId, Seq[DoiActionEvent]), DepositId] = HasId { case (id, _) => id }

  val fetchCurrentDoisRegistered = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => Future {
    ids match {
      case Seq() => Seq.empty
      case Seq(depositId) => Seq(depositId -> ctx.deposits.getCurrentDoiRegistered(depositId))
      case _ => ctx.deposits.getCurrentDoisRegistered(ids)
    }
  })
  val fetchAllDoisRegistered = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => Future {
    ids match {
      case Seq() => Seq.empty
      case Seq(depositId) => Seq(depositId -> ctx.deposits.getAllDoiRegistered(depositId))
      case _ => ctx.deposits.getAllDoisRegistered(ids)
    }
  })
  val fetchCurrentDoisAction = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => Future {
    ids match {
      case Seq() => Seq.empty
      case Seq(depositId) => Seq(depositId -> ctx.deposits.getCurrentDoiAction(depositId))
      case _ => ctx.deposits.getCurrentDoisAction(ids)
    }
  })
  val fetchAllDoisAction = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => Future {
    ids match {
      case Seq() => Seq.empty
      case Seq(depositId) => Seq(depositId -> ctx.deposits.getAllDoiAction(depositId))
      case _ => ctx.deposits.getAllDoisAction(ids)
    }
  })

  implicit val DoiRegisteredEventType: ObjectType[DataContext, DoiRegisteredEvent] = deriveObjectType(
    ObjectTypeDescription("A DOI registration event related to a deposit"),
    DocumentField("value", "Whether the DOI is registered in DataCite."),
    DocumentField("timestamp", "The timestamp at which the DOI was registered in DataCite."),
  )

  implicit val InputDoiRegisteredEventType: InputObjectType[DoiRegisteredEvent] = deriveInputObjectType(
    InputObjectTypeName("DoiRegisteredEventInput"),
    InputObjectTypeDescription("A DOI registration event related to a deposit"),
    DocumentInputField("value", "Whether the DOI is registered in DataCite."),
    DocumentInputField("timestamp", "The timestamp at which the DOI was registered in DataCite."),
  )
  implicit val inputDoiRegisteredEventFromInput: FromInput[DoiRegisteredEvent] = new FromInput[DoiRegisteredEvent] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): DoiRegisteredEvent = {
      val ad = node.asInstanceOf[Map[String, Any]]

      DoiRegisteredEvent(
        value = ad("value").asInstanceOf[String],
        timestamp = ad("timestamp").asInstanceOf[Timestamp],
      )
    }
  }

  implicit val DoiActionEventType: ObjectType[DataContext, DoiActionEvent] = deriveObjectType(
    ObjectTypeDescription("A DOI action event related to a deposit"),
    DocumentField("value", "Whether the DOI must be 'created' or 'updated' when registering in DataCite."),
    DocumentField("timestamp", "The timestamp at which this value was added."),
  )

  implicit val InputDoiActionEventType: InputObjectType[DoiActionEvent] = deriveInputObjectType(
    InputObjectTypeName("DoiRegisteredEventInput"),
    InputObjectTypeDescription("A DOI action event related to a deposit"),
    DocumentInputField("value", "Whether the DOI must be 'created' or 'updated' when registering in DataCite."),
    DocumentInputField("timestamp", "The timestamp at which this value was added."),
  )
  implicit val inputDoiActionEventFromInput: FromInput[DoiActionEvent] = new FromInput[DoiActionEvent] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): DoiActionEvent = {
      val ad = node.asInstanceOf[Map[String, Any]]

      DoiActionEvent(
        value = ad("value").asInstanceOf[String],
        timestamp = ad("timestamp").asInstanceOf[Timestamp],
      )
    }
  }
}
