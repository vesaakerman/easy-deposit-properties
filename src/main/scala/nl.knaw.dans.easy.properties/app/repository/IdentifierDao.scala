package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, InputIdentifier }

trait IdentifierDao {

  def getById(id: String): QueryErrorOr[Option[Identifier]]

  def getByType(id: DepositId, idType: IdentifierType): QueryErrorOr[Option[Identifier]]

  def getByType(ids: Seq[(DepositId, IdentifierType)]): QueryErrorOr[Seq[((DepositId, IdentifierType), Option[Identifier])]]

  def getByTypeAndValue(idType: IdentifierType, idValue: String): QueryErrorOr[Option[Identifier]]

  def getAll(id: DepositId): QueryErrorOr[Seq[Identifier]]

  def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Identifier])]]

  def store(id: DepositId, identifier: InputIdentifier): MutationErrorOr[Identifier]

  def getDepositById(id: String): QueryErrorOr[Option[Deposit]]
}
