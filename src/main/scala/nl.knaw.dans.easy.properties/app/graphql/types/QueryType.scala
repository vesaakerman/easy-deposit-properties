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
import nl.knaw.dans.easy.properties.app.model.State.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DepositorId }
import nl.knaw.dans.easy.properties.app.graphql.relay.ExtendedConnection
import sangria.marshalling.FromInput.coercedScalaInput
import sangria.relay.{ Connection, ConnectionArgs }
import sangria.schema.{ Argument, Context, Field, ObjectType, OptionInputType, OptionType, StringType, fields }

trait QueryType {
  this: MetaTypes with DepositConnectionType with DepositType with StateType with Scalars with NodeType =>

  private val stateArgument: Argument[Option[StateLabel]] = Argument(
    name = "state",
    description = Some("If provided, only show deposits with this state."),
    defaultValue = None,
    argumentType = OptionInputType(StateLabelType),
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val stateFilterArgument: Argument[StateFilter.StateFilter] = Argument(
    name = "stateFilter",
    description = Some("Determine whether to search in current states (`LATEST`, default) or all current and past states (`ALL`)."),
    argumentType = StateFilterType,
    defaultValue = Some(StateFilter.LATEST -> StateFilterToInput),
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val depositorIdArgument: Argument[Option[DepositorId]] = Argument(
    name = "depositorId",
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
      stateArgument,
      stateFilterArgument,
      depositorIdArgument,
      optDepositOrderArgument,
    ) ++ Connection.Args.All,
    fieldType = OptionType(depositConnectionType),
    resolve = ctx => ExtendedConnection.connectionFromSeq(getDeposits(ctx), ConnectionArgs(ctx)),
  )

  private def getDeposit(context: Context[DataContext, Unit]): Option[Deposit] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)

    repository.getDeposit(depositId)
  }

  private def getDeposits(context: Context[DataContext, Unit]): Seq[Deposit] = {
    val repository = context.ctx.deposits

    val state = context.arg(stateArgument)
    val stateFilter = context.arg(stateFilterArgument)
    val depositorId = context.arg(depositorIdArgument)
    val orderBy = context.arg(optDepositOrderArgument)

    val result = (state, depositorId) match {
      case (Some(label), Some(id)) if stateFilter == StateFilter.LATEST =>
        repository.getDepositsByDepositorAndCurrentState(id, label)
      case (Some(label), Some(id)) if stateFilter == StateFilter.ALL =>
        repository.getDepositsByDepositorAndAllStates(id, label)
      case (Some(label), None) if stateFilter == StateFilter.LATEST =>
        repository.getDepositsByCurrentState(label)
      case (Some(label), None) if stateFilter == StateFilter.ALL =>
        repository.getDepositsByAllStates(label)
      case (None, Some(id)) =>
        repository.getDepositsByDepositor(id)
      case (None, None) =>
        repository.getAllDeposits
    }
    orderBy.fold(result)(order => result.sorted(order.ordering))
  }

  implicit val QueryType: ObjectType[DataContext, Unit] = ObjectType(
    name = "Query",
    description = "The query root of easy-deposit-properties' GraphQL interface.",
    fields = fields[DataContext, Unit](
      depositField,
      depositsField,
      nodeField,
      nodesField,
    ),
  )
}
