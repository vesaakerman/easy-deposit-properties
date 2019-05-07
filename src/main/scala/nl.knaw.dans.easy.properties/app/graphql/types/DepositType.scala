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
import nl.knaw.dans.easy.properties.app.model.Deposit
import sangria.macros.derive._
import sangria.schema._

import scala.concurrent.ExecutionContext.Implicits.global

trait DepositType {
  this: DepositorType with StateType with MetaTypes with Scalars =>

  // lazy because we need it before being declared (in StateType)
  implicit lazy val DepositType: ObjectType[DataContext, Deposit] = deriveObjectType(
    ObjectTypeDescription("Contains all technical metadata about this deposit."),
    DocumentField("id", "The identifier of the deposit."),
    DocumentField("creationTimestamp", "The moment this deposit was created."),
    ExcludeFields("depositorId"),
    AddFields(
      Field(
        name = "state",
        fieldType = OptionType(StateType),
        description = Option("The current state of the deposit."),
        resolve = c => DeferredValue(fetchCurrentStates.defer(c.value.id)).map { case (_, optState) => optState },
      ),
      Field(
        name = "states",
        fieldType = ListType(StateType),
        description = Option("List all states of the deposit."),
        arguments = optStateOrderArgument :: Nil,
        resolve = c => DeferredValue(fetchAllStates.defer(c.value.id))
          .map {
            case (_, states) =>
              c.arg(optStateOrderArgument)
                .fold(states)(order => states.sorted(order.ordering))
          },
      ),
      Field(
        name = "depositor",
        fieldType = DepositorType,
        description = Option("Information about the depositor that submitted this deposit."),
        resolve = c => Depositor(c.value.depositorId)(c.ctx.deposits),
      ),
    ),
  )
}
