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
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DepositorId }
import sangria.marshalling.FromInput.coercedScalaInput
import sangria.relay.{ Connection, ConnectionArgs }
import sangria.schema.{ Argument, Context, Field, ObjectType, OptionInputType, OptionType, StringType, fields }

trait QueryType {
  this: DepositType
    with DepositorType
    with StateType
    with IngestStepType
    with IdentifierGraphQLType
    with DoiEventTypes
    with CuratorType
    with CurationEventType
    with MetaTypes
    with NodeType
    with Scalars =>

  private val depositorIdArgument: Argument[Option[DepositorId]] = Argument(
    name = "id",
    description = Some("If provided, only show deposits from this depositor."),
    defaultValue = None,
    argumentType = OptionInputType(StringType),
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val depositIdArgument: Argument[DepositId] = Argument(
    name = "id",
    description = Some("The id for which to find the deposit"),
    defaultValue = None,
    argumentType = UUIDType,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val identifierTypeArgument: Argument[IdentifierType.Value] = Argument(
    name = "type",
    description = Some("The type of identifier to be found."),
    defaultValue = None,
    argumentType = IdentifierTypeType,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val identifierValueArgument: Argument[String] = Argument(
    name = "value",
    description = Some("The value of the identifier to be found."),
    defaultValue = None,
    argumentType = StringType,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )

  private val depositField: Field[DataContext, Unit] = Field(
    name = "deposit",
    description = Some("Get the technical metadata of the deposit identified by 'id'."),
    arguments = List(depositIdArgument),
    fieldType = OptionType(DepositType),
    resolve = getDeposit,
  )
  private val depositsField: Field[DataContext, Unit] = Field(
    name = "deposits",
    description = Some("List all registered deposits."),
    arguments = List(
      depositStateFilterArgument,
      depositIngestStepFilterArgument,
      depositDoiRegisteredFilterArgument,
      depositDoiActionFilterArgument,
      depositCuratorFilterArgument,
      depositIsNewVersionFilterArgument,
      depositCurationRequiredFilterArgument,
      depositCurationPerformedFilterArgument,
      optDepositOrderArgument,
    ) ++ Connection.Args.All,
    fieldType = OptionType(depositConnectionType),
    resolve = ctx => ExtendedConnection.connectionFromSeq(getDeposits(ctx), ConnectionArgs(ctx)),
  )
  private val depositorField: Field[DataContext, Unit] = Field(
    name = "depositor",
    description = Some("Get the technical metadata related to this depositor."),
    arguments = List(
      depositorIdArgument,
    ),
    fieldType = OptionType(DepositorType),
    resolve = getDepositor,
  )
  private val identifierField: Field[DataContext, Unit] = Field(
    name = "identifier",
    description = Some("Find an identifier with the given type and value."),
    arguments = List(
      identifierTypeArgument,
      identifierValueArgument,
    ),
    fieldType = OptionType(IdentifierObjectType),
    resolve = getIdentifier,
  )

  private def getDeposit(context: Context[DataContext, Unit]): Option[Deposit] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)

    repository.getDeposit(depositId)
  }

  private def getDeposits(context: Context[DataContext, Unit]): Seq[Deposit] = {
    val repository = context.ctx.deposits

    val stateInput = context.arg(depositStateFilterArgument)
    val ingestStepInput = context.arg(depositIngestStepFilterArgument)
    val doiRegistered = context.arg(depositDoiRegisteredFilterArgument)
    val doiAction = context.arg(depositDoiActionFilterArgument)
    val curator = context.arg(depositCuratorFilterArgument)
    val isNewVersion = context.arg(depositIsNewVersionFilterArgument)
    val curationRequired = context.arg(depositCurationRequiredFilterArgument)
    val curationPerformed = context.arg(depositCurationPerformedFilterArgument)
    val orderBy = context.arg(optDepositOrderArgument)

    val result = repository.getDeposits(
      stateFilter = stateInput,
      ingestStepFilter = ingestStepInput,
      doiRegisteredFilter = doiRegistered,
      doiActionFilter = doiAction,
      curatorFilter = curator,
      isNewVersionFilter = isNewVersion,
      curationRequiredFilter = curationRequired,
      curationPerformedFilter = curationPerformed,
    )

    orderBy.fold(result)(order => result.sorted(order.ordering))
  }

  private def getDepositor(context: Context[DataContext, Unit]): Option[DepositorId] = {
    context.arg(depositorIdArgument)
  }

  private def getIdentifier(context: Context[DataContext, Unit]): Option[Identifier] = {
    val repository = context.ctx.deposits

    val identifierType = context.arg(identifierTypeArgument)
    val identifierValue = context.arg(identifierValueArgument)

    repository.getIdentifier(identifierType, identifierValue)
  }

  implicit val QueryType: ObjectType[DataContext, Unit] = ObjectType(
    name = "Query",
    description = "The query root of easy-deposit-properties' GraphQL interface.",
    fields = fields[DataContext, Unit](
      depositField,
      depositsField,
      depositorField,
      identifierField,
      nodeField,
      nodesField,
    ),
  )
}
