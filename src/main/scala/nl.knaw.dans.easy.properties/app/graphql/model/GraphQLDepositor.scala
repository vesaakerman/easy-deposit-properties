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
package nl.knaw.dans.easy.properties.app.graphql.model

import nl.knaw.dans.easy.properties.app.graphql._
import nl.knaw.dans.easy.properties.app.graphql.relay.ExtendedConnection
import nl.knaw.dans.easy.properties.app.graphql.resolvers.DepositResolver
import nl.knaw.dans.easy.properties.app.model.Origin.Origin
import nl.knaw.dans.easy.properties.app.model.contentType.DepositContentTypeFilter
import nl.knaw.dans.easy.properties.app.model.curator.DepositCuratorFilter
import nl.knaw.dans.easy.properties.app.model.ingestStep.DepositIngestStepFilter
import nl.knaw.dans.easy.properties.app.model.sort.DepositOrder
import nl.knaw.dans.easy.properties.app.model.state.DepositStateFilter
import nl.knaw.dans.easy.properties.app.model.{ DepositCurationPerformedFilter, DepositCurationRequiredFilter, DepositDoiActionFilter, DepositDoiRegisteredFilter, DepositIsNewVersionFilter, DepositorId, TimeFilter }
import nl.knaw.dans.easy.properties.app.repository.DepositFilters
import org.joda.time.DateTime
import sangria.macros.derive.{ GraphQLDescription, GraphQLField, GraphQLName }
import sangria.relay.ConnectionArgs
import sangria.schema.{ Context, DeferredValue }

@GraphQLName("Depositor")
@GraphQLDescription("Information about the depositor that submitted this deposit.")
class GraphQLDepositor(id: DepositorId) {

  @GraphQLField
  @GraphQLDescription("The EASY account of the depositor.")
  val depositorId: DepositorId = id

  @GraphQLField
  @GraphQLDescription("List all deposits originating from the same depositor.")
  def deposits(@GraphQLDescription("Find only those deposits that have this specified bag name.") bagName: Option[String] = None,
               @GraphQLDescription("Find only those deposits that have this specified origin.") origin: Option[Origin] = None,
               @GraphQLDescription("List only those deposits that have this specified state label.") state: Option[DepositStateFilter] = None,
               @GraphQLDescription("List only those deposits that have this specified ingest step label.") ingestStep: Option[DepositIngestStepFilter] = None,
               @GraphQLDescription("List only those deposits that have this specified value for DOI registered.") doiRegistered: Option[DepositDoiRegisteredFilter] = None,
               @GraphQLDescription("List only those deposits that have this specified value for DOI action.") doiAction: Option[DepositDoiActionFilter] = None,
               @GraphQLDescription("List only those deposits that have this specified data manager.") curator: Option[DepositCuratorFilter] = None,
               @GraphQLDescription("List only those deposits that have this specified value for 'is-new-version'.") isNewVersion: Option[DepositIsNewVersionFilter] = None,
               @GraphQLDescription("List only those deposits that have this specified value for 'curation required'.") curationRequired: Option[DepositCurationRequiredFilter] = None,
               @GraphQLDescription("List only those deposits that have this specified value for 'curation performed'.") curationPerformed: Option[DepositCurationPerformedFilter] = None,
               @GraphQLDescription("List only those deposits that have this specified content type.") contentType: Option[DepositContentTypeFilter] = None,
               @GraphQLDescription("Ordering options for the returned deposits.") orderBy: Option[DepositOrder] = None,
               @GraphQLDescription("List only those elements that have a timestamp earlier than this given timestamp.") earlierThan: Option[DateTime] = None,
               @GraphQLDescription("List only those elements that have a timestamp later than this given timestamp.") laterThan: Option[DateTime] = None,
               @GraphQLDescription("List only those elements that have a timestamp equal to the given timestamp.") atTimestamp: Option[DateTime] = None,
               before: Option[String] = None,
               after: Option[String] = None,
               first: Option[Int] = None,
               last: Option[Int] = None,
              )(implicit ctx: Context[DataContext, GraphQLDepositor]): DeferredValue[DataContext, ExtendedConnection[GraphQLDeposit]] = {
    DepositResolver.findDeposit(DepositFilters(
      depositorId = Some(id),
      bagName = bagName,
      originFilter = origin,
      stateFilter = state,
      ingestStepFilter = ingestStep,
      doiRegisteredFilter = doiRegistered,
      doiActionFilter = doiAction,
      curatorFilter = curator,
      isNewVersionFilter = isNewVersion,
      curationRequiredFilter = curationRequired,
      curationPerformedFilter = curationPerformed,
      contentTypeFilter = contentType,
      timeFilter = TimeFilter(earlierThan, laterThan, atTimestamp),
      sort = orderBy,
    ))
      .map(deposits => ExtendedConnection.connectionFromSeq(
        deposits.map(new GraphQLDeposit(_)),
        ConnectionArgs(before, after, first, last),
      ))
  }
}
