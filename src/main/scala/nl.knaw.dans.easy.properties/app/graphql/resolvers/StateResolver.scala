package nl.knaw.dans.easy.properties.app.graphql.resolvers

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.DepositId
import nl.knaw.dans.easy.properties.app.model.state.State
import sangria.schema.DeferredValue

object StateResolver {

  lazy val currentStatesFetcher: CurrentFetcher[State] = fetchCurrent(_.repo.states.getCurrent, _.repo.states.getCurrent)
  lazy val allStatesFetcher: AllFetcher[State] = fetchAll(_.repo.states.getAll, _.repo.states.getAll)

  def currentById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[State]] = {
    DeferredValue(currentStatesFetcher.defer(depositId))
      .map { case (_, optState) => optState }
  }

  def allById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[State]] = {
    DeferredValue(allStatesFetcher.defer(depositId))
      .map { case (_, states) => states }
  }
}
