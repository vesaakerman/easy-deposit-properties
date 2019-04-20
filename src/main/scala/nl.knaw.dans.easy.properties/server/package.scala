package nl.knaw.dans.easy.properties

package object server {

  case class GraphQLInput(query: String, variables: Option[String], operationName: Option[String])
}
