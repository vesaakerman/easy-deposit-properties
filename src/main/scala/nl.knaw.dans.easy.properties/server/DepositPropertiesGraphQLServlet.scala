/**
 * Copyright (C) 2019 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.properties.server

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.graphql.GraphQLSchema._
import nl.knaw.dans.easy.properties.app.graphql.middleware.Authentication.Auth
import nl.knaw.dans.easy.properties.app.graphql.middleware.{ Middlewares, ProfilingConfiguration }
import nl.knaw.dans.easy.properties.app.repository.Repository
import org.scalatra.ActionResult

import scala.concurrent.{ ExecutionContext, Future }

object DepositPropertiesGraphQLServlet {

  def apply[Conn](connGen: (Conn => Future[ActionResult]) => Future[ActionResult],
                  repository: Conn => Repository,
                  authenticationConfig: Auth,
                  profilingConfig: Option[ProfilingConfiguration] = Option.empty,
                 )(implicit executionContext: ExecutionContext): GraphQLServlet[DataContext, Conn] = {
    new GraphQLServlet[DataContext, Conn](
      schema = DepositSchema,
      connGen = connGen,
      ctxProvider = conn => auth => DataContext(repository(conn), auth, authenticationConfig),
      deferredResolver = deferredResolver,
      middlewares = new Middlewares(profilingConfig).values,
    )
  }
}
