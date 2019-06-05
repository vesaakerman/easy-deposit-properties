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

import nl.knaw.dans.easy.properties.app.graphql.error.MutationError
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, InputContentType }
import nl.knaw.dans.easy.properties.app.model.curator.{ Curator, InputCurator }
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStep, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield }
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State }
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, Deposit, DepositId, DoiActionEvent, DoiRegisteredEvent, IsNewVersionEvent }

trait DepositRepository {

  def getAllDeposits: Seq[Deposit]

  def getDeposits(filters: DepositFilters): Seq[Deposit]

  def getDepositsAggregated(filters: Seq[DepositFilters]): Seq[(DepositFilters, Seq[Deposit])]

  def getDeposit(id: DepositId): Option[Deposit]

  def addDeposit(deposit: Deposit): Either[MutationError, Deposit]

  //

  def getStateById(id: String): Option[State]

  def getCurrentState(id: DepositId): Option[State]

  def getAllStates(id: DepositId): Option[Seq[State]]

  def getCurrentStates(ids: Seq[DepositId]): Seq[(DepositId, Option[State])]

  def getAllStates(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[State]])]

  def setState(id: DepositId, state: InputState): Either[MutationError, State]

  def getDepositByStateId(id: String): Option[Deposit]

  //

  def getIngestStepById(id: String): Option[IngestStep]

  def getCurrentIngestStep(id: DepositId): Option[IngestStep]

  def getAllIngestSteps(id: DepositId): Option[Seq[IngestStep]]

  def getCurrentIngestSteps(ids: Seq[DepositId]): Seq[(DepositId, Option[IngestStep])]

  def getAllIngestSteps(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[IngestStep]])]

  def setIngestStep(id: DepositId, step: InputIngestStep): Either[MutationError, IngestStep]

  def getDepositByIngestStepId(id: String): Option[Deposit]

  //

  def getIdentifierById(id: String): Option[Identifier]

  def getIdentifier(id: DepositId, idType: IdentifierType): Option[Identifier]

  def getIdentifier(idType: IdentifierType, idValue: String): Option[Identifier]

  def getIdentifiers(id: DepositId): Option[Seq[Identifier]]

  def getIdentifiersForTypes(ids: Seq[(DepositId, IdentifierType)]): Seq[((DepositId, IdentifierType), Option[Identifier])]

  def getIdentifiers(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[Identifier]])]

  def addIdentifier(id: DepositId, identifier: InputIdentifier): Either[MutationError, Identifier]

  def getDepositByIdentifierId(id: String): Option[Deposit]

  //

  def getCurrentDoiRegistered(id: DepositId): Option[DoiRegisteredEvent]

  def getCurrentDoisRegistered(ids: Seq[DepositId]): Seq[(DepositId, Option[DoiRegisteredEvent])]

  def getAllDoiRegistered(id: DepositId): Option[Seq[DoiRegisteredEvent]]

  def getAllDoisRegistered(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[DoiRegisteredEvent]])]

  def setDoiRegistered(id: DepositId, registered: DoiRegisteredEvent): Either[MutationError, DoiRegisteredEvent]

  //

  def getCurrentDoiAction(id: DepositId): Option[DoiActionEvent]

  def getCurrentDoisAction(ids: Seq[DepositId]): Seq[(DepositId, Option[DoiActionEvent])]

  def getAllDoiAction(id: DepositId): Option[Seq[DoiActionEvent]]

  def getAllDoisAction(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[DoiActionEvent]])]

  def setDoiAction(id: DepositId, action: DoiActionEvent): Either[MutationError, DoiActionEvent]

  //

  def getCuratorById(id: String): Option[Curator]

  def getCurrentCurator(id: DepositId): Option[Curator]

  def getAllCurators(id: DepositId): Option[Seq[Curator]]

  def getCurrentCurators(ids: Seq[DepositId]): Seq[(DepositId, Option[Curator])]

  def getAllCurators(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[Curator]])]

  def setCurator(id: DepositId, curator: InputCurator): Either[MutationError, Curator]

  def getDepositByCuratorId(id: String): Option[Deposit]

  //

  def getCurrentIsNewVersionAction(id: DepositId): Option[IsNewVersionEvent]

  def getCurrentIsNewVersionActions(ids: Seq[DepositId]): Seq[(DepositId, Option[IsNewVersionEvent])]

  def getAllIsNewVersionAction(id: DepositId): Option[Seq[IsNewVersionEvent]]

  def getAllIsNewVersionActions(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[IsNewVersionEvent]])]

  def setIsNewVersionAction(id: DepositId, action: IsNewVersionEvent): Either[MutationError, IsNewVersionEvent]

  //

  def getCurrentCurationRequiredAction(id: DepositId): Option[CurationRequiredEvent]

  def getCurrentCurationRequiredActions(ids: Seq[DepositId]): Seq[(DepositId, Option[CurationRequiredEvent])]

  def getAllCurationRequiredAction(id: DepositId): Option[Seq[CurationRequiredEvent]]

  def getAllCurationRequiredActions(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[CurationRequiredEvent]])]

  def setCurationRequiredAction(id: DepositId, action: CurationRequiredEvent): Either[MutationError, CurationRequiredEvent]

  //

  def getCurrentCurationPerformedAction(id: DepositId): Option[CurationPerformedEvent]

  def getCurrentCurationPerformedActions(ids: Seq[DepositId]): Seq[(DepositId, Option[CurationPerformedEvent])]

  def getAllCurationPerformedAction(id: DepositId): Option[Seq[CurationPerformedEvent]]

  def getAllCurationPerformedActions(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[CurationPerformedEvent]])]

  def setCurationPerformedAction(id: DepositId, action: CurationPerformedEvent): Either[MutationError, CurationPerformedEvent]

  //

  def getSpringfieldById(id: String): Option[Springfield]

  def getCurrentSpringfield(id: DepositId): Option[Springfield]

  def getCurrentSpringfields(ids: Seq[DepositId]): Seq[(DepositId, Option[Springfield])]

  def getAllSpringfields(id: DepositId): Option[Seq[Springfield]]

  def getAllSpringfields(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[Springfield]])]

  def setSpringfield(id: DepositId, springfield: InputSpringfield): Either[MutationError, Springfield]

  def getDepositBySpringfieldId(id: String): Option[Deposit]

  //

  def getContentTypeById(id: String): Option[ContentType]

  def getCurrentContentType(id: DepositId): Option[ContentType]

  def getCurrentContentTypes(ids: Seq[DepositId]): Seq[(DepositId, Option[ContentType])]

  def getAllContentTypes(id: DepositId): Option[Seq[ContentType]]

  def getAllContentTypes(ids: Seq[DepositId]): Seq[(DepositId, Option[Seq[ContentType]])]

  def setContentType(id: DepositId, contentType: InputContentType): Either[MutationError, ContentType]

  def getDepositByContentTypeId(id: String): Option[Deposit]
}
