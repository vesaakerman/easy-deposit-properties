package nl.knaw.dans.easy.properties.app.repository.demo

import nl.knaw.dans.easy.properties.app.model.{ DepositId, DoiRegisteredEvent }
import nl.knaw.dans.easy.properties.app.repository.{ DoiRegisteredDao, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoDoiRegisteredDao(implicit repo: DoiRegisteredRepo, depositRepo: DepositRepo) extends DoiRegisteredDao with DemoDao with DebugEnhancedLogging {

  override def getCurrent(id: DepositId): QueryErrorOr[Option[DoiRegisteredEvent]] = {
    trace(id)
    getCurrentObject(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiRegisteredEvent])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(id: DepositId): QueryErrorOr[Seq[DoiRegisteredEvent]] = {
    trace(id)
    getAllObjects(id)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[DoiRegisteredEvent])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, registered: DoiRegisteredEvent): MutationErrorOr[DoiRegisteredEvent] = {
    trace(id, registered)
    storeNonNode(id, registered)
  }
}
