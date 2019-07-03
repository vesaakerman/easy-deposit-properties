package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.{ DepositId, DoiRegisteredEvent }

trait DoiRegisteredDao {

  def getCurrent(id: DepositId): QueryErrorOr[Option[DoiRegisteredEvent]]

  def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiRegisteredEvent])]]

  def getAll(id: DepositId): QueryErrorOr[Seq[DoiRegisteredEvent]]

  def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[DoiRegisteredEvent])]]

  def store(id: DepositId, registered: DoiRegisteredEvent): MutationErrorOr[DoiRegisteredEvent]
}
