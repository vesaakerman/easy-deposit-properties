package nl.knaw.dans.easy.properties.app.graphql.resolvers

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.DepositId
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStep
import sangria.schema.DeferredValue

object IngestStepResolver {

  lazy val currentIngestStepsFetcher: CurrentFetcher[IngestStep] = fetchCurrent(_.repo.ingestSteps.getCurrent, _.repo.ingestSteps.getCurrent)
  lazy val allIngestStepsFetcher: AllFetcher[IngestStep] = fetchAll(_.repo.ingestSteps.getAll, _.repo.ingestSteps.getAll)

  def currentById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[IngestStep]] = {
    DeferredValue(currentIngestStepsFetcher.defer(depositId))
      .map { case (_, optIngestStep) => optIngestStep }
  }

  def allById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[IngestStep]] = {
    DeferredValue(allIngestStepsFetcher.defer(depositId))
      .map { case (_, ingestSteps) => ingestSteps }
  }
}
