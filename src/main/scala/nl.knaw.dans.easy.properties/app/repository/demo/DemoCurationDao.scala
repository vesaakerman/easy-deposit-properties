package nl.knaw.dans.easy.properties.app.repository.demo

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.curation.{ Curation, InputCuration }
import nl.knaw.dans.easy.properties.app.repository.{ CurationDao, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoCurationDao(implicit repo: CurationRepo, depositRepo: DepositRepo) extends CurationDao with DemoDao with DebugEnhancedLogging {

  override def getById(id: String): QueryErrorOr[Option[Curation]] = {
    trace(id)
    getObjectById(id)
  }

  override def getCurrent(id: DepositId): QueryErrorOr[Option[Curation]] = {
    trace(id)
    getCurrentObject(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Curation])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(id: DepositId): QueryErrorOr[Seq[Curation]] = {
    trace(id)
    getAllObjects(id)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Curation])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, curation: InputCuration): MutationErrorOr[Curation] = {
    trace(id, curation)
    storeNode(id, curation) {
      case (curatorId, InputCuration(isNewVersion, isRequired, isPerformed, userId, email, timestamp)) => Curation(curatorId, isNewVersion, isRequired, isPerformed, userId, email, timestamp)
    }
  }

  override def getDepositById(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    getDepositByObjectId(id)
  }
}
