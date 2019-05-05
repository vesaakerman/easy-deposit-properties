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
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositorId }
import sangria.macros.derive.{ GraphQLDescription, GraphQLField, deriveObjectType }
import sangria.schema.ObjectType

trait StateConnectionType {
  this: ModelTypes with MetaTypes =>

  @GraphQLDescription("Container for further querying with a given 'current state'.")
  trait StateConnection {
    @GraphQLField
    @GraphQLDescription("List all deposits that currently have this state.")
    def deposits(orderBy: Option[DepositOrder] = None): Seq[Deposit]

    @GraphQLField
    @GraphQLDescription("Select all deposits that currently have this state and were submitted by the given depositor.")
    def depositor(id: DepositorId): StateDepositorConnection
  }

  object StateConnection {
    def apply(label: StateLabel)(repo: DepositRepository): StateConnection = new StateConnection {
      override def deposits(orderBy: Option[DepositOrder]): Seq[Deposit] = {
        val result = repo.getDepositsByCurrentState(label)
        orderBy.fold(result)(order => result.sorted(order.ordering))
      }

      override def depositor(id: DepositorId): StateDepositorConnection = {
        StateDepositorConnection(label, id)(repo)
      }
    }
  }

  implicit val StateConnectionType: ObjectType[DataContext, StateConnection] = deriveObjectType()

  @GraphQLDescription("Container for further querying with a given state and depositorId.")
  trait StateDepositorConnection {
    @GraphQLField
    @GraphQLDescription("List all deposits with this state and depositor.")
    def deposits(orderBy: Option[DepositOrder] = None): Seq[Deposit]
  }

  object StateDepositorConnection {
    def apply(label: StateLabel, id: DepositorId)(repo: DepositRepository): StateDepositorConnection = (orderBy: Option[DepositOrder]) => {
      val result = repo.getDepositsByDepositorAndCurrentState(id, label)
      orderBy.fold(result)(order => result.sorted(order.ordering))
    }
  }

  implicit val StateDepositorConnectionType: ObjectType[DataContext, StateDepositorConnection] = deriveObjectType()
}
