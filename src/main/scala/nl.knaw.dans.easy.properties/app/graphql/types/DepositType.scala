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
package nl.knaw.dans.easy.properties.app.graphql.types

import cats.syntax.either._
import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.graphql.relay.ExtendedConnection
import nl.knaw.dans.easy.properties.app.model.DoiAction.DoiAction
import nl.knaw.dans.easy.properties.app.model.contentType.ContentType
import nl.knaw.dans.easy.properties.app.model.curator.Curator
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType }
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStep
import nl.knaw.dans.easy.properties.app.model.springfield.Springfield
import nl.knaw.dans.easy.properties.app.model.state.State
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, Deposit, DepositorId, DoiActionEvent, DoiRegisteredEvent, IsNewVersionEvent, Timestamp, timestampOrdering }
import nl.knaw.dans.easy.properties.app.repository.{ CollectionExtensions, DepositFilters, QueryError }
import sangria.execution.deferred.{ Fetcher, HasId }
import sangria.macros.derive._
import sangria.marshalling.FromInput.coercedScalaInput
import sangria.relay._
import sangria.schema.{ Argument, BooleanType, Context, DeferredValue, Field, ListType, ObjectType, OptionInputType, OptionType, StringType }

import scala.concurrent.ExecutionContext.Implicits.global

trait DepositType {
  this: DepositorType
    with StateType
    with IngestStepType
    with IdentifierGraphQLType
    with DoiEventTypes
    with CuratorType
    with CurationEventType
    with CurationType
    with SpringfieldType
    with ContentTypeGraphQLType
    with TimebasedSearch
    with NodeType
    with MetaTypes
    with Scalars =>

  implicit val depositsHasId: HasId[(DepositFilters, Seq[Deposit]), DepositFilters] = HasId { case (filters, _) => filters }

  val depositsFetcher = Fetcher((ctx: DataContext, filters: Seq[DepositFilters]) => {
    filters match {
      case Seq() => Seq.empty.asRight[QueryError].toFuture
      case Seq(filter) => ctx.deposits.getDeposits(filter).map(deposits => Seq(filter -> deposits)).toFuture
      case _ => ctx.deposits.getDepositsAggregated(filters).toFuture
    }
  })
  val fetchLastModified: CurrentFetcher[Timestamp] = fetchCurrent(_.deposits.getLastModified, _.deposits.getLastModifieds)

  private val identifierTypeArgument: Argument[IdentifierType.Value] = Argument(
    name = "type",
    argumentType = IdentifierTypeType,
    description = Some("Find the identifier with this specific type."),
    defaultValue = None,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  lazy val depositBagNameFilterArgument: Argument[Option[String]] = Argument(
    name = "bagName",
    argumentType = OptionInputType(StringType),
    description = Some("Find only those deposits that have this specified bag name"),
    defaultValue = None,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )

  private val bagNameField: Field[DataContext, Deposit] = Field(
    name = "bagName",
    fieldType = OptionType(StringType),
    description = Option("The name of the deposited bag."),
    resolve = ctx => ctx.value.bagName,
  )
  private val lastModifiedField: Field[DataContext, Deposit] = Field(
    name = "lastModified",
    fieldType = OptionType(DateTimeType),
    description = Option("Get the timestamp at which this deposit was last modified. If the dataset was only created, the creation timestamp is returned."),
    resolve = getLastModified,
  )
  private val stateField: Field[DataContext, Deposit] = Field(
    name = "state",
    fieldType = OptionType(StateType),
    description = Option("The current state of the deposit."),
    resolve = getCurrentState,
  )
  private val statesField: Field[DataContext, Deposit] = Field(
    name = "states",
    description = Option("List all states of the deposit."),
    arguments = optStateOrderArgument :: timebasedSearchArguments ::: Connection.Args.All,
    fieldType = OptionType(stateConnectionType),
    resolve = ctx => getAllStates(ctx).map(ExtendedConnection.connectionFromSeq(_, ConnectionArgs(ctx))),
  )
  private val ingestStepField: Field[DataContext, Deposit] = Field(
    name = "ingestStep",
    fieldType = OptionType(IngestStepType),
    description = Option("The current ingest step of the deposit."),
    resolve = getCurrentIngestStep,
  )
  private val ingestStepsField: Field[DataContext, Deposit] = Field(
    name = "ingestSteps",
    description = Option("List all ingest steps of the deposit."),
    arguments = optIngestStepOrderArgument :: timebasedSearchArguments ::: Connection.Args.All,
    fieldType = OptionType(ingestStepConnectionType),
    resolve = ctx => getAllIngestSteps(ctx).map(ExtendedConnection.connectionFromSeq(_, ConnectionArgs(ctx))),
  )
  private val depositorField: Field[DataContext, Deposit] = Field(
    name = "depositor",
    fieldType = DepositorType,
    description = Option("Information about the depositor that submitted this deposit."),
    resolve = getDepositor,
  )
  private val identifierField: Field[DataContext, Deposit] = Field(
    name = "identifier",
    description = Some("Return the identifier of the given type related to this deposit"),
    arguments = List(identifierTypeArgument),
    fieldType = OptionType(IdentifierObjectType),
    resolve = getIdentifier,
  )
  private val identifiersField: Field[DataContext, Deposit] = Field(
    name = "identifiers",
    description = Some("List the identifiers related to this deposit"),
    fieldType = ListType(IdentifierObjectType),
    resolve = getIdentifiers,
  )
  private val doiRegisteredField: Field[DataContext, Deposit] = Field(
    name = "doiRegistered",
    description = Some("Returns whether the DOI is registered in DataCite."),
    fieldType = OptionType(BooleanType),
    resolve = getDoiRegistered,
  )
  private val doiRegisteredEventsField: Field[DataContext, Deposit] = Field(
    name = "doiRegisteredEvents",
    description = Some("Lists all state changes related to the registration of the DOI in DataCite"),
    fieldType = ListType(DoiRegisteredEventType),
    resolve = getDoiRegisteredEvents,
  )
  private val doiActionField: Field[DataContext, Deposit] = Field(
    name = "doiAction",
    description = Some("Returns whether the DOI should be 'created' or 'updated' on registration in DataCite"),
    fieldType = OptionType(DoiActionType),
    resolve = getDoiAction,
  )
  private val doiActionEventsField: Field[DataContext, Deposit] = Field(
    name = "doiActionEvents",
    description = Some("Lists all state changes related to whether the DOI should be 'created' or 'updated' on registration in DataCite"),
    fieldType = ListType(DoiActionEventType),
    resolve = getDoiActionEvents,
  )
  private val curatorField: Field[DataContext, Deposit] = Field(
    name = "curator",
    description = Some("The data manager currently assigned to this deposit"),
    fieldType = OptionType(CuratorType),
    resolve = getCurrentCurator,
  )
  private val curatorsField: Field[DataContext, Deposit] = Field(
    name = "curators",
    description = Some("List all data manager that were ever assigned to this deposit."),
    arguments = optCuratorOrderArgument :: timebasedSearchArguments ::: Connection.Args.All,
    fieldType = OptionType(curatorConnectionType),
    resolve = ctx => getAllCurators(ctx).map(ExtendedConnection.connectionFromSeq(_, ConnectionArgs(ctx))),
  )
  private val isNewVersionField: Field[DataContext, Deposit] = Field(
    name = "isNewVersion",
    description = Some("Whether this deposit is a new version."),
    fieldType = OptionType(BooleanType),
    resolve = getIsNewVersion,
  )
  private val isNewVersionEventsField: Field[DataContext, Deposit] = Field(
    name = "isNewVersionEvents",
    description = Some("List the present and past values for 'is-new-version'."),
    fieldType = ListType(IsNewVersionEventType),
    resolve = getIsNewVersionEvents,
  )
  private val curationRequiredField: Field[DataContext, Deposit] = Field(
    name = "curationRequired",
    description = Some("Whether this deposit requires curation."),
    fieldType = OptionType(BooleanType),
    resolve = getCurationRequired,
  )
  private val curationRequiredEventsField: Field[DataContext, Deposit] = Field(
    name = "curationRequiredEvents",
    description = Some("List the present and past values for 'curation-required'."),
    fieldType = ListType(CurationRequiredEventType),
    resolve = getCurationRequiredEvents,
  )
  private val curationPerformedField: Field[DataContext, Deposit] = Field(
    name = "curationPerformed",
    description = Some("Whether curation on this deposit has been performed."),
    fieldType = OptionType(BooleanType),
    resolve = getCurationPerformed,
  )
  private val curationPerformedEventsField: Field[DataContext, Deposit] = Field(
    name = "curationPerformedEvents",
    description = Some("List the present and past values for 'curation-performed'."),
    fieldType = ListType(CurationPerformedEventType),
    resolve = getCurationPerformedEvents,
  )
  private val springfieldField: Field[DataContext, Deposit] = Field(
    name = "springfield",
    description = Some("The springfield configuration currently associated with this deposit."),
    fieldType = OptionType(SpringfieldType),
    resolve = getSpringfield,
  )
  private val springfieldsField: Field[DataContext, Deposit] = Field(
    name = "springfields",
    description = Some("List the present and past values for springfield configuration."),
    arguments = optSpringfieldOrderArgument :: timebasedSearchArguments,
    fieldType = ListType(SpringfieldType),
    resolve = getSpringfields,
  )
  private val contentTypeField: Field[DataContext, Deposit] = Field(
    name = "contentType",
    description = Some("The content type currently associated with this deposit."),
    fieldType = OptionType(ContentTypeType),
    resolve = getContentType,
  )
  private val contentTypesField: Field[DataContext, Deposit] = Field(
    name = "contentTypes",
    description = Some("List the present and past values of content types."),
    arguments = optContentTypeOrderArgument :: timebasedSearchArguments,
    fieldType = ListType(ContentTypeType),
    resolve = getContentTypes,
  )

  private def getLastModified(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[Timestamp]] = {
    DeferredValue(fetchLastModified.defer(context.value.id))
      .map { case (_, modified) => modified }
  }

  private def getCurrentState(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[State]] = {
    DeferredValue(fetchCurrentStates.defer(context.value.id))
      .map { case (_, optState) => optState }
  }

  private def getAllStates(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[State]] = {
    DeferredValue(fetchAllStates.defer(context.value.id))
      .map { case (_, states) => timebasedFilterAndSort(context, optStateOrderArgument, states) }
  }

  private def getCurrentIngestStep(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[IngestStep]] = {
    DeferredValue(fetchCurrentIngestSteps.defer(context.value.id))
      .map { case (_, optIngestStep) => optIngestStep }
  }

  private def getAllIngestSteps(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[IngestStep]] = {
    DeferredValue(fetchAllIngestSteps.defer(context.value.id))
      .map { case (_, ingestSteps) => timebasedFilterAndSort(context, optIngestStepOrderArgument, ingestSteps) }
  }

  private def getDepositor(context: Context[DataContext, Deposit]): DepositorId = {
    context.value.depositorId
  }

  private def getIdentifier(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[Identifier]] = {
    val depositId = context.value.id
    val idType = context.arg(identifierTypeArgument)

    DeferredValue(fetchIdentifiersByType.defer(depositId -> idType))
      .map { case (_, identifier) => identifier }
  }

  private def getIdentifiers(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[Identifier]] = {
    DeferredValue(fetchIdentifiersByDepositId.defer(context.value.id))
      .map { case (_, identifiers) => identifiers }
  }

  private def getDoiRegistered(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[Boolean]] = {
    DeferredValue(fetchCurrentDoisRegistered.defer(context.value.id))
      .map { case (_, doiRegistered) => doiRegistered.map(_.value) }
  }

  private def getDoiRegisteredEvents(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[DoiRegisteredEvent]] = {
    DeferredValue(fetchAllDoisRegistered.defer(context.value.id))
      .map { case (_, events) => events.sortBy(_.timestamp) }
  }

  private def getDoiAction(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[DoiAction]] = {
    DeferredValue(fetchCurrentDoisAction.defer(context.value.id))
      .map { case (_, doiAction) => doiAction.map(_.value) }
  }

  private def getDoiActionEvents(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[DoiActionEvent]] = {
    DeferredValue(fetchAllDoisAction.defer(context.value.id))
      .map { case (_, events) => events.sortBy(_.timestamp) }
  }

  private def getCurrentCurator(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[Curator]] = {
    DeferredValue(fetchAllCurations.defer(context.value.id))
      .map { case (_, curations) =>
        curations.map(_.getCurator)
          .distinctUntilChanged(curator => (curator.userId, curator.email))
          .lastOption
      }
  }

  private def getAllCurators(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[Curator]] = {
    DeferredValue(fetchAllCurations.defer(context.value.id))
      .map { case (_, curations) => timebasedFilterAndSort(context, optCuratorOrderArgument, curations.map(_.getCurator).distinctUntilChanged(curator => (curator.userId, curator.email))) }
  }

  private def getIsNewVersion(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[Boolean]] = {
    DeferredValue(fetchCurrentCuration.defer(context.value.id))
      .map { case (_, curation) => curation.map(_.getIsNewVersionEvent.isNewVersion) }
  }

  private def getIsNewVersionEvents(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[IsNewVersionEvent]] = {
    DeferredValue(fetchAllCurations.defer(context.value.id))
      .map { case (_, curations) => curations.map(_.getIsNewVersionEvent).distinctUntilChanged(_.isNewVersion).sortBy(_.timestamp) }
  }

  private def getCurationRequired(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[Boolean]] = {
    DeferredValue(fetchCurrentCuration.defer(context.value.id))
      .map { case (_, curation) => curation.map(_.getCurationRequiredEvent.curationRequired) }
  }

  private def getCurationRequiredEvents(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[CurationRequiredEvent]] = {
    DeferredValue(fetchAllCurations.defer(context.value.id))
      .map { case (_, curations) => curations.map(_.getCurationRequiredEvent).distinctUntilChanged(_.curationRequired).sortBy(_.timestamp) }
  }

  private def getCurationPerformed(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[Boolean]] = {
    DeferredValue(fetchCurrentCuration.defer(context.value.id))
      .map { case (_, curation) => curation.map(_.getCurationPerformedEvent.curationPerformed) }
  }

  private def getCurationPerformedEvents(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[CurationPerformedEvent]] = {
    DeferredValue(fetchAllCurations.defer(context.value.id))
      .map { case (_, curations) => curations.map(_.getCurationPerformedEvent).distinctUntilChanged(_.curationPerformed).sortBy(_.timestamp) }
  }

  private def getSpringfield(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[Springfield]] = {
    DeferredValue(fetchCurrentSpringfields.defer(context.value.id))
      .map { case (_, springfield) => springfield }
  }

  private def getSpringfields(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[Springfield]] = {
    DeferredValue(fetchAllSpringfields.defer(context.value.id))
      .map { case (_, springfield) => timebasedFilterAndSort(context, optSpringfieldOrderArgument, springfield) }
  }

  private def getContentType(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Option[ContentType]] = {
    DeferredValue(fetchCurrentContentTypes.defer(context.value.id))
      .map { case (_, contentType) => contentType }
  }

  private def getContentTypes(context: Context[DataContext, Deposit]): DeferredValue[DataContext, Seq[ContentType]] = {
    DeferredValue(fetchAllContentTypes.defer(context.value.id))
      .map { case (_, contentTypes) => timebasedFilterAndSort(context, optContentTypeOrderArgument, contentTypes) }
  }

  implicit val depositIdentifiable: Identifiable[Deposit] = _.id.toString

  // lazy because we need it before being declared (in StateType)
  implicit lazy val DepositType: ObjectType[DataContext, Deposit] = deriveObjectType(
    ObjectTypeDescription("Contains all technical metadata about this deposit."),
    Interfaces[DataContext, Deposit](nodeInterface),
    RenameField("id", "depositId"),
    DocumentField("id", "The identifier of the deposit."),
    DocumentField("bagName", "The name of the deposited bag."),
    DocumentField("creationTimestamp", "The moment this deposit was created."),
    AddFields(
      Node.globalIdField[DataContext, Deposit],
      stateField,
      statesField,
      lastModifiedField,
      ingestStepField,
      ingestStepsField,
      identifierField,
      identifiersField,
      doiRegisteredField,
      doiRegisteredEventsField,
      doiActionField,
      doiActionEventsField,
      curatorField,
      curatorsField,
      isNewVersionField,
      isNewVersionEventsField,
      curationRequiredField,
      curationRequiredEventsField,
      curationPerformedField,
      curationPerformedEventsField,
      springfieldField,
      springfieldsField,
      contentTypeField,
      contentTypesField,
    ),
    ReplaceField("bagName", bagNameField),
    ReplaceField("depositorId", depositorField),
  )

  lazy val ConnectionDefinition(_, depositConnectionType) = ExtendedConnection.definition[DataContext, ExtendedConnection, Deposit](
    name = "Deposit",
    nodeType = DepositType,
  )
}
