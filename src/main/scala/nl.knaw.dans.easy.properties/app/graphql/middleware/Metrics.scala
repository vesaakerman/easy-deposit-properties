package nl.knaw.dans.easy.properties.app.graphql.middleware

import org.slf4j.Logger
import sangria.execution.{ Middleware, MiddlewareQueryContext }

class Metrics(logger: Logger) extends Middleware[Any] {

  override type QueryVal = Long

  def beforeQuery(context: MiddlewareQueryContext[Any, _, _]): QueryVal = {
    System.currentTimeMillis()
  }

  def afterQuery(startTimeMs: QueryVal, context: MiddlewareQueryContext[Any, _, _]): Unit = {
    val duration = System.currentTimeMillis() - startTimeMs
    logger.info(s"Query execution time ${ duration }ms")
  }
}
