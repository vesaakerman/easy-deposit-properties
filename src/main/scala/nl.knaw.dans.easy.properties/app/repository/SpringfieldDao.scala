package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield }

trait SpringfieldDao {

  def getById(id: String): QueryErrorOr[Option[Springfield]]

  def getCurrent(id: DepositId): QueryErrorOr[Option[Springfield]]

  def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Springfield])]]

  def getAll(id: DepositId): QueryErrorOr[Seq[Springfield]]

  def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Springfield])]]

  def store(id: DepositId, springfield: InputSpringfield): MutationErrorOr[Springfield]

  def getDepositById(id: String): QueryErrorOr[Option[Deposit]]
}
