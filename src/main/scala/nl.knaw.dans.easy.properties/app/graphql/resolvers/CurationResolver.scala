package nl.knaw.dans.easy.properties.app.graphql.resolvers

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.curation.Curation
import nl.knaw.dans.easy.properties.app.model.curator.Curator
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, DepositId, IsNewVersionEvent }
import sangria.schema.DeferredValue

object CurationResolver {

  lazy val currentCurationsFetcher: CurrentFetcher[Curation] = fetchCurrent(_.repo.curation.getCurrent, _.repo.curation.getCurrent)
  lazy val allCurationsFetcher: AllFetcher[Curation] = fetchAll(_.repo.curation.getAll, _.repo.curation.getAll)

  def currentCuratorsById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Curator]] = {
    DeferredValue(allCurationsFetcher.defer(depositId))
      .map { case (_, curations) => curations
        .map(_.getCurator)
        .distinctUntilChanged(curator => (curator.userId, curator.email))
        .lastOption
      }
  }

  def allCuratorsById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[Curator]] = {
    DeferredValue(allCurationsFetcher.defer(depositId))
      .map { case (_, curations) => curations
        .map(_.getCurator)
        .distinctUntilChanged(curator => (curator.userId, curator.email))
      }
  }
  
  def isNewVersion(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Boolean]] = {
    DeferredValue(currentCurationsFetcher.defer(depositId))
      .map { case (_, optCuration) => optCuration.map(_.getIsNewVersionEvent.isNewVersion) }
  }

  def allIsNewVersionEvents(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[IsNewVersionEvent]] = {
    DeferredValue(allCurationsFetcher.defer(depositId))
      .map { case (_, curations) => curations.map(_.getIsNewVersionEvent).distinctUntilChanged(_.isNewVersion) }
  }

  def isCurationRequired(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Boolean]] = {
    DeferredValue(currentCurationsFetcher.defer(depositId))
      .map { case (_, optCuration) => optCuration.map(_.getCurationRequiredEvent.curationRequired) }
  }

  def allIsCurationRequiredEvents(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[CurationRequiredEvent]] = {
    DeferredValue(allCurationsFetcher.defer(depositId))
      .map { case (_, curations) => curations.map(_.getCurationRequiredEvent).distinctUntilChanged(_.curationRequired) }
  }

  def isCurationPerformed(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Boolean]] = {
    DeferredValue(currentCurationsFetcher.defer(depositId))
      .map { case (_, optCuration) => optCuration.map(_.getCurationPerformedEvent.curationPerformed) }
  }

  def allIsCurationPerformedEvents(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[CurationPerformedEvent]] = {
    DeferredValue(allCurationsFetcher.defer(depositId))
      .map { case (_, curations) => curations.map(_.getCurationPerformedEvent).distinctUntilChanged(_.curationPerformed) }
  }
}
