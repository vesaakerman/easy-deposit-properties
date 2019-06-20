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
package nl.knaw.dans.easy.properties.app.graphql

import nl.knaw.dans.easy.properties.app.graphql.types._
import sangria.execution.deferred.DeferredResolver
import sangria.schema._

object GraphQLSchema extends Scalars
  with NodeType
  with MetaTypes
  with TimebasedSearch
  with ContentTypeGraphQLType
  with SpringfieldType
  with CurationEventType
  with DoiEventTypes
  with IdentifierGraphQLType
  with IngestStepType
  with StateType
  with CuratorType
  with DepositorType
  with DepositType
  with QueryType
  with MutationType {

  val DepositSchema: Schema[DataContext, Unit] = Schema[DataContext, Unit](QueryType, mutation = Option(MutationType))
  val deferredResolver: DeferredResolver[DataContext] = DeferredResolver.fetchers(
    depositsFetcher,
    fetchLastModified,
    fetchCurrentStates, fetchAllStates,
    fetchCurrentIngestSteps, fetchAllIngestSteps,
    fetchIdentifiersByDepositId, fetchIdentifiersByType,
    fetchCurrentDoisRegistered, fetchAllDoisRegistered,
    fetchCurrentDoisAction, fetchAllDoisAction,
    fetchCurrentCurators, fetchAllCurators,
    fetchCurrentIsNewVersion, fetchAllIsNewVersion,
    fetchCurrentCurationRequired, fetchAllCurationRequired,
    fetchCurrentCurationPerformed, fetchAllCurationPerformed,
    fetchCurrentSpringfields, fetchAllSpringfields,
    fetchCurrentContentTypes, fetchAllContentTypes,
  )
}
