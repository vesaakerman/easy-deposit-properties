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
import nl.knaw.dans.easy.properties.app.model.state.{ DepositStateFilter, StateFilter }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositorId }
import sangria.relay.{ Connection, ConnectionArgs }
import sangria.schema.{ Context, Field, ObjectType, OptionType, StringType, fields }

trait DepositorType {
  this: DepositType with StateType with NodeType with MetaTypes =>

  private val depositorIdField: Field[DataContext, DepositorId] = Field(
    name = "depositorId",
    description = Some("The EASY account of the depositor."),
    fieldType = StringType,
    resolve = ctx => ctx.value,
  )
  private val depositsField: Field[DataContext, DepositorId] = Field(
    name = "deposits",
    description = Some("List all deposits originating from the same depositor."),
    arguments = List(
      depositStateFilterArgument,
      optDepositOrderArgument,
    ) ++ Connection.Args.All,
    fieldType = OptionType(depositConnectionType),
    resolve = ctx => ExtendedConnection.connectionFromSeq(getDeposits(ctx), ConnectionArgs(ctx)),
  )

  private def getDeposits(context: Context[DataContext, DepositorId]): Seq[Deposit] = {
    val repository = context.ctx.deposits

    val depositorId = context.value
    val stateInput = context.arg(depositStateFilterArgument)
    val orderBy = context.arg(optDepositOrderArgument)

    val result = stateInput match {
      case Some(DepositStateFilter(label, StateFilter.LATEST)) =>
        repository.getDepositsByDepositorAndCurrentState(depositorId, label)
      case Some(DepositStateFilter(label, StateFilter.ALL)) =>
        repository.getDepositsByDepositorAndAllStates(depositorId, label)
      case None =>
        repository.getDepositsByDepositor(depositorId)
    }

    orderBy.fold(result)(order => result.sorted(order.ordering))
  }

  implicit val DepositorType: ObjectType[DataContext, DepositorId] = ObjectType(
    name = "Depositor",
    description = "Information about the depositor that submitted this deposit.",
    fields = fields[DataContext, DepositorId](
      depositorIdField,
      depositsField,
    ),
  )
}
