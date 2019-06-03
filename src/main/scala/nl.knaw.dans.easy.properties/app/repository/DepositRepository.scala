package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, DepositContentTypeFilter, InputContentType }
import nl.knaw.dans.easy.properties.app.model.curator.{ Curator, DepositCuratorFilter, InputCurator }
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ DepositIngestStepFilter, IngestStep, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield }
import nl.knaw.dans.easy.properties.app.model.state.{ DepositStateFilter, InputState, State }
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, Deposit, DepositCurationPerformedFilter, DepositCurationRequiredFilter, DepositDoiActionFilter, DepositDoiRegisteredFilter, DepositId, DepositIsNewVersionFilter, DepositorId, DoiActionEvent, DoiRegisteredEvent, IsNewVersionEvent }

trait DepositRepository {

  def getAllDeposits: Seq[Deposit]

  def getDeposits(depositorId: Option[DepositorId] = Option.empty,
                  stateFilter: Option[DepositStateFilter] = Option.empty,
                  ingestStepFilter: Option[DepositIngestStepFilter] = Option.empty,
                  doiRegisteredFilter: Option[DepositDoiRegisteredFilter] = Option.empty,
                  doiActionFilter: Option[DepositDoiActionFilter] = Option.empty,
                  curatorFilter: Option[DepositCuratorFilter] = Option.empty,
                  isNewVersionFilter: Option[DepositIsNewVersionFilter] = Option.empty,
                  curationRequiredFilter: Option[DepositCurationRequiredFilter] = Option.empty,
                  curationPerformedFilter: Option[DepositCurationPerformedFilter] = Option.empty,
                  contentTypeFilter: Option[DepositContentTypeFilter] = Option.empty,
                 ): Seq[Deposit]

  def getDeposit(id: DepositId): Option[Deposit]

  def addDeposit(deposit: Deposit): Option[Deposit]

  //

  def getStateById(id: String): Option[State]

  def getCurrentState(id: DepositId): Option[State]

  def getAllStates(id: DepositId): Seq[State]

  def getCurrentStates(ids: Seq[DepositId]): Seq[(DepositId, Option[State])]

  def getAllStates(ids: Seq[DepositId]): Seq[(DepositId, Seq[State])]

  def setState(id: DepositId, state: InputState): Option[State]

  def getDepositByStateId(id: String): Option[Deposit]

  //

  def getIngestStepById(id: String): Option[IngestStep]

  def getCurrentIngestStep(id: DepositId): Option[IngestStep]

  def getAllIngestSteps(id: DepositId): Seq[IngestStep]

  def getCurrentIngestSteps(ids: Seq[DepositId]): Seq[(DepositId, Option[IngestStep])]

  def getAllIngestSteps(ids: Seq[DepositId]): Seq[(DepositId, Seq[IngestStep])]

  def setIngestStep(id: DepositId, step: InputIngestStep): Option[IngestStep]

  def getDepositByIngestStepId(id: String): Option[Deposit]

  //

  def getIdentifierById(id: String): Option[Identifier]

  def getIdentifier(id: DepositId, idType: IdentifierType): Option[Identifier]

  def getIdentifier(idType: IdentifierType, idValue: String): Option[Identifier]

  def getIdentifiers(id: DepositId): Seq[Identifier]

  def getIdentifiersForTypes(ids: Seq[(DepositId, IdentifierType)]): Seq[((DepositId, IdentifierType), Option[Identifier])]

  def getIdentifiers(ids: Seq[DepositId]): Seq[(DepositId, Seq[Identifier])]

  def addIdentifier(id: DepositId, identifier: InputIdentifier): Option[Identifier]

  def getDepositByIdentifierId(id: String): Option[Deposit]

  //

  def getCurrentDoiRegistered(id: DepositId): Option[DoiRegisteredEvent]

  def getCurrentDoisRegistered(ids: Seq[DepositId]): Seq[(DepositId, Option[DoiRegisteredEvent])]

  def getAllDoiRegistered(id: DepositId): Seq[DoiRegisteredEvent]

  def getAllDoisRegistered(ids: Seq[DepositId]): Seq[(DepositId, Seq[DoiRegisteredEvent])]

  def setDoiRegistered(id: DepositId, registered: DoiRegisteredEvent): Option[DoiRegisteredEvent]

  //

  def getCurrentDoiAction(id: DepositId): Option[DoiActionEvent]

  def getCurrentDoisAction(ids: Seq[DepositId]): Seq[(DepositId, Option[DoiActionEvent])]

  def getAllDoiAction(id: DepositId): Seq[DoiActionEvent]

  def getAllDoisAction(ids: Seq[DepositId]): Seq[(DepositId, Seq[DoiActionEvent])]

  def setDoiAction(id: DepositId, action: DoiActionEvent): Option[DoiActionEvent]

  //

  def getCuratorById(id: String): Option[Curator]

  def getCurrentCurator(id: DepositId): Option[Curator]

  def getAllCurators(id: DepositId): Seq[Curator]

  def getCurrentCurators(ids: Seq[DepositId]): Seq[(DepositId, Option[Curator])]

  def getAllCurators(ids: Seq[DepositId]): Seq[(DepositId, Seq[Curator])]

  def setCurator(id: DepositId, curator: InputCurator): Option[Curator]

  def getDepositByCuratorId(id: String): Option[Deposit]

  //

  def getCurrentIsNewVersionAction(id: DepositId): Option[IsNewVersionEvent]

  def getCurrentIsNewVersionActions(ids: Seq[DepositId]): Seq[(DepositId, Option[IsNewVersionEvent])]

  def getAllIsNewVersionAction(id: DepositId): Seq[IsNewVersionEvent]

  def getAllIsNewVersionActions(ids: Seq[DepositId]): Seq[(DepositId, Seq[IsNewVersionEvent])]

  def setIsNewVersionAction(id: DepositId, action: IsNewVersionEvent): Option[IsNewVersionEvent]

  //

  def getCurrentCurationRequiredAction(id: DepositId): Option[CurationRequiredEvent]

  def getCurrentCurationRequiredActions(ids: Seq[DepositId]): Seq[(DepositId, Option[CurationRequiredEvent])]

  def getAllCurationRequiredAction(id: DepositId): Seq[CurationRequiredEvent]

  def getAllCurationRequiredActions(ids: Seq[DepositId]): Seq[(DepositId, Seq[CurationRequiredEvent])]

  def setCurationRequiredAction(id: DepositId, action: CurationRequiredEvent): Option[CurationRequiredEvent]

  //

  def getCurrentCurationPerformedAction(id: DepositId): Option[CurationPerformedEvent]

  def getCurrentCurationPerformedActions(ids: Seq[DepositId]): Seq[(DepositId, Option[CurationPerformedEvent])]

  def getAllCurationPerformedAction(id: DepositId): Seq[CurationPerformedEvent]

  def getAllCurationPerformedActions(ids: Seq[DepositId]): Seq[(DepositId, Seq[CurationPerformedEvent])]

  def setCurationPerformedAction(id: DepositId, action: CurationPerformedEvent): Option[CurationPerformedEvent]

  //

  def getSpringfieldById(id: String): Option[Springfield]

  def getCurrentSpringfield(id: DepositId): Option[Springfield]

  def getCurrentSpringfields(ids: Seq[DepositId]): Seq[(DepositId, Option[Springfield])]

  def getAllSpringfields(id: DepositId): Seq[Springfield]

  def getAllSpringfields(ids: Seq[DepositId]): Seq[(DepositId, Seq[Springfield])]

  def setSpringfield(id: DepositId, springfield: InputSpringfield): Option[Springfield]

  def getDepositBySpringfieldId(id: String): Option[Deposit]

  //

  def getContentTypeById(id: String): Option[ContentType]

  def getCurrentContentType(id: DepositId): Option[ContentType]

  def getCurrentContentTypes(ids: Seq[DepositId]): Seq[(DepositId, Option[ContentType])]

  def getAllContentTypes(id: DepositId): Seq[ContentType]

  def getAllContentTypes(ids: Seq[DepositId]): Seq[(DepositId, Seq[ContentType])]

  def setContentType(id: DepositId, contentType: InputContentType): Option[ContentType]

  def getDepositByContentTypeId(id: String): Option[Deposit]
}
