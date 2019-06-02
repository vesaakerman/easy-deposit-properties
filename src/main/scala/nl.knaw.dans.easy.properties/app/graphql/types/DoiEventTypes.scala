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
import nl.knaw.dans.easy.properties.app.model.DoiAction.DoiAction
import nl.knaw.dans.easy.properties.app.model.SeriesFilter.SeriesFilter
import nl.knaw.dans.easy.properties.app.model.{ DepositDoiActionFilter, DepositDoiRegisteredFilter, DoiAction, DoiActionEvent, DoiRegisteredEvent, SeriesFilter, Timestamp }
import sangria.macros.derive._
import sangria.marshalling.FromInput._
import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput, ResultMarshaller }
import sangria.schema.{ Argument, EnumType, InputObjectType, ObjectType, OptionInputType }

trait DoiEventTypes {
  this: MetaTypes with Scalars =>

  val fetchCurrentDoisRegistered: CurrentFetcher[DoiRegisteredEvent] = fetchCurrent(_.deposits.getCurrentDoiRegistered, _.deposits.getCurrentDoisRegistered)
  val fetchAllDoisRegistered: AllFetcher[DoiRegisteredEvent] = fetchAll(_.deposits.getAllDoiRegistered, _.deposits.getAllDoisRegistered)
  val fetchCurrentDoisAction: CurrentFetcher[DoiActionEvent] = fetchCurrent(_.deposits.getCurrentDoiAction, _.deposits.getCurrentDoisAction)
  val fetchAllDoisAction: AllFetcher[DoiActionEvent] = fetchAll(_.deposits.getAllDoiAction, _.deposits.getAllDoisAction)

  implicit val DepositDoiRegisteredFilterType: InputObjectType[DepositDoiRegisteredFilter] = deriveInputObjectType(
    InputObjectTypeDescription("The label and filter to be used in searching for deposits by whether the DOI is registered."),
    DocumentInputField("value", "If provided, only show deposits with the same value for DOI registered."),
    DocumentInputField("filter", "Determine whether to search in current value for DOI registered (`LATEST`, default) or all current and past values (`ALL`)."),
  )
  implicit val DepositDoiRegisteredFilterFromInput: FromInput[DepositDoiRegisteredFilter] = new FromInput[DepositDoiRegisteredFilter] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): DepositDoiRegisteredFilter = {
      val ad = node.asInstanceOf[Map[String, Any]]

      DepositDoiRegisteredFilter(
        value = ad("value").asInstanceOf[Boolean],
        filter = ad("filter").asInstanceOf[Option[SeriesFilter]].getOrElse(SeriesFilter.LATEST),
      )
    }
  }

  implicit val DepositDoiActionFilterType: InputObjectType[DepositDoiActionFilter] = deriveInputObjectType(
    InputObjectTypeDescription("The label and filter to be used in searching for deposits by DOI registration action."),
    DocumentInputField("value", "If provided, only show deposits with the same value for DOI action."),
    DocumentInputField("filter", "Determine whether to search in current value for DOI action (`LATEST`, default) or all current and past values (`ALL`)."),
  )
  implicit val DepositDoiActionFilterFromInput: FromInput[DepositDoiActionFilter] = new FromInput[DepositDoiActionFilter] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): DepositDoiActionFilter = {
      val ad = node.asInstanceOf[Map[String, Any]]

      DepositDoiActionFilter(
        value = ad("value").asInstanceOf[DoiAction],
        filter = ad("filter").asInstanceOf[Option[SeriesFilter]].getOrElse(SeriesFilter.LATEST),
      )
    }
  }

  val depositDoiRegisteredFilterArgument: Argument[Option[DepositDoiRegisteredFilter]] = {
    Argument(
      name = "doiRegistered",
      argumentType = OptionInputType(DepositDoiRegisteredFilterType),
      description = Some("List only those deposits that have this specified value for DOI registered."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(DepositDoiRegisteredFilterFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }

  val depositDoiActionFilterArgument: Argument[Option[DepositDoiActionFilter]] = {
    Argument(
      name = "doiAction",
      argumentType = OptionInputType(DepositDoiActionFilterType),
      description = Some("List only those deposits that have this specified value for DOI action."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(DepositDoiActionFilterFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }

  implicit val DoiRegisteredEventType: ObjectType[DataContext, DoiRegisteredEvent] = deriveObjectType(
    ObjectTypeDescription("A DOI registration event related to a deposit"),
    DocumentField("value", "Whether the DOI is registered in DataCite."),
    DocumentField("timestamp", "The timestamp at which the DOI was registered in DataCite."),
  )

  implicit val InputDoiRegisteredEventType: InputObjectType[DoiRegisteredEvent] = deriveInputObjectType(
    InputObjectTypeName("DoiRegisteredEventInput"),
    InputObjectTypeDescription("A DOI registration event related to a deposit"),
    DocumentInputField("value", "Whether the DOI is registered in DataCite."),
    DocumentInputField("timestamp", "The timestamp at which the DOI was registered in DataCite."),
  )
  implicit val inputDoiRegisteredEventFromInput: FromInput[DoiRegisteredEvent] = new FromInput[DoiRegisteredEvent] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): DoiRegisteredEvent = {
      val ad = node.asInstanceOf[Map[String, Any]]

      DoiRegisteredEvent(
        value = ad("value").asInstanceOf[Boolean],
        timestamp = ad("timestamp").asInstanceOf[Timestamp],
      )
    }
  }

  implicit val DoiActionType: EnumType[DoiAction.Value] = deriveEnumType(
    EnumTypeDescription("Whether the DANS-DOI must be created or updated in the DataCite resolver."),
    DocumentValue("CREATE", "The DANS-DOI must be created in the DataCite resolver."),
    DocumentValue("UPDATE", "The DANS-DOI must be updated in the DataCite resolver."),
    DocumentValue("NONE", "None action must be taken for this DANS-DOI in the DataCite resolver."),
  )

  implicit val DoiActionEventType: ObjectType[DataContext, DoiActionEvent] = deriveObjectType(
    ObjectTypeDescription("A DOI action event related to a deposit"),
    DocumentField("value", "Whether the DOI must be 'created' or 'updated' when registering in DataCite."),
    DocumentField("timestamp", "The timestamp at which this value was added."),
  )

  implicit val InputDoiActionEventType: InputObjectType[DoiActionEvent] = deriveInputObjectType(
    InputObjectTypeName("DoiActionEventInput"),
    InputObjectTypeDescription("A DOI action event related to a deposit"),
    DocumentInputField("value", "Whether the DOI must be 'created' or 'updated' when registering in DataCite."),
    DocumentInputField("timestamp", "The timestamp at which this value was added."),
  )
  implicit val inputDoiActionEventFromInput: FromInput[DoiActionEvent] = new FromInput[DoiActionEvent] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): DoiActionEvent = {
      val ad = node.asInstanceOf[Map[String, Any]]

      DoiActionEvent(
        value = ad("value").asInstanceOf[DoiAction],
        timestamp = ad("timestamp").asInstanceOf[Timestamp],
      )
    }
  }
}
