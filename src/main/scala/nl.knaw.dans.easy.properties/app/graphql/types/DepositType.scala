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
import nl.knaw.dans.easy.properties.app.graphql.relay.ExtendedConnection
import nl.knaw.dans.easy.properties.app.model.state.State
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositorId, IngestStep }
import sangria.macros.derive._
import sangria.relay._
import sangria.schema.{ Context, DeferredValue, Field, ObjectType, OptionType }

import scala.concurrent.ExecutionContext.Implicits.global

trait DepositType {
  this: DepositorType with StateType with IngestStepType with NodeType with MetaTypes with Scalars =>

  private val stateField: Field[DataContext, Deposit] = Field(
    name = "state",
    fieldType = OptionType(StateType),
    description = Option("The current state of the deposit."),
    resolve = getCurrentState,
  )
  private val statesField: Field[DataContext, Deposit] = Field(
    name = "states",
    description = Option("List all states of the deposit."),
    arguments = optStateOrderArgument :: Connection.Args.All,
    fieldType = OptionType(stateConnectionType),
    resolve = ctx => getAllStates(ctx).map(ExtendedConnection.connectionFromSeq(_, ConnectionArgs(ctx))),
  )
  private val ingestStepField: Field[DataContext, Deposit] = Field(
    name = "ingestStep",
    fieldType = OptionType(IngestStepType),
    description = Option("The current ingest step of the deposit."),
    resolve = getCurrentIngestStep,
  )
  private val ingestStepsField: Field[DataContext, Deposit] = Field(
    name = "ingestSteps",
    description = Option("List all ingest steps of the deposit."),
    arguments = optIngestStepOrderArgument :: Connection.Args.All,
    fieldType = OptionType(ingestStepConnectionType),
    resolve = ctx => getAllIngestSteps(ctx).map(ExtendedConnection.connectionFromSeq(_, ConnectionArgs(ctx))),
  )
  private val depositorField: Field[DataContext, Deposit] = Field(
    name = "depositor",
    fieldType = DepositorType,
    description = Option("Information about the depositor that submitted this deposit."),
    resolve = getDepositor,
  )

  private def getCurrentState(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[State]] = {
    val id = context.value.id

    DeferredValue(fetchCurrentStates.defer(id)).map { case (_, optState) => optState }
  }

  private def getAllStates(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[State]] = {
    val id = context.value.id
    val orderBy = context.arg(optStateOrderArgument)

    DeferredValue(fetchAllStates.defer(id))
      .map { case (_, states) => orderBy.fold(states)(order => states.sorted(order.ordering)) }
  }

  private def getCurrentIngestStep(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[IngestStep]] = {
    val id = context.value.id

    DeferredValue(fetchCurrentIngestSteps.defer(id)).map { case (_, optIngestStep) => optIngestStep }
  }

  private def getAllIngestSteps(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[IngestStep]] = {
    val id = context.value.id
    val orderBy = context.arg(optIngestStepOrderArgument)

    DeferredValue(fetchAllIngestSteps.defer(id))
      .map { case (_, ingestSteps) => orderBy.fold(ingestSteps)(order => ingestSteps.sorted(order.ordering)) }
  }

  private def getDepositor(context: Context[DataContext, Deposit]): DepositorId = {
    context.value.depositorId
  }

  implicit object DepositIdentifiable extends Identifiable[Deposit] {
    override def id(deposit: Deposit): String = deposit.id.toString
  }

  // lazy because we need it before being declared (in StateType)
  implicit lazy val DepositType: ObjectType[DataContext, Deposit] = deriveObjectType(
    ObjectTypeDescription("Contains all technical metadata about this deposit."),
    Interfaces[DataContext, Deposit](nodeInterface),
    RenameField("id", "depositId"),
    DocumentField("id", "The identifier of the deposit."),
    DocumentField("creationTimestamp", "The moment this deposit was created."),
    ExcludeFields("depositorId"),
    AddFields(
      Node.globalIdField[DataContext, Deposit],
      stateField,
      statesField,
      ingestStepField,
      ingestStepsField,
      depositorField,
    ),
  )

  lazy val ConnectionDefinition(_, depositConnectionType) = ExtendedConnection.definition[DataContext, ExtendedConnection, Deposit](
    name = "Deposit",
    nodeType = DepositType,
  )
}
