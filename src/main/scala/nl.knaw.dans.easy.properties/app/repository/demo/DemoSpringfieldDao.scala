package nl.knaw.dans.easy.properties.app.repository.demo

import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.repository.{ MutationErrorOr, QueryErrorOr, SpringfieldDao }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoSpringfieldDao(implicit repo: SpringfieldRepo, depositRepo: DepositRepo) extends SpringfieldDao with DemoDao with DebugEnhancedLogging {

  override def getById(id: String): QueryErrorOr[Option[Springfield]] = {
    trace(id)
    getObjectById(id)
  }

  override def getCurrent(id: DepositId): QueryErrorOr[Option[Springfield]] = {
    trace(id)
    getCurrentObject(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Springfield])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(id: DepositId): QueryErrorOr[Seq[Springfield]] = {
    trace(id)
    getAllObjects(id)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Springfield])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, springfield: InputSpringfield): MutationErrorOr[Springfield] = {
    trace(id, springfield)
    storeNode(id, springfield) {
      case (springfieldId, InputSpringfield(domain, user, collection, playmode, timestamp)) => Springfield(springfieldId, domain, user, collection, playmode, timestamp)
    }
  }

  override def getDepositById(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    getDepositByObjectId(id)
  }
}
