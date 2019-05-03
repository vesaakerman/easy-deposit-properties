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
import nl.knaw.dans.easy.properties.app.model.State.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, State }
import sangria.execution.deferred.{ Fetcher, HasId }
import sangria.macros.derive._
import sangria.schema._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ModelTypes {
  this: DepositorConnectionType with MetaTypes with Scalars =>

  implicit val StateLabelType: EnumType[StateLabel.Value] = deriveEnumType()
  implicit val stateHasId: HasId[(DepositId, Option[State]), DepositId] = HasId { case (id, _) => id }

  val states = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => {
    Future { ctx.deposits.getStates(ids) }
  })

  implicit val StateType: ObjectType[DataContext, State] = deriveObjectType(
    ObjectTypeDescription("The state of the deposit."),
    DocumentField("label", "The state label of the deposit."),
    DocumentField("description", "Additional information about the state."),
    AddFields(
      Field(
        name = "deposit",
        fieldType = ListType(DepositType),
        description = Option(""),
        arguments = Argument("orderBy", OptionInputType(DepositOrderInputType)) :: Nil,
        resolve = c => {
          val result = c.ctx.deposits.getDepositByState(c.value.label)
          c.argOpt[DepositOrder]("orderBy")
            .fold(result)(order => result.sorted(order.ordering))
        },
      ),
    ),
  )

  // lazy because we need it before being declared (in StateType)
  implicit lazy val DepositType: ObjectType[DataContext, Deposit] = deriveObjectType(
    DocumentField("id", "The identifier of the deposit."),
    DocumentField("creationTimestamp", "The moment this deposit was created."),
    ExcludeFields("depositorId"),
    AddFields(
      Field(
        name = "state",
        fieldType = OptionType(StateType),
        description = Option("The state of the deposit."),
        resolve = c => DeferredValue(states.defer(c.value.id)).map { case (_, optState) => optState },
      ),
      Field(
        name = "depositor",
        fieldType = DepositorConnectionType,
        description = Option(""),
        resolve = c => DepositorConnection(c.value.depositorId)(c.ctx.deposits),
      )
    ),
  )
}
