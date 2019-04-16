package nl.knaw.dans.easy.properties.server.graphql

import java.util.UUID

import nl.knaw.dans.easy.properties.app.model.State.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, State }

import scala.collection.mutable.ListBuffer

trait DemoRepository {

  private val deposits = ListBuffer(
    Deposit(UUID.fromString("00000000-0000-0000-0000-000000000001"), State(StateLabel.SUBMITTED, "await processing")),
    Deposit(UUID.fromString("00000000-0000-0000-0000-000000000002"), State(StateLabel.FAILED, "I did something wrong")),
  )

  def getDeposit(id: UUID): Option[Deposit] = deposits.find(_.id == id)

  def getAllDeposits: Seq[Deposit] = deposits

  def setState(id: UUID, state: State): Option[Deposit] = {
    for {
      deposit <- getDeposit(id)
      index = deposits.indexOf(deposit)
      newDeposit = deposit.copy(state = state)
      _ = deposits.update(index, newDeposit)
    } yield newDeposit
  }
}
