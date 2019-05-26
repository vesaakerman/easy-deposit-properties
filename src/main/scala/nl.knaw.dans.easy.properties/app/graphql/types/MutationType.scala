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
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DepositorId, IngestStep, InputIngestStep, Timestamp }
import sangria.marshalling.FromInput.coercedScalaInput
import sangria.schema.{ Argument, Context, Field, ObjectType, OptionType, StringType, fields }

trait MutationType {
  this: DepositType with StateType with IngestStepType with Scalars =>

  private val depositIdArgument: Argument[DepositId] = Argument(
    name = "depositId",
    description = None, // TODO fill in the documentation
    defaultValue = None,
    argumentType = UUIDType,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val creationTimestampArgument: Argument[Timestamp] = Argument(
    name = "creationTimestamp",
    description = None, // TODO fill in the documentation
    defaultValue = None,
    argumentType = DateTimeType,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val depositorIdArgument: Argument[DepositorId] = Argument(
    name = "depositorId",
    description = None, // TODO fill in the documentation
    defaultValue = None,
    argumentType = StringType,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val stateArgument: Argument[InputState] = Argument(
    name = "state",
    description = None, // TODO fill in the documentation
    defaultValue = None,
    argumentType = InputStateType,
    fromInput = InputStateFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val ingestStepArgument: Argument[InputIngestStep] = Argument(
    name = "ingestStep",
    description = None, // TODO fill in the documentation
    defaultValue = None,
    argumentType = IngestStepInputType,
    fromInput = InputIngestStepFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )

  private val addDepositField: Field[DataContext, Unit] = Field(
    name = "addDeposit",
    description = Some("Register a new deposit with 'id', 'creationTimestamp' and 'depositId'."),
    arguments = List(
      depositIdArgument,
      creationTimestampArgument,
      depositorIdArgument,
    ),
    fieldType = OptionType(DepositType),
    resolve = addDeposit,
  )
  private val updateStateField: Field[DataContext, Unit] = Field(
    name = "updateState",
    description = Some("Update the state of the deposit identified by 'id'."),
    arguments = List(
      depositIdArgument,
      stateArgument,
    ),
    fieldType = OptionType(StateType),
    resolve = updateState,
  )
  private val updateIngestStepField: Field[DataContext, Unit] = Field(
    name = "updateIngestStep",
    description = Some("Update the ingest step of the deposit identified by 'id'."),
    arguments = List(
      depositIdArgument,
      ingestStepArgument,
    ),
    fieldType = OptionType(IngestStepType),
    resolve = updateIngestStep,
  )

  private def addDeposit(context: Context[DataContext, Unit]): Option[Deposit] = {
    val repository = context.ctx.deposits

    val id = context.arg(depositIdArgument)
    val creationTimestamp = context.arg(creationTimestampArgument)
    val depositorId = context.arg(depositorIdArgument)

    repository.addDeposit(Deposit(id, creationTimestamp, depositorId))
  }

  private def updateState(context: Context[DataContext, Unit]): Option[State] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val state = context.arg(stateArgument)

    repository.setState(depositId, state)
  }

  private def updateIngestStep(context: Context[DataContext, Unit]): Option[IngestStep] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val ingestStep = context.arg(ingestStepArgument)

    repository.setIngestStep(depositId, ingestStep)
  }

  implicit val MutationType: ObjectType[DataContext, Unit] = ObjectType(
    name = "Mutation",
    description = "The root query for implementing GraphQL mutations.",
    fields = fields[DataContext, Unit](
      addDepositField,
      updateStateField,
      updateIngestStepField,
    ),
  )
}
