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
import nl.knaw.dans.easy.properties.app.model.curator.{ Curator, InputCurator }
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStep, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield }
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State }
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, Deposit, DepositId, DoiActionEvent, DoiRegisteredEvent, IsNewVersionEvent }

trait DepositRepository {

  def getAllDeposits: QueryErrorOr[Seq[Deposit]]

  def getDeposits(filters: DepositFilters): QueryErrorOr[Seq[Deposit]]

  def getDepositsAggregated(filters: Seq[DepositFilters]): QueryErrorOr[Seq[(DepositFilters, Seq[Deposit])]]

  def getDeposit(id: DepositId): QueryErrorOr[Deposit]

  def addDeposit(deposit: Deposit): MutationErrorOr[Deposit]

  //

  def getStateById(id: String): QueryErrorOr[Option[State]]

  def getCurrentState(id: DepositId): QueryErrorOr[Option[State]]

  def getAllStates(id: DepositId): QueryErrorOr[Option[Seq[State]]]

  def getCurrentStates(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[State])]]

  def getAllStates(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[State]])]]

  def setState(id: DepositId, state: InputState): MutationErrorOr[State]

  def getDepositByStateId(id: String): QueryErrorOr[Option[Deposit]]

  //

  def getIngestStepById(id: String): QueryErrorOr[Option[IngestStep]]

  def getCurrentIngestStep(id: DepositId): QueryErrorOr[Option[IngestStep]]

  def getAllIngestSteps(id: DepositId): QueryErrorOr[Option[Seq[IngestStep]]]

  def getCurrentIngestSteps(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[IngestStep])]]

  def getAllIngestSteps(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[IngestStep]])]]

  def setIngestStep(id: DepositId, step: InputIngestStep): MutationErrorOr[IngestStep]

  def getDepositByIngestStepId(id: String): QueryErrorOr[Option[Deposit]]

  //

  def getIdentifierById(id: String): QueryErrorOr[Option[Identifier]]

  def getIdentifier(id: DepositId, idType: IdentifierType): QueryErrorOr[Option[Identifier]]

  def getIdentifier(idType: IdentifierType, idValue: String): QueryErrorOr[Option[Identifier]]

  def getIdentifiers(id: DepositId): QueryErrorOr[Option[Seq[Identifier]]]

  def getIdentifiersForTypes(ids: Seq[(DepositId, IdentifierType)]): QueryErrorOr[Seq[((DepositId, IdentifierType), Option[Identifier])]]

  def getIdentifiers(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[Identifier]])]]

  def addIdentifier(id: DepositId, identifier: InputIdentifier): MutationErrorOr[Identifier]

  def getDepositByIdentifierId(id: String): QueryErrorOr[Option[Deposit]]

  //

  def getCurrentDoiRegistered(id: DepositId): QueryErrorOr[Option[DoiRegisteredEvent]]

  def getCurrentDoisRegistered(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiRegisteredEvent])]]

  def getAllDoiRegistered(id: DepositId): QueryErrorOr[Option[Seq[DoiRegisteredEvent]]]

  def getAllDoisRegistered(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[DoiRegisteredEvent]])]]

  def setDoiRegistered(id: DepositId, registered: DoiRegisteredEvent): MutationErrorOr[DoiRegisteredEvent]

  //

  def getCurrentDoiAction(id: DepositId): QueryErrorOr[Option[DoiActionEvent]]

  def getCurrentDoisAction(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiActionEvent])]]

  def getAllDoiAction(id: DepositId): QueryErrorOr[Option[Seq[DoiActionEvent]]]

  def getAllDoisAction(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[DoiActionEvent]])]]

  def setDoiAction(id: DepositId, action: DoiActionEvent): MutationErrorOr[DoiActionEvent]

  //

  def getCuratorById(id: String): QueryErrorOr[Option[Curator]]

  def getCurrentCurator(id: DepositId): QueryErrorOr[Option[Curator]]

  def getAllCurators(id: DepositId): QueryErrorOr[Option[Seq[Curator]]]

  def getCurrentCurators(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Curator])]]

  def getAllCurators(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[Curator]])]]

  def setCurator(id: DepositId, curator: InputCurator): MutationErrorOr[Curator]

  def getDepositByCuratorId(id: String): QueryErrorOr[Option[Deposit]]

  //

  def getCurrentIsNewVersionAction(id: DepositId): QueryErrorOr[Option[IsNewVersionEvent]]

  def getCurrentIsNewVersionActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[IsNewVersionEvent])]]

  def getAllIsNewVersionAction(id: DepositId): QueryErrorOr[Option[Seq[IsNewVersionEvent]]]

  def getAllIsNewVersionActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[IsNewVersionEvent]])]]

  def setIsNewVersionAction(id: DepositId, action: IsNewVersionEvent): MutationErrorOr[IsNewVersionEvent]

  //

  def getCurrentCurationRequiredAction(id: DepositId): QueryErrorOr[Option[CurationRequiredEvent]]

  def getCurrentCurationRequiredActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[CurationRequiredEvent])]]

  def getAllCurationRequiredAction(id: DepositId): QueryErrorOr[Option[Seq[CurationRequiredEvent]]]

  def getAllCurationRequiredActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[CurationRequiredEvent]])]]

  def setCurationRequiredAction(id: DepositId, action: CurationRequiredEvent): MutationErrorOr[CurationRequiredEvent]

  //

  def getCurrentCurationPerformedAction(id: DepositId): QueryErrorOr[Option[CurationPerformedEvent]]

  def getCurrentCurationPerformedActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[CurationPerformedEvent])]]

  def getAllCurationPerformedAction(id: DepositId): QueryErrorOr[Option[Seq[CurationPerformedEvent]]]

  def getAllCurationPerformedActions(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[CurationPerformedEvent]])]]

  def setCurationPerformedAction(id: DepositId, action: CurationPerformedEvent): MutationErrorOr[CurationPerformedEvent]

  //

  def getSpringfieldById(id: String): QueryErrorOr[Option[Springfield]]

  def getCurrentSpringfield(id: DepositId): QueryErrorOr[Option[Springfield]]

  def getCurrentSpringfields(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Springfield])]]

  def getAllSpringfields(id: DepositId): QueryErrorOr[Option[Seq[Springfield]]]

  def getAllSpringfields(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[Springfield]])]]

  def setSpringfield(id: DepositId, springfield: InputSpringfield): MutationErrorOr[Springfield]

  def getDepositBySpringfieldId(id: String): QueryErrorOr[Option[Deposit]]

  //

  def getContentTypeById(id: String): QueryErrorOr[Option[ContentType]]

  def getCurrentContentType(id: DepositId): QueryErrorOr[Option[ContentType]]

  def getCurrentContentTypes(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[ContentType])]]

  def getAllContentTypes(id: DepositId): QueryErrorOr[Option[Seq[ContentType]]]

  def getAllContentTypes(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Seq[ContentType]])]]

  def setContentType(id: DepositId, contentType: InputContentType): MutationErrorOr[ContentType]

  def getDepositByContentTypeId(id: String): QueryErrorOr[Option[Deposit]]
}
