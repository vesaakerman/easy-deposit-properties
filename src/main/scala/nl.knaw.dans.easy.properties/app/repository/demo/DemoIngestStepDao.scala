package nl.knaw.dans.easy.properties.app.repository.demo

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStep, InputIngestStep }
import nl.knaw.dans.easy.properties.app.repository.{ IngestStepDao, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoIngestStepDao(implicit repo: IngestStepRepo, depositRepo: DepositRepo) extends IngestStepDao with DemoDao with DebugEnhancedLogging {

  override def getById(id: String): QueryErrorOr[Option[IngestStep]] = {
    trace(id)
    getObjectById(id)
  }

  override def getCurrent(id: DepositId): QueryErrorOr[Option[IngestStep]] = {
    trace(id)
    getCurrentObject(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[IngestStep])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(id: DepositId): QueryErrorOr[Seq[IngestStep]] = {
    trace(id)
    getAllObjects(id)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[IngestStep])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, inputStep: InputIngestStep): MutationErrorOr[IngestStep] = {
    trace(id, inputStep)
    storeNode(id, inputStep) {
      case (stepId, InputIngestStep(step, timestamp)) => IngestStep(stepId, step, timestamp)
    }
  }

  override def getDepositById(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    getDepositByObjectId(id)
  }
}
