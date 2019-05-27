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
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType }
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStep
import nl.knaw.dans.easy.properties.app.model.state.State
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositorId }
import sangria.macros.derive._
import sangria.marshalling.FromInput.coercedScalaInput
import sangria.relay._
import sangria.schema.{ Argument, Context, DeferredValue, Field, ListType, ObjectType, OptionType }

import scala.concurrent.ExecutionContext.Implicits.global

trait DepositType {
  this: DepositorType with StateType with IngestStepType with IdentifierType with NodeType with MetaTypes with Scalars =>

  private val identifierTypeArgument: Argument[IdentifierType.Value] = Argument(
    name = "type",
    argumentType = IdentifierTypeType,
    description = Some("Find the identifier with this specific type."),
    defaultValue = None,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )

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
  private val identifierField: Field[DataContext, Deposit] = Field(
    name = "identifier",
    description = Some("Return the identifier of the given type related to this deposit"),
    arguments = List(identifierTypeArgument),
    fieldType = OptionType(IdentifierObjectType),
    resolve = getIdentifier,
  )
  private val identifiersField: Field[DataContext, Deposit] = Field(
    name = "identifiers",
    description = Some("List the identifiers related to this deposit"),
    fieldType = ListType(IdentifierObjectType),
    resolve = getIdentifiers,
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

  private def getIdentifier(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[Identifier]] = {
    val depositId = context.value.id
    val idType = context.arg(identifierTypeArgument)

    DeferredValue(fetchIdentifiersByType.defer(depositId -> idType))
      .map { case (_, identifier) => identifier }
  }

  private def getIdentifiers(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[Identifier]] = {
    val depositId = context.value.id

    DeferredValue(fetchIdentifiersByDepositId.defer(depositId))
      .map { case (_, identifiers) => identifiers }
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
      identifierField,
      identifiersField,
    ),
  )

  lazy val ConnectionDefinition(_, depositConnectionType) = ExtendedConnection.definition[DataContext, ExtendedConnection, Deposit](
    name = "Deposit",
    nodeType = DepositType,
  )
}