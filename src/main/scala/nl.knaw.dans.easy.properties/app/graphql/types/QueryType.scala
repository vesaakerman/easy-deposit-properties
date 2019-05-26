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
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DepositorId }
import sangria.marshalling.FromInput.coercedScalaInput
import sangria.relay.{ Connection, ConnectionArgs }
import sangria.schema.{ Argument, Context, Field, ObjectType, OptionInputType, OptionType, StringType, fields }

trait QueryType {
  this: MetaTypes with DepositType with DepositorType with StateType with NodeType with Scalars =>

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
      stateInputArgument,
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

  private def getDeposit(context: Context[DataContext, Unit]): Option[Deposit] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)

    repository.getDeposit(depositId)
  }

  private def getDeposits(context: Context[DataContext, Unit]): Seq[Deposit] = {
    val repository = context.ctx.deposits

    val stateInput = context.arg(stateInputArgument)
    val orderBy = context.arg(optDepositOrderArgument)

    val result = stateInput match {
      case Some(StateInput(label, StateFilter.LATEST)) =>
        repository.getDepositsByCurrentState(label)
      case Some(StateInput(label, StateFilter.ALL)) =>
        repository.getDepositsByAllStates(label)
      case None =>
        repository.getAllDeposits
    }
    orderBy.fold(result)(order => result.sorted(order.ordering))
  }

  private def getDepositor(context: Context[DataContext, Unit]): Option[DepositorId] = {
    context.arg(depositorIdArgument)
  }

  implicit val QueryType: ObjectType[DataContext, Unit] = ObjectType(
    name = "Query",
    description = "The query root of easy-deposit-properties' GraphQL interface.",
    fields = fields[DataContext, Unit](
      depositField,
      depositsField,
      depositorField,
      nodeField,
      nodesField,
    ),
  )
}
