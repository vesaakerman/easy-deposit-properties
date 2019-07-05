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
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import sangria.execution.deferred.Fetcher
import sangria.schema.DeferredValue

object IdentifierResolver {

  type IdentifiersByType = Fetcher[DataContext, ((DepositId, IdentifierType), Option[Identifier]), ((DepositId, IdentifierType), Option[Identifier]), (DepositId, IdentifierType.Value)]

  lazy val byIdFetcher: ByIdFetcher[Identifier] = fetchById(_.repo.identifiers.getById)
  lazy val identifiersByTypeFetcher: IdentifiersByType = Fetcher(_.repo.identifiers.getByType(_).toFuture)
  lazy val identifierTypesAndValuesFetcher = Fetcher((ctx: DataContext, ids: Seq[(IdentifierType, String)]) => {
    ctx.repo.identifiers.getByTypesAndValues(ids).toFuture
  })
  lazy val identifiersByDepositIdFetcher: AllFetcher[Identifier] = fetchAll(_.repo.identifiers.getAll)
  lazy val depositByIdentifierIdFetcher: DepositByIdFetcher = fetchDepositsById(_.repo.identifiers.getDepositsById)

  def identifierById(id: String)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Identifier]] = {
    DeferredValue(byIdFetcher.defer(id))
      .map { case (_, optIdentifier) => optIdentifier }
  }

  def identifierByType(depositId: DepositId, idType: IdentifierType.Value)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Identifier]] = {
    DeferredValue(identifiersByTypeFetcher.defer(depositId -> idType))
      .map { case (_, optIdentifier) => optIdentifier }
  }
  
  def identifierByTypeAndValue(idType: IdentifierType, idValue: String)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Identifier]] = {
    DeferredValue(identifierTypesAndValuesFetcher.defer(idType -> idValue))
      .map { case (_, optIdentifier) => optIdentifier }
  }

  def allById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[Identifier]] = {
    DeferredValue(identifiersByDepositIdFetcher.defer(depositId))
      .map { case (_, identifiers) => identifiers }
  }

  def depositByIdentifierId(id: String)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Deposit]] = {
    DeferredValue(depositByIdentifierIdFetcher.defer(id))
      .map { case (_, optDeposit) => optDeposit }
  }
}
