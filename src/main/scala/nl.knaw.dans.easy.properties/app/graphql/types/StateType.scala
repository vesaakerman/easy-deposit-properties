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
import nl.knaw.dans.easy.properties.app.graphql.relay.ExtendedConnection
import nl.knaw.dans.easy.properties.app.model.State.StateLabel
import nl.knaw.dans.easy.properties.app.model.State.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, InputState, State, Timestamp }
import sangria.execution.deferred.{ Fetcher, HasId }
import sangria.macros.derive._
import sangria.marshalling.FromInput._
import sangria.marshalling.ToInput.ScalarToInput
import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput, ResultMarshaller, ToInput }
import sangria.relay._
import sangria.schema.{ Argument, Context, EnumType, Field, InputObjectType, ObjectType, OptionInputType, OptionType }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait StateType {
  this: DepositType with NodeType with MetaTypes with Scalars =>

  implicit val StateLabelType: EnumType[StateLabel.Value] = deriveEnumType(
    EnumTypeDescription("The label identifying the state of a deposit."),
    DocumentValue("DRAFT", "Open for additional data."),
    DocumentValue("UPLOADED", "Deposit uploaded, waiting to be finalized."),
    DocumentValue("FINALIZING", "Closed and being checked for validity."),
    DocumentValue("INVALID", "Does not contain a valid bag."),
    DocumentValue("SUBMITTED", "Valid and waiting for processing by easy-ingest-flow, or being processed in it."),
    DocumentValue("IN_REVIEW", "Currently undergoing curation by the datamanagers."),
    DocumentValue("REJECTED", "Did not meet the requirements set by easy-ingest-flow for this type of deposit."),
    DocumentValue("FAILED", "Failed to be archived because of some unexpected condition. It may be possible to manually fix this."),
    DocumentValue("FEDORA_ARCHIVED", "Was successfully archived in the Fedora Archive."),
    DocumentValue("ARCHIVED", "Was successfully archived in the data vault."),
  )

  implicit val currentStateHasId: HasId[(DepositId, Option[State]), DepositId] = HasId { case (id, _) => id }
  implicit val allStatesHasId: HasId[(DepositId, Seq[State]), DepositId] = HasId { case (id, _) => id }

  val fetchCurrentStates = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => Future {
    ids match {
      case Seq() => Seq.empty
      case Seq(id) => Seq(id -> ctx.deposits.getCurrentState(id))
      case _ => ctx.deposits.getCurrentStates(ids)
    }
  })
  val fetchAllStates = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => Future {
    ids match {
      case Seq() => Seq.empty
      case Seq(id) => Seq(id -> ctx.deposits.getAllStates(id))
      case _ => ctx.deposits.getAllStates(ids)
    }
  })

  @GraphQLDescription("Mark a query to only search through current states, or also to include past states.")
  object StateFilter extends Enumeration {
    type StateFilter = Value

    // @formatter:off
    @GraphQLDescription("Only search through current states.")
    val LATEST: StateFilter = Value("LATEST")
    @GraphQLDescription("Search through both current and past states.")
    val ALL   : StateFilter = Value("ALL")
    // @formatter:on
  }
  implicit val StateFilterType: EnumType[StateFilter.Value] = deriveEnumType()
  implicit val StateFilterToInput: ToInput[StateFilter.Value, _] = new ScalarToInput
  private val stateFilterArgument: Argument[StateFilter.Value] = Argument(
    name = "stateFilter",
    argumentType = StateFilterType,
    description = Some("Determine whether to search in current states (`LATEST`, default) or all current and past states (`ALL`)."),
    defaultValue = Some(StateFilter.LATEST -> StateFilterToInput),
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )

  case class StateInput(label: StateLabel, filter: StateFilter.Value = StateFilter.LATEST)
  implicit val StateInputType: InputObjectType[StateInput] = deriveInputObjectType(
    InputObjectTypeDescription("The label and filter to be used in searching for deposits"),
    DocumentInputField("label", "If provided, only show deposits with this state."),
    DocumentInputField("filter", "Determine whether to search in current states (`LATEST`, default) or all current and past states (`ALL`)."),
  )
  implicit val StateInputFromInput: FromInput[StateInput] = new FromInput[StateInput] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): StateInput = {
      val ad = node.asInstanceOf[Map[String, Any]]

      StateInput(
        label = ad("label").asInstanceOf[StateLabel],
        filter = ad("filter").asInstanceOf[Option[StateFilter.Value]].getOrElse(StateFilter.LATEST),
      )
    }
  }
  val stateInputArgument: Argument[Option[StateInput]] = {
    Argument(
      name = "state",
      argumentType = OptionInputType(StateInputType),
      description = Some("List only those deposits that have this specified label."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(StateInputFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }

  private val depositField: Field[DataContext, State] = Field(
    name = "deposit",
    description = Some("Returns the deposit that is associated with this particular ingest step"),
    fieldType = OptionType(DepositType),
    resolve = getDepositByState,
  )

  private val depositsField: Field[DataContext, State] = Field(
    name = "deposits",
    description = Some("List all deposits with the same current state label."),
    arguments = List(
      stateFilterArgument,
      optDepositOrderArgument,
    ) ++ Connection.Args.All,
    fieldType = OptionType(depositConnectionType),
    resolve = ctx => ExtendedConnection.connectionFromSeq(getDeposits(ctx), ConnectionArgs(ctx)),
  )

  private def getDepositByState(context: Context[DataContext, State]): Option[Deposit] = {
    val repository = context.ctx.deposits

    val stepId = context.value.id

    repository.getDepositByStateId(stepId)
  }

  private def getDeposits(context: Context[DataContext, State]): Seq[Deposit] = {
    val repository = context.ctx.deposits

    val label = context.value.label
    val stateFilter = context.arg(stateFilterArgument)
    val orderBy = context.arg(optDepositOrderArgument)

    val result = stateFilter match {
      case StateFilter.LATEST => repository.getDepositsByCurrentState(label)
      case StateFilter.ALL => repository.getDepositsByAllStates(label)
    }

    orderBy.fold(result)(order => result.sorted(order.ordering))
  }

  implicit object StateIdentifiable extends Identifiable[State] {
    override def id(state: State): String = state.id
  }

  implicit val StateType: ObjectType[DataContext, State] = deriveObjectType(
    ObjectTypeDescription("The state of the deposit."),
    Interfaces[DataContext, State](nodeInterface),
    ExcludeFields("id"),
    DocumentField("label", "The state label of the deposit."),
    DocumentField("description", "Additional information about the state."),
    DocumentField("timestamp", "The timestamp at which the deposit got into this state."),
    AddFields(
      Node.globalIdField[DataContext, State],
      depositField,
      depositsField,
    ),
  )

  val ConnectionDefinition(_, stateConnectionType) = ExtendedConnection.definition[DataContext, ExtendedConnection, State](
    name = "State",
    nodeType = StateType,
  )

  implicit val InputStateType: InputObjectType[InputState] = deriveInputObjectType(
    InputObjectTypeName("InputState"),
    InputObjectTypeDescription("The state of a deposit"),
    DocumentInputField("label", "The state label of the deposit."),
    DocumentInputField("description", "Additional information about the state."),
    DocumentInputField("timestamp", "The timestamp at which the deposit got into this state."),
  )
  implicit val InputStateFromInput: FromInput[InputState] = new FromInput[InputState] {
    override val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): InputState = {
      val ad = node.asInstanceOf[Map[String, Any]]

      InputState(
        label = ad("label").asInstanceOf[StateLabel.Value],
        description = ad("description").asInstanceOf[String],
        timestamp = ad("timestamp").asInstanceOf[Timestamp],
      )
    }
  }
}
