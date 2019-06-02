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
import nl.knaw.dans.easy.properties.app.model.curator.{ Curator, InputCurator }
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ IngestStep, InputIngestStep }
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield }
import nl.knaw.dans.easy.properties.app.model.state.{ InputState, State }
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, Deposit, DepositId, DepositorId, DoiActionEvent, DoiRegisteredEvent, IsNewVersionEvent, Timestamp }
import sangria.marshalling.FromInput.coercedScalaInput
import sangria.schema.{ Argument, Context, Field, ObjectType, OptionType, StringType, fields }

trait MutationType {
  this: DepositType
    with StateType
    with IngestStepType
    with IdentifierGraphQLType
    with DoiEventTypes
    with CuratorType
    with CurationEventType
    with SpringfieldType
    with Scalars =>

  private val depositIdArgument: Argument[DepositId] = Argument(
    name = "depositId",
    description = Some("The deposit's identifier."),
    defaultValue = None,
    argumentType = UUIDType,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val creationTimestampArgument: Argument[Timestamp] = Argument(
    name = "creationTimestamp",
    description = Some("The timestamp at which this deposit was created."),
    defaultValue = None,
    argumentType = DateTimeType,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val depositorIdArgument: Argument[DepositorId] = Argument(
    name = "depositorId",
    description = Some("The depositor that submits this deposit."),
    defaultValue = None,
    argumentType = StringType,
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val stateArgument: Argument[InputState] = Argument(
    name = "state",
    description = Some("The deposit's state to be updated."),
    defaultValue = None,
    argumentType = InputStateType,
    fromInput = InputStateFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val ingestStepArgument: Argument[InputIngestStep] = Argument(
    name = "ingestStep",
    description = Some("The ingest step to be updated."),
    defaultValue = None,
    argumentType = IngestStepInputType,
    fromInput = InputIngestStepFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val identifierArgument: Argument[InputIdentifier] = Argument(
    name = "identifier",
    description = Some("The identifier to be added."),
    defaultValue = None,
    argumentType = InputIdentifierType,
    fromInput = InputIdentifierFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val doiRegisteredArgument: Argument[DoiRegisteredEvent] = Argument(
    name = "doiRegistered",
    description = Some("The DOI registration event to be set."),
    defaultValue = None,
    argumentType = InputDoiRegisteredEventType,
    fromInput = inputDoiRegisteredEventFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val doiActionArgument: Argument[DoiActionEvent] = Argument(
    name = "doiAction",
    description = Some("The DOI action event to be set."),
    defaultValue = None,
    argumentType = InputDoiActionEventType,
    fromInput = inputDoiActionEventFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curatorArgument: Argument[InputCurator] = Argument(
    name = "curator",
    description = Some("The data manager to be assigned to this deposit."),
    defaultValue = None,
    argumentType = InputCuratorType,
    fromInput = InputCuratorFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val isNewVersionArgument: Argument[IsNewVersionEvent] = Argument(
    name = "isNewVersion",
    description = Some("Whether this deposit is a new version."),
    defaultValue = None,
    argumentType = InputIsNewVersionEventType,
    fromInput = inputIsNewVersionEventFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationRequiredArgument: Argument[CurationRequiredEvent] = Argument(
    name = "curationRequired",
    description = Some("Whether this deposit requires curation."),
    defaultValue = None,
    argumentType = InputCurationRequiredEventType,
    fromInput = inputCurationRequiredEventFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val curationPerformedArgument: Argument[CurationPerformedEvent] = Argument(
    name = "curationPerformed",
    description = Some("Whether curation is performed on this deposit."),
    defaultValue = None,
    argumentType = InputCurationPerformedEventType,
    fromInput = inputCurationPerformedEventFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  private val springfieldArgument: Argument[InputSpringfield] = Argument(
    name = "springfield",
    description = Some("The springfield configuration to be associated with this deposit."),
    defaultValue = None,
    argumentType = InputSpringfieldType,
    fromInput = inputSpringfieldFromInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )

  private val addDepositField: Field[DataContext, Unit] = Field(
    name = "addDeposit",
    description = Some("Register a new deposit with 'id', 'creationTimestamp' and 'depositId'."),
    arguments = List(
      depositIdArgument,
      creationTimestampArgument,
      depositorIdArgument,
    ),
    fieldType = OptionType(DepositType),
    resolve = addDeposit,
  )
  private val updateStateField: Field[DataContext, Unit] = Field(
    name = "updateState",
    description = Some("Update the state of the deposit identified by 'id'."),
    arguments = List(
      depositIdArgument,
      stateArgument,
    ),
    fieldType = OptionType(StateType),
    resolve = updateState,
  )
  private val updateIngestStepField: Field[DataContext, Unit] = Field(
    name = "updateIngestStep",
    description = Some("Update the ingest step of the deposit identified by 'id'."),
    arguments = List(
      depositIdArgument,
      ingestStepArgument,
    ),
    fieldType = OptionType(IngestStepType),
    resolve = updateIngestStep,
  )
  private val addIdentifierField: Field[DataContext, Unit] = Field(
    name = "addIdentifier",
    description = Some("Add an identifier to the deposit identified by 'id'."),
    arguments = List(
      depositIdArgument,
      identifierArgument,
    ),
    fieldType = OptionType(IdentifierObjectType),
    resolve = addIdentifier,
  )
  private val setDoiRegisteredField: Field[DataContext, Unit] = Field(
    name = "setDoiRegistered",
    description = Some("Set whether the DOI has been registered in DataCite."),
    arguments = List(
      depositIdArgument,
      doiRegisteredArgument,
    ),
    fieldType = OptionType(DoiRegisteredEventType),
    resolve = setDoiRegistered,
  )
  private val setDoiActionField: Field[DataContext, Unit] = Field(
    name = "setDoiAction",
    description = Some("Set whether the DOI should be 'created' or 'updated' on registration in DataCite."),
    arguments = List(
      depositIdArgument,
      doiActionArgument,
    ),
    fieldType = OptionType(DoiActionEventType),
    resolve = setDoiAction,
  )
  private val setCuratorField: Field[DataContext, Unit] = Field(
    name = "setCurator",
    description = Some("Assign a data manager to the deposit identified by 'id'."),
    arguments = List(
      depositIdArgument,
      curatorArgument,
    ),
    fieldType = OptionType(CuratorType),
    resolve = setCurator,
  )
  private val setIsNewVersionField: Field[DataContext, Unit] = Field(
    name = "setIsNewVersion",
    description = Some("Set whether this deposit is a new version."),
    arguments = List(
      depositIdArgument,
      isNewVersionArgument,
    ),
    fieldType = OptionType(IsNewVersionEventType),
    resolve = setIsNewVersion,
  )
  private val setCurationRequiredField: Field[DataContext, Unit] = Field(
    name = "setCurationRequired",
    description = Some("Set whether this deposit requires curation."),
    arguments = List(
      depositIdArgument,
      curationRequiredArgument,
    ),
    fieldType = OptionType(CurationRequiredEventType),
    resolve = setCurationRequired,
  )
  private val setCurationPerformedField: Field[DataContext, Unit] = Field(
    name = "setCurationPerformed",
    description = Some("Set whether curation is performed on this deposit."),
    arguments = List(
      depositIdArgument,
      curationPerformedArgument,
    ),
    fieldType = OptionType(CurationPerformedEventType),
    resolve = setCurationPerformed,
  )
  private val setSpringfieldField: Field[DataContext, Unit] = Field(
    name = "setSpringfield",
    description = Some("Set the springfield configuration for this deposit."),
    arguments = List(
      depositIdArgument,
      springfieldArgument,
    ),
    fieldType = OptionType(SpringfieldType),
    resolve = setSpringfield,
  )

  private def addDeposit(context: Context[DataContext, Unit]): Option[Deposit] = {
    val repository = context.ctx.deposits

    val id = context.arg(depositIdArgument)
    val creationTimestamp = context.arg(creationTimestampArgument)
    val depositorId = context.arg(depositorIdArgument)

    repository.addDeposit(Deposit(id, creationTimestamp, depositorId))
  }

  private def updateState(context: Context[DataContext, Unit]): Option[State] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val state = context.arg(stateArgument)

    repository.setState(depositId, state)
  }

  private def updateIngestStep(context: Context[DataContext, Unit]): Option[IngestStep] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val ingestStep = context.arg(ingestStepArgument)

    repository.setIngestStep(depositId, ingestStep)
  }

  private def addIdentifier(context: Context[DataContext, Unit]): Option[Identifier] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val identifier = context.arg(identifierArgument)

    repository.addIdentifier(depositId, identifier)
  }

  private def setDoiRegistered(context: Context[DataContext, Unit]): Option[DoiRegisteredEvent] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val doiRegistered = context.arg(doiRegisteredArgument)

    repository.setDoiRegistered(depositId, doiRegistered)
  }

  private def setDoiAction(context: Context[DataContext, Unit]): Option[DoiActionEvent] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val doiAction = context.arg(doiActionArgument)

    repository.setDoiAction(depositId, doiAction)
  }

  private def setCurator(context: Context[DataContext, Unit]): Option[Curator] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val curator = context.arg(curatorArgument)

    repository.setCurator(depositId, curator)
  }

  private def setIsNewVersion(context: Context[DataContext, Unit]): Option[IsNewVersionEvent] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val isNewVersion = context.arg(isNewVersionArgument)

    repository.setIsNewVersionAction(depositId, isNewVersion)
  }

  private def setCurationRequired(context: Context[DataContext, Unit]): Option[CurationRequiredEvent] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val curationRequired = context.arg(curationRequiredArgument)

    repository.setCurationRequiredAction(depositId, curationRequired)
  }

  private def setCurationPerformed(context: Context[DataContext, Unit]): Option[CurationPerformedEvent] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val curationPerformed = context.arg(curationPerformedArgument)

    repository.setCurationPerformedAction(depositId, curationPerformed)
  }

  private def setSpringfield(context: Context[DataContext, Unit]): Option[Springfield] = {
    val repository = context.ctx.deposits

    val depositId = context.arg(depositIdArgument)
    val springfield = context.arg(springfieldArgument)

    repository.setSpringfield(depositId, springfield)
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
    ),
  )
}
