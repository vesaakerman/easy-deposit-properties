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

import nl.knaw.dans.easy.properties.app.graphql.{ DataContext, DepositRepository }
import nl.knaw.dans.easy.properties.app.model.State.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DepositorId }
import sangria.macros.derive.{ GraphQLDescription, GraphQLField, deriveContextObjectType }
import sangria.schema.ObjectType

trait QueryType {
  this: DepositorType with StateConnectionType with MetaTypes with ModelTypes with Scalars =>

  @GraphQLDescription("The query root of easy-deposit-properties' GraphQL interface.")
  trait Query {
    val repository: DepositRepository

    @GraphQLField
    @GraphQLDescription("List all registered deposits.")
    def deposits(orderBy: Option[DepositOrder] = None): Seq[Deposit] = {
      val result = repository.getAllDeposits
      orderBy.fold(result)(order => result.sorted(order.ordering))
    }

    @GraphQLField
    @GraphQLDescription("Get the technical metadata of the deposit identified by 'id'.")
    def deposit(id: DepositId): Option[Deposit] = {
      repository.getDeposit(id)
    }

    @GraphQLField
    @GraphQLDescription("Select a depositor.")
    def depositor(depositor: DepositorId): Depositor = {
      Depositor(depositor)(repository)
    }

    @GraphQLField
    @GraphQLDescription("Lookup a state by its label.")
    def state(label: StateLabel): StateConnection = {
      StateConnection(label)(repository)
    }
  }

  object Query {
    def apply(repo: DepositRepository): Query = new Query {
      override val repository: DepositRepository = repo
    }
  }

  implicit val QueryType: ObjectType[DataContext, Unit] = deriveContextObjectType[DataContext, Query, Unit](ctx => Query(ctx.deposits))
}
