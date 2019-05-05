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
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DepositorId, State, Timestamp }
import sangria.macros.derive.{ GraphQLDescription, GraphQLField, deriveContextObjectType }
import sangria.schema.ObjectType

trait MutationType {
  this: ModelTypes with Scalars =>

  @GraphQLDescription("The root query for implementing GraphQL mutations.")
  trait Mutation {
    val repository: DepositRepository

    @GraphQLField
    @GraphQLDescription("Register a new deposit with 'id', 'creationTimestamp' and 'depositId'.")
    def addDeposit(id: DepositId, creationTimestamp: Timestamp, depositorId: DepositorId): Option[Deposit] = {
      repository.addDeposit(Deposit(id, creationTimestamp, depositorId))
    }

    @GraphQLField
    @GraphQLDescription("Update the state of the deposit identified by 'id'.")
    def updateState(id: DepositId, state: State): Option[Deposit] = {
      repository.setState(id, state)
    }
  }

  object Mutation {
    def apply(repo: DepositRepository): Mutation = new Mutation {
      override val repository: DepositRepository = repo
    }
  }

  implicit val MutationType: ObjectType[DataContext, Unit] = deriveContextObjectType[DataContext, Mutation, Unit](ctx => Mutation(ctx.deposits))
}
