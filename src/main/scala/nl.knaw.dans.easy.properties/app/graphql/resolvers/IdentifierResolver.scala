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
package nl.knaw.dans.easy.properties.app.graphql.resolvers

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.DepositId
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType }
import sangria.execution.deferred.Fetcher
import sangria.schema.DeferredValue

object IdentifierResolver {

  type IdentifiersByType = Fetcher[DataContext, ((DepositId, IdentifierType), Option[Identifier]), ((DepositId, IdentifierType), Option[Identifier]), (DepositId, IdentifierType.Value)]

  lazy val identifiersByTypeFetcher: IdentifiersByType = Fetcher(_.repo.identifiers.getByType(_).toFuture)
  lazy val identifiersByDepositIdFetcher: AllFetcher[Identifier] = fetchAll(_.repo.identifiers.getAll)

  def identifierByType(depositId: DepositId, idType: IdentifierType.Value)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Identifier]] = {
    DeferredValue(identifiersByTypeFetcher.defer(depositId -> idType))
      .map { case (_, identifier) => identifier }
  }

  def allById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[Identifier]] = {
    DeferredValue(identifiersByDepositIdFetcher.defer(depositId))
      .map { case (_, identifiers) => identifiers }
  }
}
