package nl.knaw.dans.easy.properties.app.repository.demo

import nl.knaw.dans.easy.properties.app.model.{ DepositId, DoiActionEvent }
import nl.knaw.dans.easy.properties.app.repository.{ DoiActionDao, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoDoiActionDao(implicit repo: DoiActionRepo, depositRepo: DepositRepo) extends DoiActionDao with DemoDao with DebugEnhancedLogging {

  override def getCurrent(id: DepositId): QueryErrorOr[Option[DoiActionEvent]] = {
    trace(id)
    getCurrentObject(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiActionEvent])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(id: DepositId): QueryErrorOr[Seq[DoiActionEvent]] = {
    trace(id)
    getAllObjects(id)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[DoiActionEvent])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, action: DoiActionEvent): MutationErrorOr[DoiActionEvent] = {
    trace(id, action)
    storeNonNode(id, action)
  }
}
