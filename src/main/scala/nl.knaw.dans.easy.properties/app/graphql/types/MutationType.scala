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
import nl.knaw.dans.easy.properties.app.model.curator.InputCurator
import nl.knaw.dans.easy.properties.app.model.identifier.{ IdentifierType, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStepLabel, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, SpringfieldPlayMode }
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, StateLabel }
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, Deposit, DepositId, DepositorId, DoiAction, DoiActionEvent, DoiRegisteredEvent, IsNewVersionEvent, Timestamp }
import sangria.relay.Mutation
import sangria.schema.{ Action, BooleanType, Context, Field, InputField, InputObjectType, ObjectType, OptionType, StringType, fields }

trait MutationType {
  this: DepositType
    with StateType
    with IngestStepType
    with IdentifierGraphQLType
    with DoiEventTypes
    with CuratorType
    with CurationEventType
    with SpringfieldType
    with ContentTypeGraphQLType
    with Scalars =>

  case class AddDepositPayload(clientMutationId: Option[String], depositId: DepositId) extends Mutation
  case class UpdateStatePayload(clientMutationId: Option[String], objectId: String) extends Mutation
  case class UpdateIngestStepPayload(clientMutationId: Option[String], objectId: String) extends Mutation
  case class AddIdentifierPayload(clientMutationId: Option[String], objectId: String) extends Mutation
  case class SetDoiRegisteredPayload(clientMutationId: Option[String], obj: DoiRegisteredEvent) extends Mutation
  case class SetDoiActionPayload(clientMutationId: Option[String], obj: DoiActionEvent) extends Mutation
  case class SetCuratorPayload(clientMutationId: Option[String], objectId: String) extends Mutation
  case class SetIsNewVersionPayload(clientMutationId: Option[String], obj: IsNewVersionEvent) extends Mutation
  case class SetCurationRequiredPayload(clientMutationId: Option[String], obj: CurationRequiredEvent) extends Mutation
  case class SetCurationPerformedPayload(clientMutationId: Option[String], obj: CurationPerformedEvent) extends Mutation
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
  private val curatorUserIdInputField: InputField[String] = InputField(
    name = "userId",
    description = Some("The data manager's username in EASY."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curatorEmailInputField: InputField[String] = InputField(
    name = "email",
    description = Some("The data manager's email address."),
    defaultValue = None,
    fieldType = StringType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curatorTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which the data manager was assigned to this deposit."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val isNewVersionValueInputField: InputField[Boolean] = InputField(
    name = "value",
    description = Some("True if the deposit is a new version."),
    defaultValue = None,
    fieldType = BooleanType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val isNewVersionTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which was decided that this is a new version."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationRequiredValueInputField: InputField[Boolean] = InputField(
    name = "value",
    description = Some("True if curation by a data manager is required."),
    defaultValue = None,
    fieldType = BooleanType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationRequiredTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which was decided that this deposit requires curation."),
    defaultValue = None,
    fieldType = DateTimeType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationPerformedValueInputField: InputField[Boolean] = InputField(
    name = "value",
    description = Some("True if curation by the data manager has been performed."),
    defaultValue = None,
    fieldType = BooleanType,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationPerformedTimestampInputField: InputField[Timestamp] = InputField(
    name = "timestamp",
    description = Some("The timestamp at which the curation was completed."),
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
    resolve = ctx => ctx.ctx.deposits.getDeposit(ctx.value.depositId).toTry,
  )
  private val stateField: Field[DataContext, UpdateStatePayload] = Field(
    name = "state",
    fieldType = OptionType(StateType),
    resolve = ctx => ctx.ctx.deposits.getStateById(ctx.value.objectId).toTry,
  )
  private val ingestStepField: Field[DataContext, UpdateIngestStepPayload] = Field(
    name = "ingestStep",
    fieldType = OptionType(IngestStepType),
    resolve = ctx => ctx.ctx.deposits.getIngestStepById(ctx.value.objectId).toTry,
  )
  private val identifierField: Field[DataContext, AddIdentifierPayload] = Field(
    name = "identifier",
    fieldType = OptionType(IdentifierObjectType),
    resolve = ctx => ctx.ctx.deposits.getIdentifierById(ctx.value.objectId).toTry,
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
  private val curatorField: Field[DataContext, SetCuratorPayload] = Field(
    name = "curator",
    fieldType = OptionType(CuratorType),
    resolve = ctx => ctx.ctx.deposits.getCuratorById(ctx.value.objectId).toTry,
  )
  private val isNewVersionField: Field[DataContext, SetIsNewVersionPayload] = Field(
    name = "isNewVersion",
    fieldType = OptionType(IsNewVersionEventType),
    resolve = ctx => ctx.value.obj,
  )
  private val curationRequiredField: Field[DataContext, SetCurationRequiredPayload] = Field(
    name = "curationRequired",
    fieldType = OptionType(CurationRequiredEventType),
    resolve = ctx => ctx.value.obj,
  )
  private val curationPerformedField: Field[DataContext, SetCurationPerformedPayload] = Field(
    name = "curationPerformed",
    fieldType = OptionType(CurationPerformedEventType),
    resolve = ctx => ctx.value.obj,
  )
  private val springfieldField: Field[DataContext, SetSpringfieldPayload] = Field(
    name = "springfield",
    fieldType = OptionType(SpringfieldType),
    resolve = ctx => ctx.ctx.deposits.getSpringfieldById(ctx.value.objectId).toTry,
  )
  private val contentTypeField: Field[DataContext, SetContentTypePayload] = Field(
    name = "contentType",
    fieldType = OptionType(ContentTypeType),
    resolve = ctx => ctx.ctx.deposits.getContentTypeById(ctx.value.objectId).toTry,
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
  private val setCuratorField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, SetCuratorPayload, InputObjectType.DefaultInput](
    fieldName = "setCurator",
    typeName = "SetCurator",
    fieldDescription = Some("Assign a data manager to the deposit identified by 'id'."),
    inputFields = List(
      depositIdInputField,
      curatorUserIdInputField,
      curatorEmailInputField,
      curatorTimestampInputField,
    ),
    outputFields = fields(
      curatorField,
    ),
    mutateAndGetPayload = setCurator,
  )
  private val setIsNewVersionField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, SetIsNewVersionPayload, InputObjectType.DefaultInput](
    fieldName = "setIsNewVersion",
    typeName = "SetIsNewVersion",
    fieldDescription = Some("Set whether this deposit is a new version."),
    inputFields = List(
      depositIdInputField,
      isNewVersionValueInputField,
      isNewVersionTimestampInputField,
    ),
    outputFields = fields(
      isNewVersionField,
    ),
    mutateAndGetPayload = setIsNewVersion,
  )
  private val setCurationRequiredField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, SetCurationRequiredPayload, InputObjectType.DefaultInput](
    fieldName = "setCurationRequired",
    typeName = "SetCurationRequired",
    fieldDescription = Some("Set whether this deposit requires curation."),
    inputFields = List(
      depositIdInputField,
      curationRequiredValueInputField,
      curationRequiredTimestampInputField,
    ),
    outputFields = fields(
      curationRequiredField,
    ),
    mutateAndGetPayload = setCurationRequired,
  )
  private val setCurationPerformedField: Field[DataContext, Unit] = Mutation.fieldWithClientMutationId[DataContext, Unit, SetCurationPerformedPayload, InputObjectType.DefaultInput](
    fieldName = "setCurationPerformed",
    typeName = "SetCurationPerformed",
    fieldDescription = Some("Set whether curation is performed on this deposit."),
    inputFields = List(
      depositIdInputField,
      curationPerformedValueInputField,
      curationPerformedTimestampInputField,
    ),
    outputFields = fields(
      curationPerformedField,
    ),
    mutateAndGetPayload = setCurationPerformed,
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
    context.ctx.deposits
      .addDeposit(Deposit(
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
    context.ctx.deposits
      .setState(
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
    context.ctx.deposits
      .setIngestStep(
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
    context.ctx.deposits
      .addIdentifier(
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
    context.ctx.deposits
      .setDoiRegistered(
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
    context.ctx.deposits
      .setDoiAction(
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

  private def setCurator(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, SetCuratorPayload] = {
    context.ctx.deposits
      .setCurator(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        curator = InputCurator(
          userId = input(curatorUserIdInputField.name).asInstanceOf[String],
          email = input(curatorEmailInputField.name).asInstanceOf[String],
          timestamp = input(curatorTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(curator => SetCuratorPayload(
        clientMutationId = input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        objectId = curator.id,
      ))
      .toTry
  }

  private def setIsNewVersion(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, SetIsNewVersionPayload] = {
    context.ctx.deposits
      .setIsNewVersionAction(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        action = IsNewVersionEvent(
          isNewVersion = input(isNewVersionValueInputField.name).asInstanceOf[Boolean],
          timestamp = input(isNewVersionTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(isNewVersionEvent => SetIsNewVersionPayload(
        clientMutationId = input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        obj = isNewVersionEvent
      ))
      .toTry
  }

  private def setCurationRequired(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, SetCurationRequiredPayload] = {
    context.ctx.deposits
      .setCurationRequiredAction(
        input(depositIdInputField.name).asInstanceOf[DepositId],
        CurationRequiredEvent(
          input(curationRequiredValueInputField.name).asInstanceOf[Boolean],
          input(curationRequiredTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(curationRequiredEvent => SetCurationRequiredPayload(
        input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        curationRequiredEvent
      ))
      .toTry
  }

  private def setCurationPerformed(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, SetCurationPerformedPayload] = {
    context.ctx.deposits
      .setCurationPerformedAction(
        id = input(depositIdInputField.name).asInstanceOf[DepositId],
        action = CurationPerformedEvent(
          curationPerformed = input(curationPerformedValueInputField.name).asInstanceOf[Boolean],
          timestamp = input(curationPerformedTimestampInputField.name).asInstanceOf[Timestamp],
        ),
      )
      .map(curationPerformedEvent => SetCurationPerformedPayload(
        clientMutationId = input.get(Mutation.ClientMutationIdFieldName).flatMap(_.asInstanceOf[Option[String]]),
        obj = curationPerformedEvent,
      ))
      .toTry
  }

  private def setSpringfield(input: InputObjectType.DefaultInput, context: Context[DataContext, Unit]): Action[DataContext, SetSpringfieldPayload] = {
    context.ctx.deposits
      .setSpringfield(
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
    context.ctx.deposits
      .setContentType(
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
      setCuratorField,
      setIsNewVersionField,
      setCurationRequiredField,
      setCurationPerformedField,
      setSpringfieldField,
      setContentTypeField,
    ),
  )
}
