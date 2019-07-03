package nl.knaw.dans.easy.properties.app.repository.demo

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State }
import nl.knaw.dans.easy.properties.app.repository.{ MutationErrorOr, QueryErrorOr, StateDao }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoStateDao(implicit repo: StateRepo, depositRepo: DepositRepo) extends StateDao with DemoDao with DebugEnhancedLogging {

  override def getById(id: String): QueryErrorOr[Option[State]] = {
    trace(id)
    getObjectById(id)
  }

  override def getCurrent(id: DepositId): QueryErrorOr[Option[State]] = {
    trace(id)
    getCurrentObject(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[State])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(id: DepositId): QueryErrorOr[Seq[State]] = {
    trace(id)
    getAllObjects(id)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[State])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, state: InputState): MutationErrorOr[State] = {
    trace(id, state)
    storeNode(id, state) {
      case (stateId, InputState(label, description, timestamp)) => State(stateId, label, description, timestamp)
    }
  }

  override def getDepositById(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    getDepositByObjectId(id)
  }
}
