package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, Timestamp }

trait DepositDao {

  def getAll: QueryErrorOr[Seq[Deposit]]

  def find(id: DepositId): QueryErrorOr[Deposit]

  def search(filters: DepositFilters): QueryErrorOr[Seq[Deposit]]

  def search(filters: Seq[DepositFilters]): QueryErrorOr[Seq[(DepositFilters, Seq[Deposit])]]

  def store(deposit: Deposit): MutationErrorOr[Deposit]

  def lastModified(id: DepositId): QueryErrorOr[Option[Timestamp]]

  def lastModified(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Timestamp])]]
}
