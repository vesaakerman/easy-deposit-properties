package nl.knaw.dans.easy.properties.app.graphql.resolvers

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.DoiAction.DoiAction
import nl.knaw.dans.easy.properties.app.model.{ DepositId, DoiActionEvent, DoiRegisteredEvent }
import sangria.schema.DeferredValue

object DoiEventResolver {

  lazy val currentDoisRegisteredFetcher: CurrentFetcher[DoiRegisteredEvent] = fetchCurrent(_.repo.doiRegistered.getCurrent, _.repo.doiRegistered.getCurrent)
  lazy val allDoisRegisteredFetcher: AllFetcher[DoiRegisteredEvent] = fetchAll(_.repo.doiRegistered.getAll, _.repo.doiRegistered.getAll)
  lazy val currentDoisActionFetcher: CurrentFetcher[DoiActionEvent] = fetchCurrent(_.repo.doiAction.getCurrent, _.repo.doiAction.getCurrent)
  lazy val allDoisActionFetcher: AllFetcher[DoiActionEvent] = fetchAll(_.repo.doiAction.getAll, _.repo.doiAction.getAll)

  def isDoiRegistered(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Boolean]] = {
    DeferredValue(currentDoisRegisteredFetcher.defer(depositId))
      .map { case (_, optDoiRegisteredEvent) => optDoiRegisteredEvent.map(_.value) }
  }

  def allDoiRegisteredById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[DoiRegisteredEvent]] = {
    DeferredValue(allDoisRegisteredFetcher.defer(depositId))
      .map { case (_, doiRegisteredEvents) => doiRegisteredEvents }
  }

  def currentDoiActionById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[DoiAction]] = {
    DeferredValue(currentDoisActionFetcher.defer(depositId))
      .map { case (_, optDoiActionEvent) => optDoiActionEvent.map(_.value) }
  }

  def allDoiActionsById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[DoiActionEvent]] = {
    DeferredValue(allDoisActionFetcher.defer(depositId))
      .map { case (_, doiActionEvents) => doiActionEvents }
  }
}
