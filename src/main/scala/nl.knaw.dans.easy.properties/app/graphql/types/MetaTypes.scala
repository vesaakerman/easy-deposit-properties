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

import nl.knaw.dans.easy.properties.app.graphql.ordering.{ DepositOrder, DepositOrderField, OrderDirection }
import nl.knaw.dans.easy.properties.app.model.SeriesFilter
import nl.knaw.dans.easy.properties.app.model.SeriesFilter.SeriesFilter
import sangria.macros.derive._
import sangria.marshalling.FromInput._
import sangria.marshalling.ToInput.ScalarToInput
import sangria.marshalling.{ FromInput, ToInput }
import sangria.schema.{ Argument, EnumType, InputObjectType, OptionInputType }

trait MetaTypes {

  implicit val SeriesFilterType: EnumType[SeriesFilter.Value] = deriveEnumType(
    EnumTypeDescription("Mark a query to only search through current states, or also to include past states."),
    DocumentValue("LATEST", "Only search through current states."),
    DocumentValue("ALL", "Search through both current and past states."),
  )
  implicit val SeriesFilterToInput: ToInput[SeriesFilter, _] = new ScalarToInput

  implicit val OrderDirectionType: EnumType[OrderDirection.Value] = deriveEnumType()

  implicit val DepositOrderFieldType: EnumType[DepositOrderField.Value] = deriveEnumType()
  implicit val DepositOrderInputType: InputObjectType[DepositOrder] = deriveInputObjectType(
    InputObjectTypeDescription("Ordering options for deposits"),
    DocumentInputField("field", "The field to order deposit by"),
    DocumentInputField("direction", "The ordering direction"),
  )
  implicit val DepositOrderFromInput: FromInput[DepositOrder] = fromInput(ad => DepositOrder(
    field = ad("field").asInstanceOf[DepositOrderField.Value],
    direction = ad("direction").asInstanceOf[OrderDirection.Value],
  ))
  val optDepositOrderArgument: Argument[Option[DepositOrder]] = {
    Argument(
      name = "orderBy",
      argumentType = OptionInputType(DepositOrderInputType),
      description = Some("Ordering options for the returned deposits."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(DepositOrderFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }
}
