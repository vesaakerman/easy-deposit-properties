package nl.knaw.dans.easy.properties.app.graphql

import java.util.UUID

import nl.knaw.dans.easy.properties.app.model.{ Deposit, State }

trait DepositRepository {

  def getAllDeposits: Seq[Deposit]

  def getDeposit(id: UUID): Option[Deposit]

  def registerDeposit(deposit: Deposit): Option[Deposit]

  def getState(id: UUID): Option[State]

  def setState(id: UUID, state: State): Option[Deposit]
}
