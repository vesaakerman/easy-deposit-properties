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
package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, InputContentType }
import nl.knaw.dans.easy.properties.app.model.curation.{ Curation, InputCuration }
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStep, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield }
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DoiActionEvent, DoiRegisteredEvent, Timestamp }

trait DepositRepository {

  def getAllDeposits: QueryErrorOr[Seq[Deposit]]

  def getDeposits(filters: DepositFilters): QueryErrorOr[Seq[Deposit]]

  def getDepositsAggregated(filters: Seq[DepositFilters]): QueryErrorOr[Seq[(DepositFilters, Seq[Deposit])]]

  def getDeposit(id: DepositId): QueryErrorOr[Deposit]

  def addDeposit(deposit: Deposit): MutationErrorOr[Deposit]

  //

  def getLastModified(id: DepositId): QueryErrorOr[Option[Timestamp]]

  def getLastModifieds(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Timestamp])]]

  //

  def getStateById(id: String): QueryErrorOr[Option[State]]

  def getCurrentState(id: DepositId): QueryErrorOr[Option[State]]

  def getAllStates(id: DepositId): QueryErrorOr[Seq[State]]

  def getCurrentStates(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[State])]]

  def getAllStates(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[State])]]

  def setState(id: DepositId, state: InputState): MutationErrorOr[State]

  def getDepositByStateId(id: String): QueryErrorOr[Option[Deposit]]

  //

  def getIngestStepById(id: String): QueryErrorOr[Option[IngestStep]]

  def getCurrentIngestStep(id: DepositId): QueryErrorOr[Option[IngestStep]]

  def getAllIngestSteps(id: DepositId): QueryErrorOr[Seq[IngestStep]]

  def getCurrentIngestSteps(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[IngestStep])]]

  def getAllIngestSteps(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[IngestStep])]]

  def setIngestStep(id: DepositId, step: InputIngestStep): MutationErrorOr[IngestStep]

  def getDepositByIngestStepId(id: String): QueryErrorOr[Option[Deposit]]

  //

  def getIdentifierById(id: String): QueryErrorOr[Option[Identifier]]

  def getIdentifier(id: DepositId, idType: IdentifierType): QueryErrorOr[Option[Identifier]]

  def getIdentifier(idType: IdentifierType, idValue: String): QueryErrorOr[Option[Identifier]]

  def getIdentifiers(id: DepositId): QueryErrorOr[Seq[Identifier]]

  def getIdentifiersForTypes(ids: Seq[(DepositId, IdentifierType)]): QueryErrorOr[Seq[((DepositId, IdentifierType), Option[Identifier])]]

  def getIdentifiers(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Identifier])]]

  def addIdentifier(id: DepositId, identifier: InputIdentifier): MutationErrorOr[Identifier]

  def getDepositByIdentifierId(id: String): QueryErrorOr[Option[Deposit]]

  //

  def getCurrentDoiRegistered(id: DepositId): QueryErrorOr[Option[DoiRegisteredEvent]]

  def getCurrentDoisRegistered(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiRegisteredEvent])]]

  def getAllDoiRegistered(id: DepositId): QueryErrorOr[Seq[DoiRegisteredEvent]]

  def getAllDoisRegistered(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[DoiRegisteredEvent])]]

  def setDoiRegistered(id: DepositId, registered: DoiRegisteredEvent): MutationErrorOr[DoiRegisteredEvent]

  //

  def getCurrentDoiAction(id: DepositId): QueryErrorOr[Option[DoiActionEvent]]

  def getCurrentDoisAction(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiActionEvent])]]

  def getAllDoiAction(id: DepositId): QueryErrorOr[Seq[DoiActionEvent]]

  def getAllDoisAction(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[DoiActionEvent])]]

  def setDoiAction(id: DepositId, action: DoiActionEvent): MutationErrorOr[DoiActionEvent]

  //

  def getCurationById(id: String): QueryErrorOr[Option[Curation]]

  def getCurrentCuration(id: DepositId): QueryErrorOr[Option[Curation]]

  def getAllCurations(id: DepositId): QueryErrorOr[Seq[Curation]]

  def getCurrentCurations(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Curation])]]

  def getAllCurations(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Curation])]]

  def setCuration(id: DepositId, curation: InputCuration): MutationErrorOr[Curation]

  def getDepositByCurationId(id: String): QueryErrorOr[Option[Deposit]]

  //

  def getSpringfieldById(id: String): QueryErrorOr[Option[Springfield]]

  def getCurrentSpringfield(id: DepositId): QueryErrorOr[Option[Springfield]]

  def getCurrentSpringfields(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Springfield])]]

  def getAllSpringfields(id: DepositId): QueryErrorOr[Seq[Springfield]]

  def getAllSpringfields(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Springfield])]]

  def setSpringfield(id: DepositId, springfield: InputSpringfield): MutationErrorOr[Springfield]

  def getDepositBySpringfieldId(id: String): QueryErrorOr[Option[Deposit]]

  //

  def getContentTypeById(id: String): QueryErrorOr[Option[ContentType]]

  def getCurrentContentType(id: DepositId): QueryErrorOr[Option[ContentType]]

  def getCurrentContentTypes(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[ContentType])]]

  def getAllContentTypes(id: DepositId): QueryErrorOr[Seq[ContentType]]

  def getAllContentTypes(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[ContentType])]]

  def setContentType(id: DepositId, contentType: InputContentType): MutationErrorOr[ContentType]

  def getDepositByContentTypeId(id: String): QueryErrorOr[Option[Deposit]]
}
