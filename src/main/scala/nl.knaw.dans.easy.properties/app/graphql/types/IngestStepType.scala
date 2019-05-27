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
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStepFilter.IngestStepFilter
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStepLabel.IngestStepLabel
import nl.knaw.dans.easy.properties.app.model.ingestStep._
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, Timestamp }
import sangria.execution.deferred.{ Fetcher, HasId }
import sangria.macros.derive._
import sangria.marshalling.FromInput.{ coercedScalaInput, inputObjectResultInput, optionInput }
import sangria.marshalling.ToInput.ScalarToInput
import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput, ResultMarshaller, ToInput }
import sangria.relay._
import sangria.schema.{ Argument, Context, EnumType, Field, InputObjectType, ObjectType, OptionInputType, OptionType }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait IngestStepType {
  this: DepositType with NodeType with MetaTypes with Scalars =>

  implicit val IngestStepFilterType: EnumType[IngestStepFilter] = deriveEnumType(
    EnumTypeDescription("Mark a query to only search through current ingest steps, or also to include past ingest steps."),
    DocumentValue("LATEST", "Only search through current ingest steps."),
    DocumentValue("ALL", "Search through both current and past ingest steps.")
  )
  implicit val IngestStepFilterToInput: ToInput[IngestStepFilter, _] = new ScalarToInput

  implicit val StepLabelType: EnumType[IngestStepLabel.Value] = deriveEnumType(
    EnumTypeDescription("The label identifying the ingest step."),
    DocumentValue("VALIDATE", "Started validating the deposit."),
    DocumentValue("PID_GENERATOR", "Persistent identifiers are being generated for this deposit."),
    DocumentValue("FEDORA", "A dissemination copy of the deposit is being made in Fedora."),
    DocumentValue("SPRINGFIELD", "Dissemination copies of the audio/video files in the deposit are being made in Springfield."),
    DocumentValue("BAGSTORE", "Creating an archival copy of the deposit for storage in the vault."),
    DocumentValue("BAGINDEX", "Indexing the archival copy and its relation to other bags."),
    DocumentValue("SOLR4FILES", "The file content of the deposit's payload is being index."),
    DocumentValue("COMPLETED", "The ingest process of this deposit has completed."),
  )

  implicit val currentStepHasId: HasId[(DepositId, Option[IngestStep]), DepositId] = HasId { case (id, _) => id }
  implicit val allStepsHasId: HasId[(DepositId, Seq[IngestStep]), DepositId] = HasId { case (id, _) => id }

  val fetchCurrentIngestSteps = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => Future {
    ids match {
      case Seq() => Seq.empty
      case Seq(id) => Seq(id -> ctx.deposits.getCurrentIngestStep(id))
      case _ => ctx.deposits.getCurrentIngestSteps(ids)
    }
  })
  val fetchAllIngestSteps = Fetcher((ctx: DataContext, ids: Seq[DepositId]) => Future {
    ids match {
      case Seq() => Seq.empty
      case Seq(id) => Seq(id -> ctx.deposits.getAllIngestSteps(id))
      case _ => ctx.deposits.getAllIngestSteps(ids)
    }
  })

  implicit val DepositIngestStepFilterType: InputObjectType[DepositIngestStepFilter] = deriveInputObjectType(
    InputObjectTypeDescription("The label and filter to be used in searching for deposits by ingest step"),
    DocumentInputField("label", "If provided, only show deposits with this state."),
    DocumentInputField("filter", "Determine whether to search in current states (`LATEST`, default) or all current and past states (`ALL`)."),
  )
  implicit val DepositIngestStepFilterFromInput: FromInput[DepositIngestStepFilter] = new FromInput[DepositIngestStepFilter] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): DepositIngestStepFilter = {
      val ad = node.asInstanceOf[Map[String, Any]]

      DepositIngestStepFilter(
        label = ad("label").asInstanceOf[IngestStepLabel],
        filter = ad("filter").asInstanceOf[Option[IngestStepFilter]].getOrElse(IngestStepFilter.LATEST),
      )
    }
  }

  private val ingestStepFilterArgument: Argument[IngestStepFilter] = Argument(
    name = "ingestStepFilter",
    argumentType = IngestStepFilterType,
    description = Some("Determine whether to search in current ingest steps (`LATEST`, default) or all current and past ingest steps (`ALL`)."),
    defaultValue = Some(IngestStepFilter.LATEST -> IngestStepFilterToInput),
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  val depositIngestStepFilterArgument: Argument[Option[DepositIngestStepFilter]] = {
    Argument(
      name = "ingestStep",
      argumentType = OptionInputType(DepositIngestStepFilterType),
      description = Some("List only those deposits that have this specified ingest step label."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(DepositIngestStepFilterFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }

  private val depositField: Field[DataContext, IngestStep] = Field(
    name = "deposit",
    description = Some("Returns the deposit that is associated with this particular ingest step"),
    fieldType = OptionType(DepositType),
    resolve = getDepositByIngestStep,
  )

  private val depositsField: Field[DataContext, IngestStep] = Field(
    name = "deposits",
    description = Some("List all deposits with the same current ingest step."),
    arguments = List(
      ingestStepFilterArgument,
      optDepositOrderArgument,
    ) ++ Connection.Args.All,
    fieldType = OptionType(depositConnectionType),
    resolve = ctx => ExtendedConnection.connectionFromSeq(getDeposits(ctx), ConnectionArgs(ctx)),
  )

  private def getDepositByIngestStep(context: Context[DataContext, IngestStep]): Option[Deposit] = {
    val repository = context.ctx.deposits

    val stepId = context.value.id

    repository.getDepositByIngestStepId(stepId)
  }

  private def getDeposits(context: Context[DataContext, IngestStep]): Seq[Deposit] = {
    val repository = context.ctx.deposits

    val step = context.value.step
    val stepFilter = context.arg(ingestStepFilterArgument)
    val orderBy = context.arg(optDepositOrderArgument)

    val result = repository.getDeposits(
      ingestStepFilter = Some(DepositIngestStepFilter(step, stepFilter))
    )

    orderBy.fold(result)(order => result.sorted(order.ordering))
  }

  implicit object IngestStepIdentifiable extends Identifiable[IngestStep] {
    override def id(step: IngestStep): String = step.id
  }

  implicit lazy val IngestStepType: ObjectType[DataContext, IngestStep] = deriveObjectType(
    ObjectTypeDescription("The ingest step of the deposit."),
    Interfaces[DataContext, IngestStep](nodeInterface),
    ExcludeFields("id"),
    DocumentField("step", "The label of the ingest step."),
    DocumentField("timestamp", "The timestamp at which the deposit got into this ingest step."),
    AddFields(
      Node.globalIdField[DataContext, IngestStep],
      depositField,
      depositsField,
    ),
  )

  val ConnectionDefinition(_, ingestStepConnectionType) = ExtendedConnection.definition[DataContext, ExtendedConnection, IngestStep](
    name = "IngestStep",
    nodeType = IngestStepType,
  )

  implicit val IngestStepInputType: InputObjectType[InputIngestStep] = deriveInputObjectType(
    InputObjectTypeName("InputIngestStep"),
    InputObjectTypeDescription("The ingest step of a deposit"),
    DocumentInputField("step", "The label of the ingest step."),
    DocumentInputField("timestamp", "The timestamp at which the deposit got into this ingest step."),
  )
  implicit val InputIngestStepFromInput: FromInput[InputIngestStep] = new FromInput[InputIngestStep] {
    override val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): InputIngestStep = {
      val ad = node.asInstanceOf[Map[String, Any]]

      InputIngestStep(
        step = ad("step").asInstanceOf[IngestStepLabel],
        timestamp = ad("timestamp").asInstanceOf[Timestamp],
      )
    }
  }
}