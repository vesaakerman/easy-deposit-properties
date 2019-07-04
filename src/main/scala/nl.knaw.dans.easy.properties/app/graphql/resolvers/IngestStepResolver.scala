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
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStep
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import sangria.schema.DeferredValue

object IngestStepResolver {

  lazy val currentIngestStepsFetcher: CurrentFetcher[IngestStep] = fetchCurrent(_.repo.ingestSteps.getCurrent)
  lazy val allIngestStepsFetcher: AllFetcher[IngestStep] = fetchAll(_.repo.ingestSteps.getAll)
  lazy val depositByIngestStepIdFetcher: DepositByIdFetcher = fetchDepositsById(_.repo.ingestSteps.getDepositsById)

  def currentById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Option[IngestStep]] = {
    DeferredValue(currentIngestStepsFetcher.defer(depositId))
      .map { case (_, optIngestStep) => optIngestStep }
  }

  def allById(depositId: DepositId)(implicit ctx: DataContext): DeferredValue[DataContext, Seq[IngestStep]] = {
    DeferredValue(allIngestStepsFetcher.defer(depositId))
      .map { case (_, ingestSteps) => ingestSteps }
  }

  def depositByIngestStepId(id: String)(implicit ctx: DataContext): DeferredValue[DataContext, Option[Deposit]] = {
    DeferredValue(depositByIngestStepIdFetcher.defer(id))
      .map { case (_, optDeposit) => optDeposit }
  }
}
