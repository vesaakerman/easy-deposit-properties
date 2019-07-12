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
package nl.knaw.dans.easy.properties.app.graphql.middleware

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import sangria.execution._
import sangria.schema.Context

object Authentication extends Middleware[DataContext] with MiddlewareBeforeField[DataContext] {
  type QueryVal = Unit
  type FieldVal = Unit
  private type MCtx = MiddlewareQueryContext[DataContext, _, _]

  case object RequiresAuthentication extends FieldTag
  case class AuthenticationException(message: String) extends Exception(message)
  case class Auth(username: String, password: String)

  override def beforeQuery(context: MCtx): QueryVal = ()

  override def afterQuery(queryVal: QueryVal, context: MCtx): Unit = ()

  override def beforeField(queryVal: QueryVal, mctx: MCtx, ctx: Context[DataContext, _]): BeforeFieldResult[DataContext, FieldVal] = {
    if (ctx.field.tags contains RequiresAuthentication)
      if (!ctx.ctx.isLoggedIn)
        throw AuthenticationException("you must be logged in!")
    
    continue
  }
}
