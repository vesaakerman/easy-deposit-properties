package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStep, InputIngestStep }

trait IngestStepDao {

  def getById(id: String): QueryErrorOr[Option[IngestStep]]

  def getCurrent(id: DepositId): QueryErrorOr[Option[IngestStep]]

  def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[IngestStep])]]

  def getAll(id: DepositId): QueryErrorOr[Seq[IngestStep]]

  def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[IngestStep])]]

  def store(id: DepositId, step: InputIngestStep): MutationErrorOr[IngestStep]

  def getDepositById(id: String): QueryErrorOr[Option[Deposit]]
}
