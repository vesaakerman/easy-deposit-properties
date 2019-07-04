package nl.knaw.dans.easy.properties.app.graphql.resolvers

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.DepositId
import nl.knaw.dans.easy.properties.app.model.springfield.Springfield
import sangria.schema.DeferredValue

object SpringfieldResolver {

  val currentSpringfieldsFetcher: CurrentFetcher[Springfield] = fetchCurrent(_.repo.springfield.getCurrent, _.repo.springfield.getCurrent)
  val allSpringfieldsFetcher: AllFetcher[Springfield] = fetchAll(_.repo.springfield.getAll, _.repo.springfield.getAll)

  def currentById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Springfield]] = {
    DeferredValue(currentSpringfieldsFetcher.defer(depositId))
      .map { case (_, optSpringfield) => optSpringfield }
  }

  def allById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[Springfield]] = {
    DeferredValue(allSpringfieldsFetcher.defer(depositId))
      .map { case (_, springfields) => springfields }
  }
}
