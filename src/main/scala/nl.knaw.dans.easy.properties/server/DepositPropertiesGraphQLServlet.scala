package nl.knaw.dans.easy.properties.server

import nl.knaw.dans.easy.properties.app.graphql.GraphQLSchema.DepositSchema
import nl.knaw.dans.easy.properties.app.graphql.{ DataContext, DepositRepository }

object DepositPropertiesGraphQLServlet {

  def apply(repository: DepositRepository): GraphQLServlet[DataContext] = {
    new GraphQLServlet(DepositSchema, DataContext(repository))
  }
}
