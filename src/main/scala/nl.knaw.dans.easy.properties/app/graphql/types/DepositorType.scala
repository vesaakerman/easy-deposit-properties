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
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositorId }
import sangria.macros.derive.{ GraphQLDescription, GraphQLField, deriveObjectType }
import sangria.schema.ObjectType

trait DepositorType {
  this: MetaTypes with DepositType with Scalars =>

  @GraphQLDescription("Information about the depositor that submitted this deposit.")
  trait Depositor {
    @GraphQLField
    @GraphQLDescription("The EASY account of the depositor.")
    def depositorId: DepositorId

    @GraphQLField
    @GraphQLDescription("List all deposits originating from the same depositor.")
    def deposits(@GraphQLDescription("Ordering options for the returned deposits.")
                 orderBy: Option[DepositOrder] = None): Seq[Deposit]
  }

  object Depositor {
    def apply(dp: DepositorId)(repo: DepositRepository): Depositor = new Depositor {
      override def depositorId: DepositorId = dp

      override def deposits(orderBy: Option[DepositOrder] = None): Seq[Deposit] = {
        val result = repo.getDepositsByDepositor(dp)
        orderBy.fold(result)(order => result.sorted(order.ordering))
      }
    }
  }

  implicit val DepositorType: ObjectType[DataContext, Depositor] = deriveObjectType()
}
