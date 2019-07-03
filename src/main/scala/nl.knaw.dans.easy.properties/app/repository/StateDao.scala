package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State }

trait StateDao {

  def getById(id: String): QueryErrorOr[Option[State]]

  def getCurrent(id: DepositId): QueryErrorOr[Option[State]]

  def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[State])]]

  def getAll(id: DepositId): QueryErrorOr[Seq[State]]

  def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[State])]]

  def store(id: DepositId, state: InputState): MutationErrorOr[State]

  def getDepositById(id: String): QueryErrorOr[Option[Deposit]]
}
