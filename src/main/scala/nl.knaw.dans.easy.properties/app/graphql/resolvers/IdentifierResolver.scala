package nl.knaw.dans.easy.properties.app.graphql.resolvers

import cats.syntax.either._
import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.DepositId
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType }
import nl.knaw.dans.easy.properties.app.repository.QueryError
import sangria.execution.deferred.Fetcher
import sangria.schema.DeferredValue

object IdentifierResolver {

  lazy val identifiersByTypeFetcher = Fetcher((ctx: DataContext, ids: Seq[(DepositId, IdentifierType.Value)]) => {
    ids match {
      case Seq() => Seq.empty.asRight[QueryError].toFuture
      case Seq((depositId, identifierType)) => ctx.repo.identifiers.getByType(depositId, identifierType).map(optIdentifier => Seq((depositId, identifierType) -> optIdentifier)).toFuture
      case _ => ctx.repo.identifiers.getByType(ids).toFuture
    }
  })
  lazy val identifiersByDepositIdFetcher: AllFetcher[Identifier] = fetchAll(_.repo.identifiers.getAll, _.repo.identifiers.getAll)
  
  def identifierByType(depositId: DepositId, idType: IdentifierType.Value)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Identifier]] = {
    DeferredValue(identifiersByTypeFetcher.defer(depositId -> idType))
      .map { case (_, identifier) => identifier }
  }

  def allById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[Identifier]] = {
    DeferredValue(identifiersByDepositIdFetcher.defer(depositId))
      .map { case (_, identifiers) => identifiers }
  }
}
