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
import nl.knaw.dans.easy.properties.app.model.SeriesFilter.SeriesFilter
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, DepositCurationPerformedFilter, DepositCurationRequiredFilter, DepositIsNewVersionFilter, IsNewVersionEvent, SeriesFilter }
import sangria.macros.derive._
import sangria.marshalling.FromInput
import sangria.marshalling.FromInput._
import sangria.schema.{ Argument, InputObjectType, ObjectType, OptionInputType }

trait CurationEventType {
  this: MetaTypes with Scalars =>

  val fetchCurrentIsNewVersion: CurrentFetcher[IsNewVersionEvent] = fetchCurrent(_.deposits.getCurrentIsNewVersionAction, _.deposits.getCurrentIsNewVersionActions)
  val fetchAllIsNewVersion: AllFetcher[IsNewVersionEvent] = fetchAll(_.deposits.getAllIsNewVersionAction, _.deposits.getAllIsNewVersionActions)
  val fetchCurrentCurationRequired: CurrentFetcher[CurationRequiredEvent] = fetchCurrent(_.deposits.getCurrentCurationRequiredAction, _.deposits.getCurrentCurationRequiredActions)
  val fetchAllCurationRequired: AllFetcher[CurationRequiredEvent] = fetchAll(_.deposits.getAllCurationRequiredAction, _.deposits.getAllCurationRequiredActions)
  val fetchCurrentCurationPerformed: CurrentFetcher[CurationPerformedEvent] = fetchCurrent(_.deposits.getCurrentCurationPerformedAction, _.deposits.getCurrentCurationPerformedActions)
  val fetchAllCurationPerformed: AllFetcher[CurationPerformedEvent] = fetchAll(_.deposits.getAllCurationPerformedAction, _.deposits.getAllCurationPerformedActions)

  implicit val DepositIsNewVersionFilterType: InputObjectType[DepositIsNewVersionFilter] = deriveInputObjectType(
    InputObjectTypeDescription("The value and filter to be used in searching for deposits by 'is-new-version'."),
    DocumentInputField("isNewVersion", "If provided, only show deposits with this value for 'is-new-version'."),
    DocumentInputField("filter", "Determine whether to search in current values (`LATEST`, default) or all current and past values (`ALL`)."),
    RenameInputField("isNewVersion", "value"),
  )
  implicit val DepositIsNewVersionFilterFromInput: FromInput[DepositIsNewVersionFilter] = fromInput(ad => DepositIsNewVersionFilter(
    isNewVersion = ad("value").asInstanceOf[Boolean],
    filter = ad("filter").asInstanceOf[Option[SeriesFilter]].getOrElse(SeriesFilter.LATEST),
  ))

  implicit val DepositCurationRequiredFilterType: InputObjectType[DepositCurationRequiredFilter] = deriveInputObjectType(
    InputObjectTypeDescription("The value and filter to be used in searching for deposits by 'curation-required'."),
    DocumentInputField("curationRequired", "If provided, only show deposits with this value for 'curation-required'."),
    DocumentInputField("filter", "Determine whether to search in current values (`LATEST`, default) or all current and past values (`ALL`)."),
    RenameInputField("curationRequired", "value"),
  )
  implicit val DepositCurationRequiredFilterFromInput: FromInput[DepositCurationRequiredFilter] = fromInput(ad => DepositCurationRequiredFilter(
    curationRequired = ad("value").asInstanceOf[Boolean],
    filter = ad("filter").asInstanceOf[Option[SeriesFilter]].getOrElse(SeriesFilter.LATEST),
  ))

  implicit val DepositCurationPerformedFilterType: InputObjectType[DepositCurationPerformedFilter] = deriveInputObjectType(
    InputObjectTypeDescription("The value and filter to be used in searching for deposits by 'curation-performed'."),
    DocumentInputField("curationPerformed", "If provided, only show deposits with this value for 'curation-performed'."),
    DocumentInputField("filter", "Determine whether to search in current values (`LATEST`, default) or all current and past values (`ALL`)."),
    RenameInputField("curationPerformed", "value"),
  )
  implicit val DepositCurationPerformedFilterFromInput: FromInput[DepositCurationPerformedFilter] = fromInput(ad => DepositCurationPerformedFilter(
    curationPerformed = ad("value").asInstanceOf[Boolean],
    filter = ad("filter").asInstanceOf[Option[SeriesFilter]].getOrElse(SeriesFilter.LATEST),
  ))

  val depositIsNewVersionFilterArgument: Argument[Option[DepositIsNewVersionFilter]] = {
    Argument(
      name = "isNewVersion",
      argumentType = OptionInputType(DepositIsNewVersionFilterType),
      description = Some("List only those deposits that have this specified value for 'is-new-version'."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(DepositIsNewVersionFilterFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }

  val depositCurationRequiredFilterArgument: Argument[Option[DepositCurationRequiredFilter]] = {
    Argument(
      name = "curationRequired",
      argumentType = OptionInputType(DepositCurationRequiredFilterType),
      description = Some("List only those deposits that have this specified value for 'curation required'."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(DepositCurationRequiredFilterFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }

  val depositCurationPerformedFilterArgument: Argument[Option[DepositCurationPerformedFilter]] = {
    Argument(
      name = "curationPerformed",
      argumentType = OptionInputType(DepositCurationPerformedFilterType),
      description = Some("List only those deposits that have this specified value for 'curation performed'."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(DepositCurationPerformedFilterFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }

  implicit val IsNewVersionEventType: ObjectType[DataContext, IsNewVersionEvent] = deriveObjectType(
    ObjectTypeDescription("State whether this deposit is a new version, requiring a new DOI and deposit agreement to be generated by easy-ingest-flow."),
    DocumentField("isNewVersion", "True if the deposit is a new version."),
    DocumentField("timestamp", "The timestamp at which was decided that this is a new version."),
    RenameField("isNewVersion", "value"),
  )

  implicit val CurationRequiredEventType: ObjectType[DataContext, CurationRequiredEvent] = deriveObjectType(
    ObjectTypeDescription("Whether curation by data manager is required."),
    DocumentField("curationRequired", "True if curation by a data manager is required."),
    DocumentField("timestamp", "The timestamp at which was decided that this deposit requires curation."),
    RenameField("curationRequired", "value"),
  )

  implicit val CurationPerformedEventType: ObjectType[DataContext, CurationPerformedEvent] = deriveObjectType(
    ObjectTypeDescription("Whether the deposit has been curated by the data manager."),
    DocumentField("curationPerformed", "True if curation by the data manager has been performed."),
    DocumentField("timestamp", "The timestamp at which the curation was completed."),
    RenameField("curationPerformed", "value"),
  )
}
