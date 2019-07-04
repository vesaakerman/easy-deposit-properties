package nl.knaw.dans.easy.properties.app.graphql.resolvers

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.DepositId
import nl.knaw.dans.easy.properties.app.model.contentType.ContentType
import sangria.schema.DeferredValue

object ContentTypeResolver {

  lazy val currentContentTypesFetcher: CurrentFetcher[ContentType] = fetchCurrent(_.repo.contentType.getCurrent, _.repo.contentType.getCurrent)
  lazy val allContentTypesFetcher: AllFetcher[ContentType] = fetchAll(_.repo.contentType.getAll, _.repo.contentType.getAll)

  def currentById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[ContentType]] = {
    DeferredValue(currentContentTypesFetcher.defer(depositId))
      .map { case (_, optContentType) => optContentType }
  }

  def allById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[ContentType]] = {
    DeferredValue(allContentTypesFetcher.defer(depositId))
      .map { case (_, contentTypes) => contentTypes }
  }
}
