package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, InputContentType }

trait ContentTypeDao {

  def getById(id: String): QueryErrorOr[Option[ContentType]]

  def getCurrent(id: DepositId): QueryErrorOr[Option[ContentType]]

  def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[ContentType])]]

  def getAll(id: DepositId): QueryErrorOr[Seq[ContentType]]

  def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[ContentType])]]

  def store(id: DepositId, contentType: InputContentType): MutationErrorOr[ContentType]

  def getDepositById(id: String): QueryErrorOr[Option[Deposit]]
}
