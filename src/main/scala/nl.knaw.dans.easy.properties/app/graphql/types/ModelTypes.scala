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
import nl.knaw.dans.easy.properties.app.model.State.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, State, Timestamp }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import sangria.execution.deferred.{ Fetcher, HasId }
import sangria.macros.derive._
import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput, ResultMarshaller }
import sangria.schema._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ModelTypes extends DebugEnhancedLogging {
  this: DepositorType with MetaTypes with Scalars =>

  implicit val StateLabelType: EnumType[StateLabel.Value] = deriveEnumType(
    EnumTypeDescription("The label identifying the state of a deposit."),
    DocumentValue("DRAFT", "Open for additional data."),
    DocumentValue("UPLOADED", "Deposit upload has been completed."),
    DocumentValue("FINALIZING", "Closed and being checked for validity."),
    DocumentValue("INVALID", "Does not contain a valid bag."),
    DocumentValue("SUBMITTED", "Valid and waiting for processing by easy-ingest-flow, or being processed in it."),
    DocumentValue("IN_REVIEW", "Currently undergoing curation by the datamanagers."),
    DocumentValue("REJECTED", "Did not meet the requirements set by easy-ingest-flow for this type of deposit."),
    DocumentValue("FAILED", "Failed to be archived because of some unexpected condition. It may be possible to manually fix this."),
    DocumentValue("FEDORA_ARCHIVED", "Was successfully archived in the Fedora Archive."),
    DocumentValue("ARCHIVED", "Was successfully archived in the data vault."),
  )
  implicit val currentStateHasId: HasId[(DepositId, Option[State]), DepositId] = HasId { case (id, _) => id }
  implicit val allStatesHasId: HasId[(DepositId, Seq[State]), DepositId] = HasId { case (id, _) => id }

  val fetchCurrentStates = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => Future {
    ctx.deposits.getCurrentStates(ids)
  })
  val fetchAllStates = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => Future {
    ctx.deposits.getAllStates(ids)
  })

  implicit val StateType: ObjectType[DataContext, State] = deriveObjectType(
    ObjectTypeDescription("The state of the deposit."),
    DocumentField("label", "The state label of the deposit."),
    DocumentField("description", "Additional information about the state."),
    DocumentField("timestamp", "The timestamp at which the deposit got into this state."),
    AddFields(
      Field(
        name = "deposit",
        fieldType = ListType(DepositType),
        description = Option("List all deposits with the same state current label."),
        arguments = optDepositOrderArgument :: Nil,
        resolve = c => {
          val result = c.ctx.deposits.getDepositsByCurrentState(c.value.label)
          c.arg(optDepositOrderArgument)
            .fold(result)(order => result.sorted(order.ordering))
        },
      ),
    ),
  )

  implicit val StateInputType: InputObjectType[State] = deriveInputObjectType(
    InputObjectTypeName("StateInput"),
    InputObjectTypeDescription("The state of a deposit"),
    DocumentInputField("label", "The state label of the deposit."),
    DocumentInputField("description", "Additional information about the state."),
    DocumentInputField("timestamp", "The timestamp at which the deposit got into this state."),
  )
  implicit val StateFromInput: FromInput[State] = new FromInput[State] {
    override val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): State = {
      val ad = node.asInstanceOf[Map[String, Any]]

      State(
        label = ad("label").asInstanceOf[StateLabel.Value],
        description = ad("description").asInstanceOf[String],
        timestamp = ad("timestamp").asInstanceOf[Timestamp],
      )
    }
  }

  // lazy because we need it before being declared (in StateType)
  implicit lazy val DepositType: ObjectType[DataContext, Deposit] = deriveObjectType(
    ObjectTypeDescription("Contains all technical metadata about this deposit."),
    DocumentField("id", "The identifier of the deposit."),
    DocumentField("creationTimestamp", "The moment this deposit was created."),
    ExcludeFields("depositorId"),
    AddFields(
      Field(
        name = "state",
        fieldType = OptionType(StateType),
        description = Option("The current state of the deposit."),
        resolve = c => DeferredValue(fetchCurrentStates.defer(c.value.id)).map { case (_, optState) => optState },
      ),
      Field(
        name = "states",
        fieldType = ListType(StateType),
        description = Option("List all states of the deposit."),
        arguments = optStateOrderArgument :: Nil,
        resolve = c => DeferredValue(fetchAllStates.defer(c.value.id))
          .map {
            case (_, states) =>
              c.arg(optStateOrderArgument)
                .fold(states)(order => states.sorted(order.ordering))
          },
      ),
      Field(
        name = "depositor",
        fieldType = DepositorType,
        description = Option("Information about the depositor that submitted this deposit."),
        resolve = c => Depositor(c.value.depositorId)(c.ctx.deposits),
      ),
    ),
  )
}
