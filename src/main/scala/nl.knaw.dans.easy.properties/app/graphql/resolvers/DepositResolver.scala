package nl.knaw.dans.easy.properties.app.graphql.resolvers

import cats.syntax.either._
import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, Timestamp }
import nl.knaw.dans.easy.properties.app.repository.{ DepositFilters, QueryError }
import sangria.execution.deferred.Fetcher
import sangria.schema.DeferredValue

object DepositResolver {

  lazy val depositsFetcher = Fetcher((ctx: DataContext, filters: Seq[DepositFilters]) => filters match {
    case Seq() => Seq.empty.asRight[QueryError].toFuture
    case Seq(filter) => ctx.repo.deposits.search(filter).map(deposits => Seq(filter -> deposits)).toFuture
    case _ => ctx.repo.deposits.search(filters).toFuture
  })
  lazy val lastModifiedFetcher: CurrentFetcher[Timestamp] = fetchCurrent(_.repo.deposits.lastModified, _.repo.deposits.lastModified)

  def findDeposit(depositFilters: DepositFilters)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[Deposit]] = {
    DeferredValue(depositsFetcher.defer(depositFilters))
      .map { case (_, deposits) => deposits }
  }
  
  def lastModified(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Timestamp]] = {
    DeferredValue(lastModifiedFetcher.defer(depositId))
      .map { case (_, lastModified) => lastModified }
  }
}
