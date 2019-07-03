package nl.knaw.dans.easy.properties.app.repository.demo

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, InputContentType }
import nl.knaw.dans.easy.properties.app.repository.{ ContentTypeDao, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoContentTypeDao(implicit repo: ContentTypeRepo, depositRepo: DepositRepo) extends ContentTypeDao with DemoDao with DebugEnhancedLogging {

  override def getById(id: String): QueryErrorOr[Option[ContentType]] = {
    trace(id)
    getObjectById(id)
  }

  override def getCurrent(id: DepositId): QueryErrorOr[Option[ContentType]] = {
    trace(id)
    getCurrentObject(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[ContentType])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(id: DepositId): QueryErrorOr[Seq[ContentType]] = {
    trace(id)
    getAllObjects(id)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[ContentType])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, contentType: InputContentType): MutationErrorOr[ContentType] = {
    trace(id, contentType)
    storeNode(id, contentType) {
      case (contentTypeId, InputContentType(value, timestamp)) => ContentType(contentTypeId, value, timestamp)
    }
  }

  override def getDepositById(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    getDepositByObjectId(id)
  }
}
