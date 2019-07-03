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

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentTypeValue, InputContentType }
import nl.knaw.dans.easy.properties.app.model.curation.InputCuration
import nl.knaw.dans.easy.properties.app.model.identifier.{ IdentifierType, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStepLabel, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, SpringfieldPlayMode }
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, StateLabel }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DepositorId, DoiAction, DoiActionEvent, DoiRegisteredEvent, Timestamp }
import sangria.relay.Mutation
import sangria.schema.{ Action, BooleanType, Context, Field, InputField, InputObjectType, ObjectType, OptionType, StringType, fields }

trait MutationType {
  this: DepositType
    with StateType
    with IngestStepType
    with IdentifierGraphQLType
    with DoiEventTypes
    with CurationType
    with SpringfieldType
    with ContentTypeGraphQLType
    with Scalars =>

  case class AddDepositPayload(clientMutationId: Option[String], depositId: DepositId) extends Mutation
  case class UpdateStatePayload(clientMutationId: Option[String], objectId: String) extends Mutation
  case class UpdateIngestStepPayload(clientMutationId: Option[String], objectId: String) extends Mutation
  case class AddIdentifierPayload(clientMutationId: Option[String], objectId: String) extends Mutation
  case class SetDoiRegisteredPayload(clientMutationId: Option[String], obj: DoiRegisteredEvent) extends Mutation
  case class SetDoiActionPayload(clientMutationId: Option[String], obj: DoiActionEvent) extends Mutation
  case class SetCurationPayload(clientMutationId: Option[String], objectId: String) extends Mutation
  case class SetSpringfieldPayload(clientMutationId: Option[String], objectId: String) extends Mutation
  case class SetContentTypePayload(clientMutationId: Option[String], objectId: String) extends Mutation

  private val depositIdInputField: InputField[DepositId] = InputField(
    name = "depositId",
    description = Some("The deposit's identifier."),
    defaultValue = None,
    fieldType = UUIDType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val bagNameInputField: InputField[String] = InputField(
    name = "bagName",
    description = Some("The name of the deposited bag."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val creationTimestampInputField: InputField[Timestamp] = InputField(
    name = "creationTimestamp",
    description = Some("The timestamp at which this deposit was created."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val depositorIdInputField: InputField[DepositorId] = InputField(
    name = "depositorId",
    description = Some("The depositor that submits this deposit."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val stateLabelInputField: InputField[StateLabel.Value] = InputField(
    name = "label",
    description = Some("The state label of the deposit."),
    defaultValue = None,
    fieldType = StateLabelType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val stateDescriptionInputField: InputField[String] = InputField(
    name = "description",
    description = Some("Additional information about the state."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val stateTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which the deposit got into this state."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val ingestStepLabelInputField: InputField[IngestStepLabel.Value] = InputField(
    name = "step",
    description = Some("The label of the ingest step."),
    defaultValue = None,
    fieldType = StepLabelType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val ingestStepTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which the deposit got into this ingest step."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val identifierTypeInputField: InputField[IdentifierType.Value] = InputField(
    name = "type",
    description = Some("The type of identifier."),
    defaultValue = None,
    fieldType = IdentifierTypeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val identifierValueInputField: InputField[String] = InputField(
    name = "value",
    description = Some("The value of the identifier."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val identifierTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which the identifier got added to this deposit."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val doiRegisteredValueInputField: InputField[Boolean] = InputField(
    name = "value",
    description = Some("Whether the DOI is registered in DataCite."),
    defaultValue = None,
    fieldType = BooleanType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val doiRegisteredTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which the DOI was registered in DataCite."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val doiActionValueInputField: InputField[DoiAction.Value] = InputField(
    name = "value",
    description = Some("Whether the DOI must be 'created' or 'updated' when registering in DataCite."),
    defaultValue = None,
    fieldType = DoiActionType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val doiActionTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which this value was added."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationUserIdInputField: InputField[String] = InputField(
    name = "datamanagerUserId",
    description = Some("The data manager's username in EASY."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationEmailInputField: InputField[String] = InputField(
    name = "datamanagerEmail",
    description = Some("The data manager's email address."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val isNewVersionValueInputField: InputField[Boolean] = InputField(
    name = "isNewVersion",
    description = Some("True if the deposit is a new version."),
    defaultValue = None,
    fieldType = BooleanType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationRequiredValueInputField: InputField[Boolean] = InputField(
    name = "isCurationRequired",
    description = Some("True if curation by a data manager is required."),
    defaultValue = None,
    fieldType = BooleanType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationPerformedValueInputField: InputField[Boolean] = InputField(
    name = "isCurationPerformed",
    description = Some("True if curation by the data manager has been performed."),
    defaultValue = None,
    fieldType = BooleanType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which the curation event was assigned to this deposit."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val springfieldDomainInputField: InputField[String] = InputField(
    name = "domain",
    description = Some("The domain of Springfield."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val springfieldUserInputField: InputField[String] = InputField(
    name = "user",
    description = Some("The user of Springfield."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val springfieldCollectionInputField: InputField[String] = InputField(
    name = "collection",
    description = Some("The collection of Springfield."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val springfieldPlayModeInputField: InputField[SpringfieldPlayMode.Value] = InputField(
    name = "playmode",
    description = Some("The playmode used in Springfield."),
    defaultValue = None,
    fieldType = SpringfieldPlayModeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val springfieldTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which this springfield configuration was associated with the deposit."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val contentTypeValueInputField: InputField[ContentTypeValue.Value] = InputField(
    name = "value",
    description = Some("The content type associated with this deposit."),
    defaultValue = None,
    fieldType = ContentTypeValueType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val contentTypeTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which this springfield configuration was associated with the deposit."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )

  private val depositField: Field[DataContext, AddDepositPayload] = Field(
    name = "deposit",
    fieldType = OptionType(DepositType),
    resolve = ctx => ctx.ctx.repo.deposits.find(ctx.value.depositId).toTry,
  )
  private val stateField: Field[DataContext, UpdateStatePayload] = Field(
    name = "state",
    fieldType = OptionType(StateType),
    resolve = ctx => ctx.ctx.repo.states.getById(ctx.value.objectId).toTry,
  )
  private val ingestStepField: Field[DataContext, UpdateIngestStepPayload] = Field(
    name = "ingestStep",
    fieldType = OptionType(IngestStepType),
    resolve = ctx => ctx.ctx.repo.ingestSteps.getById(ctx.value.objectId).toTry,
  )
  private val identifierField: Field[DataContext, AddIdentifierPayload] = Field(
    name = "identifier",
    fieldType = OptionType(IdentifierObjectType),
    resolve = ctx => ctx.ctx.repo.identifiers.getById(ctx.value.objectId).toTry,
  )
  private val doiRegisteredField: Field[DataContext, SetDoiRegisteredPayload] = Field(
    name = "doiRegistered",
    fieldType = OptionType(DoiRegisteredEventType),
    resolve = ctx => ctx.value.obj,
  )
  private val doiActionField: Field[DataContext, SetDoiActionPayload] = Field(
    name = "doiAction",
    fieldType = OptionType(DoiActionEventType),
    resolve = ctx => ctx.value.obj,
  )
  private val curationField: Field[DataContext, SetCurationPayload] = Field(
    name = "curation",
    fieldType = OptionType(CurationType),
    resolve = ctx => ctx.ctx.repo.curation.getById(ctx.value.objectId).toTry
  )
  private val springfieldField: Field[DataContext, SetSpringfieldPayload] = Field(
    name = "springfield",
    fieldType = OptionType(SpringfieldType),
    resolve = ctx => ctx.ctx.repo.springfield.getById(ctx.value.objectId).toTry,
  )
  private val contentTypeField: Field[DataContext, SetContentTypePayload] = Field(
    name = "contentType",
    fieldType = OptionType(ContentTypeType),
    resolve = ctx => ctx.ctx.repo.contentType.getById(ctx.value.objectId).toTry,
  )

  private val addDepositField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, AddDepositPayload, InputObjectType.DefaultInput](
    fieldName = "addDeposit",
    typeName = "AddDeposit",
    fieldDescription = Some("Register a new deposit with 'id', 'creationTimestamp' and 'depositId'."),
    inputFields = List(
      depositIdInputField,
      bagNameInputField,
      creationTimestampInputField,
      depositorIdInputField,
    ),
    outputFields = fields(
      depositField,
    ),
    mutateAndGetPayload = addDeposit,
  )
  private val updateStateField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, UpdateStatePayload, InputObjectType.DefaultInput](
    fieldName = "updateState",
    typeName = "UpdateState",
    fieldDescription = Some("Update the state of the deposit identified by 'id'."),
    inputFields = List(
      depositIdInputField,
      stateLabelInputField,
      stateDescriptionInputField,
      stateTimestampInputField,
    ),
    outputFields = fields(
      stateField,
    ),
    mutateAndGetPayload = updateState,
  )
  private val updateIngestStepField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, UpdateIngestStepPayload, InputObjectType.DefaultInput](
    fieldName = "updateIngestStep",
    typeName = "UpdateIngestStep",
    fieldDescription = Some("Update the ingest step of the deposit identified by 'id'."),
    inputFields = List(
      depositIdInputField,
      ingestStepLabelInputField,
      ingestStepTimestampInputField,
    ),
    outputFields = fields(
      ingestStepField,
    ),
    mutateAndGetPayload = updateIngestStep,
  )
  private val addIdentifierField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, AddIdentifierPayload, InputObjectType.DefaultInput](
    fieldName = "addIdentifier",
    typeName = "AddIdentifier",
    fieldDescription = Some("Add an identifier to the deposit identified by 'id'."),
    inputFields = List(
      depositIdInputField,
      identifierTypeInputField,
      identifierValueInputField,
      identifierTimestampInputField,
    ),
    outputFields = fields(
      identifierField,
    ),
    mutateAndGetPayload = addIdentifier,
  )
  private val setDoiRegisteredField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, SetDoiRegisteredPayload, InputObjectType.DefaultInput](
    fieldName = "setDoiRegistered",
    typeName = "SetDoiRegistered",
    fieldDescription = Some("Set whether the DOI has been registered in DataCite."),
    inputFields = List(
      depositIdInputField,
      doiRegisteredValueInputField,
      doiRegisteredTimestampInputField,
    ),
    outputFields = fields(
      doiRegisteredField,
    ),
    mutateAndGetPayload = setDoiRegistered,
  )
  private val setDoiActionField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, SetDoiActionPayload, InputObjectType.DefaultInput](
    fieldName = "setDoiAction",
    typeName = "SetDoiAction",
    fieldDescription = Some("Set whether the DOI should be 'created' or 'updated' on registration in DataCite."),
    inputFields = List(
      depositIdInputField,
      doiActionValueInputField,
      doiActionTimestampInputField,
    ),
    outputFields = fields(
      doiActionField,
    ),
    mutateAndGetPayload = setDoiAction,
  )
  private val setCurationField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, SetCurationPayload, InputObjectType.DefaultInput](
    fieldName = "setCuration",
    typeName = "SetCuration",
    fieldDescription = Some("Assign a curation event to the deposit identified by 'id'."),
    inputFields = List(
      depositIdInputField,
      curationUserIdInputField,
      curationEmailInputField,
      isNewVersionValueInputField,
      curationRequiredValueInputField,
      curationPerformedValueInputField,
      curationTimestampInputField,
    ),
    outputFields = fields(
      curationField,
    ),
    mutateAndGetPayload = setCuration,
  )
  private val setSpringfieldField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, SetSpringfieldPayload, InputObjectType.DefaultInput](
    fieldName = "setSpringfield",
    typeName = "SetSpringfield",
    fieldDescription = Some("Set the springfield configuration for this deposit."),
    inputFields = List(
      depositIdInputField,
      springfieldDomainInputField,
      springfieldUserInputField,
      springfieldCollectionInputField,
      springfieldPlayModeInputField,
      springfieldTimestampInputField,
    ),
    outputFields = fields(
      springfieldField,
    ),
    mutateAndGetPayload = setSpringfield,
  )
  private val setContentTypeField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, SetContentTypePayload, InputObjectType.DefaultInput](
    fieldName = "setContentType",
    typeName = "SetContentType",
    fieldDescription = Some("Set the content type for this deposit."),
    inputFields = List(
      depositIdInputField,
      contentTypeValueInputField,
      contentTypeTimestampInputField,
    ),
    outputFields = fields(
      contentTypeField,
    ),
    mutateAndGetPayload = setContentType,
  )

  private def addDeposit(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, AddDepositPayload] = {
    context.ctx.repo.deposits
      .store(Deposit(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        bagName = Option(input(bagNameInputField.name).asInstanceOf[String]),
        creationTimestamp = input(creationTimestampInputField.name).asInstanceOf[Timestamp],
        depositorId = input(depositorIdInputField.name).asInstanceOf[String],
      ))
      .map(deposit => AddDepositPayload(
        clientMutationId = input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        depositId = deposit.id,
      ))
      .toTry
  }

  private def updateState(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, UpdateStatePayload] = {
    context.ctx.repo.states
      .store(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        state = InputState(
          label = input(stateLabelInputField.name).asInstanceOf[StateLabel.Value],
          description = input(stateDescriptionInputField.name).asInstanceOf[String],
          timestamp = input(stateTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(state => UpdateStatePayload(
        clientMutationId = input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        objectId = state.id,
      ))
      .toTry
  }

  private def updateIngestStep(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, UpdateIngestStepPayload] = {
    context.ctx.repo.ingestSteps
      .store(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        step = InputIngestStep(
          step = input(ingestStepLabelInputField.name).asInstanceOf[IngestStepLabel.Value],
          timestamp = input(ingestStepTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(ingestStep => UpdateIngestStepPayload(
        clientMutationId = input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        objectId = ingestStep.id,
      ))
      .toTry
  }

  private def addIdentifier(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, AddIdentifierPayload] = {
    context.ctx.repo.identifiers
      .store(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        identifier = InputIdentifier(
          idType = input(identifierTypeInputField.name).asInstanceOf[IdentifierType.Value],
          idValue = input(identifierValueInputField.name).asInstanceOf[String],
          timestamp = input(identifierTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(identifier => AddIdentifierPayload(
        clientMutationId = input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        objectId = identifier.id,
      ))
      .toTry
  }

  private def setDoiRegistered(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, SetDoiRegisteredPayload] = {
    context.ctx.repo.doiRegistered
      .store(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        registered = DoiRegisteredEvent(
          value = input(doiRegisteredValueInputField.name).asInstanceOf[Boolean],
          timestamp = input(doiRegisteredTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(doiRegisteredEvent => SetDoiRegisteredPayload(
        input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        doiRegisteredEvent,
      ))
      .toTry
  }

  private def setDoiAction(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, SetDoiActionPayload] = {
    context.ctx.repo.doiAction
      .store(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        action = DoiActionEvent(
          value = input(doiActionValueInputField.name).asInstanceOf[DoiAction.Value],
          timestamp = input(doiActionTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(doiActionEvent => SetDoiActionPayload(
        clientMutationId = input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        obj = doiActionEvent,
      ))
      .toTry
  }

  private def setCuration(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, SetCurationPayload] = {
    context.ctx.repo.curation
      .store(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        curation = InputCuration(
          isNewVersion = input(isNewVersionValueInputField.name).asInstanceOf[Boolean],
          isRequired = input(curationRequiredValueInputField.name).asInstanceOf[Boolean],
          isPerformed = input(curationPerformedValueInputField.name).asInstanceOf[Boolean],
          datamanagerUserId = input(curationUserIdInputField.name).asInstanceOf[String],
          datamanagerEmail = input(curationEmailInputField.name).asInstanceOf[String],
          timestamp = input(curationTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(curation => SetCurationPayload(
        clientMutationId = input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        objectId = curation.id,
      ))
      .toTry
  }

  private def setSpringfield(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, SetSpringfieldPayload] = {
    context.ctx.repo.springfield
      .store(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        springfield = InputSpringfield(
          domain = input(springfieldDomainInputField.name).asInstanceOf[String],
          user = input(springfieldUserInputField.name).asInstanceOf[String],
          collection = input(springfieldCollectionInputField.name).asInstanceOf[String],
          playmode = input(springfieldPlayModeInputField.name).asInstanceOf[SpringfieldPlayMode.Value],
          timestamp = input(springfieldTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(springfield => SetSpringfieldPayload(
        clientMutationId = input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        objectId = springfield.id,
      ))
      .toTry
  }

  private def setContentType(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, SetContentTypePayload] = {
    context.ctx.repo.contentType
      .store(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        contentType = InputContentType(
          value = input(contentTypeValueInputField.name).asInstanceOf[ContentTypeValue.Value],
          timestamp = input(contentTypeTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(contentType => SetContentTypePayload(
        input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        contentType.id,
      ))
      .toTry
  }

  implicit val MutationType: ObjectType[DataContext, Unit] = ObjectType(
    name = "Mutation",
    description = "The root query for implementing GraphQL mutations.",
    fields = fields[DataContext, Unit](
      addDepositField,
      updateStateField,
      updateIngestStepField,
      addIdentifierField,
      setDoiRegisteredField,
      setDoiActionField,
      setCurationField,
      setSpringfieldField,
      setContentTypeField,
    ),
  )
}
