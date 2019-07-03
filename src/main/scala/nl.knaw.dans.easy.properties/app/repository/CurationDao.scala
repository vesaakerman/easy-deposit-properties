package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.curation.{ Curation, InputCuration }

trait CurationDao {

  def getById(id: String): QueryErrorOr[Option[Curation]]

  def getCurrent(id: DepositId): QueryErrorOr[Option[Curation]]

  def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Curation])]]

  def getAll(id: DepositId): QueryErrorOr[Seq[Curation]]

  def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Curation])]]

  def store(id: DepositId, curation: InputCuration): MutationErrorOr[Curation]

  def getDepositById(id: String): QueryErrorOr[Option[Deposit]]
}
