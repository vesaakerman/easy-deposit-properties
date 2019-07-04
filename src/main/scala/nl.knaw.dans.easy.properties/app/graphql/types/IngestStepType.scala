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
import nl.knaw.dans.easy.properties.app.graphql.resolvers.{ DepositResolver, IngestStepResolver }
import nl.knaw.dans.easy.properties.app.model.SeriesFilter.SeriesFilter
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStepLabel.IngestStepLabel
import nl.knaw.dans.easy.properties.app.model.ingestStep._
import nl.knaw.dans.easy.properties.app.model.{ Deposit, SeriesFilter, Timestamp, timestampOrdering }
import nl.knaw.dans.easy.properties.app.repository.DepositFilters
import sangria.macros.derive._
import sangria.marshalling.FromInput
import sangria.marshalling.FromInput.{ coercedScalaInput, inputObjectResultInput, optionInput }
import sangria.relay._
import sangria.schema.{ Argument, Context, DeferredValue, EnumType, Field, InputObjectType, ObjectType, OptionInputType, OptionType }

import scala.concurrent.ExecutionContext.Implicits.global

trait IngestStepType {
  this: DepositType
    with TimebasedSearch
    with NodeType
    with MetaTypes
    with Scalars =>

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

  implicit val DepositIngestStepFilterType: InputObjectType[DepositIngestStepFilter] = deriveInputObjectType(
    InputObjectTypeDescription("The label and filter to be used in searching for deposits by ingest step"),
    DocumentInputField("label", "If provided, only show deposits with this state."),
    DocumentInputField("filter", "Determine whether to search in current states (`LATEST`, default) or all current and past states (`ALL`)."),
  )
  implicit val DepositIngestStepFilterFromInput: FromInput[DepositIngestStepFilter] = fromInput(ad => DepositIngestStepFilter(
    label = ad("label").asInstanceOf[IngestStepLabel],
    filter = ad("filter").asInstanceOf[Option[SeriesFilter]].getOrElse(SeriesFilter.LATEST),
  ))

  private val seriesFilterArgument: Argument[SeriesFilter] = Argument(
    name = "ingestStepFilter",
    argumentType = SeriesFilterType,
    description = Some("Determine whether to search in current ingest steps (`LATEST`, default) or all current and past ingest steps (`ALL`)."),
    defaultValue = Some(SeriesFilter.LATEST -> SeriesFilterToInput),
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
    resolve = getDepositByIngestStep(_),
  )

  private val depositsField: Field[DataContext, IngestStep] = Field(
    name = "deposits",
    description = Some("List all deposits with the same current ingest step."),
    arguments = List(
      seriesFilterArgument,
      optDepositOrderArgument,
    ) ::: timebasedSearchArguments ::: Connection.Args.All,
    fieldType = OptionType(depositConnectionType),
    resolve = ctx => getDeposits(ctx).map(ExtendedConnection.connectionFromSeq(_, ConnectionArgs(ctx))),
  )

  private def getDepositByIngestStep(implicit context: Context[DataContext, IngestStep]): DeferredValue[DataContext, Option[Deposit]] = {
    IngestStepResolver.depositByIngestStepId(context.value.id)
  }

  private def getDeposits(implicit context: Context[DataContext, IngestStep]): DeferredValue[DataContext, Seq[Deposit]] = {
    val step = context.value.step
    val stepFilter = context.arg(seriesFilterArgument)

    DepositResolver.findDeposit(DepositFilters(
      ingestStepFilter = Some(DepositIngestStepFilter(step, stepFilter))
    )).map(timebasedFilterAndSort(optDepositOrderArgument))
  }

  implicit lazy val IngestStepType: ObjectType[DataContext, IngestStep] = deriveObjectType(
    ObjectTypeDescription("The ingest step of the deposit."),
    Interfaces[DataContext, IngestStep](nodeInterface),
    DocumentField("step", "The label of the ingest step."),
    DocumentField("timestamp", "The timestamp at which the deposit got into this ingest step."),
    AddFields(
      depositField,
      depositsField,
    ),
    ReplaceField("id", Node.globalIdField[DataContext, IngestStep]),
  )

  val ConnectionDefinition(_, ingestStepConnectionType) = ExtendedConnection.definition[DataContext, ExtendedConnection, IngestStep](
    name = "IngestStep",
    nodeType = IngestStepType,
  )

  @GraphQLDescription("Properties by which ingest steps can be ordered")
  object IngestStepOrderField extends Enumeration {
    type IngestStepOrderField = Value

    // @formatter:off
    @GraphQLDescription("Order ingest steps by step")
    val STEP    : IngestStepOrderField = Value("STEP")
    @GraphQLDescription("Order ingest steps by timestamp")
    val TIMESTAMP: IngestStepOrderField = Value("TIMESTAMP")
    // @formatter:on
  }
  implicit val IngestStepOrderFieldType: EnumType[IngestStepOrderField.Value] = deriveEnumType()

  case class IngestStepOrder(field: IngestStepOrderField.IngestStepOrderField,
                             direction: OrderDirection.OrderDirection) extends Ordering[IngestStep] {
    def compare(x: IngestStep, y: IngestStep): Int = {
      val orderByField: Ordering[IngestStep] = field match {
        case IngestStepOrderField.STEP =>
          Ordering[IngestStepLabel].on(_.step)
        case IngestStepOrderField.TIMESTAMP =>
          Ordering[Timestamp].on(_.timestamp)
      }

      direction.withOrder(orderByField).compare(x, y)
    }
  }
  implicit val IngestStepOrderInputType: InputObjectType[IngestStepOrder] = deriveInputObjectType(
    InputObjectTypeDescription("Ordering options for ingest steps"),
    DocumentInputField("field", "The field to order ingest steps by"),
    DocumentInputField("direction", "The ordering direction"),
  )
  implicit val IngestStepOrderFromInput: FromInput[IngestStepOrder] = fromInput(ad => IngestStepOrder(
    field = ad("field").asInstanceOf[IngestStepOrderField.Value],
    direction = ad("direction").asInstanceOf[OrderDirection.Value],
  ))
  val optIngestStepOrderArgument: Argument[Option[IngestStepOrder]] = {
    Argument(
      name = "orderBy",
      argumentType = OptionInputType(IngestStepOrderInputType),
      description = Some("Ordering options for the returned ingest steps."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(IngestStepOrderFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }
}
