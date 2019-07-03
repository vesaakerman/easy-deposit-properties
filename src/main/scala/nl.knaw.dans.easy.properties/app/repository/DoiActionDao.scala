package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.{ DepositId, DoiActionEvent }

trait DoiActionDao {

  def getCurrent(id: DepositId): QueryErrorOr[Option[DoiActionEvent]]

  def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiActionEvent])]]

  def getAll(id: DepositId): QueryErrorOr[Seq[DoiActionEvent]]

  def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[DoiActionEvent])]]

  def store(id: DepositId, action: DoiActionEvent): MutationErrorOr[DoiActionEvent]
}
