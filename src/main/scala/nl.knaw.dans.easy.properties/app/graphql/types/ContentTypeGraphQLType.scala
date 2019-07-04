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
import nl.knaw.dans.easy.properties.app.graphql.resolvers.DepositResolver
import nl.knaw.dans.easy.properties.app.model.SeriesFilter.SeriesFilter
import nl.knaw.dans.easy.properties.app.model.contentType.ContentTypeValue.ContentTypeValue
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, ContentTypeValue, DepositContentTypeFilter }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, SeriesFilter, Timestamp, timestampOrdering }
import nl.knaw.dans.easy.properties.app.repository.DepositFilters
import sangria.macros.derive._
import sangria.marshalling.FromInput
import sangria.marshalling.FromInput._
import sangria.relay._
import sangria.schema.{ Argument, Context, DeferredValue, EnumType, Field, InputObjectType, ObjectType, OptionInputType, OptionType, StringType }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

trait ContentTypeGraphQLType {
  this: DepositType
    with TimebasedSearch
    with NodeType
    with MetaTypes
    with Scalars =>

  implicit val ContentTypeValueType: EnumType[ContentTypeValue.Value] = deriveEnumType(
    EnumTypeDescription("A SWORD2 internal property to record the type of messages sent by a client to create the deposit."),
    DocumentValue("ZIP", "content type 'application/zip'"),
    DocumentValue("OCTET", "content type 'application/octet-stream'"),
  )

  implicit val DepositContentTypeFilterType: InputObjectType[DepositContentTypeFilter] = deriveInputObjectType(
    InputObjectTypeDescription("The label and filter to be used in searching for deposits by content type."),
    DocumentInputField("value", "If provided, only show deposits with this content type."),
    DocumentInputField("filter", "Determine whether to search in current content types (`LATEST`, default) or all current and past content types (`ALL`)."),
  )
  implicit val DepositContentTypeFilterFromInput: FromInput[DepositContentTypeFilter] = fromInput(ad => DepositContentTypeFilter(
    value = ad("value").asInstanceOf[ContentTypeValue],
    filter = ad("filter").asInstanceOf[Option[SeriesFilter]].getOrElse(SeriesFilter.LATEST),
  ))

  private val seriesFilterArgument: Argument[SeriesFilter] = Argument(
    name = "contentTypeFilter",
    argumentType = SeriesFilterType,
    description = Some("Determine whether to search in current content types (`LATEST`, default) or all current and past content types (`ALL`)."),
    defaultValue = Some(SeriesFilter.LATEST -> SeriesFilterToInput),
    fromInput = coercedScalaInput,
    astDirectives = Vector.empty,
    astNodes = Vector.empty,
  )
  val depositContentTypeFilterArgument: Argument[Option[DepositContentTypeFilter]] = {
    Argument(
      name = "contentType",
      argumentType = OptionInputType(DepositContentTypeFilterType),
      description = Some("List only those deposits that have this specified content type."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(DepositContentTypeFilterFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }

  private val depositField: Field[DataContext, ContentType] = Field(
    name = "deposit",
    description = Some("Returns the deposit that is associated with this particular content type."),
    fieldType = OptionType(DepositType),
    resolve = getDepositByContentType,
  )
  private val depositsField: Field[DataContext, ContentType] = Field(
    name = "deposits",
    description = Some("List all deposits with the same current content type."),
    arguments = List(
      seriesFilterArgument,
      optDepositOrderArgument,
    ) ::: timebasedSearchArguments ::: Connection.Args.All,
    fieldType = OptionType(depositConnectionType),
    resolve = ctx => getDeposits(ctx).map(ExtendedConnection.connectionFromSeq(_, ConnectionArgs(ctx))),
  )

  private def getDepositByContentType(context: Context[DataContext, ContentType]): Try[Option[Deposit]] = {
    context.ctx.repo.contentType
      .getDepositById(context.value.id)
      .toTry
  }

  private def getDeposits(implicit context: Context[DataContext, ContentType]): DeferredValue[DataContext, Seq[Deposit]] = {
    val contentType = context.value.value
    val contentTypeFilter = context.arg(seriesFilterArgument)

    DepositResolver.findDeposit(DepositFilters(
      contentTypeFilter = Some(DepositContentTypeFilter(contentType, contentTypeFilter))
    )).map(timebasedFilterAndSort(optDepositOrderArgument))
  }

  implicit val ContentTypeType: ObjectType[DataContext, ContentType] = deriveObjectType(
    ObjectTypeDescription("A SWORD2 internal property to record the type of messages sent by a client to create the deposit."),
    Interfaces[DataContext, ContentType](nodeInterface),
    DocumentField("timestamp", "The timestamp at which this springfield configuration was associated with the deposit."),
    AddFields(
      depositField,
      depositsField,
    ),
    ReplaceField("id", Node.globalIdField[DataContext, ContentType]),
    ReplaceField("value", Field(
      name = "value",
      description = Some("The content type associated with this deposit."),
      fieldType = OptionType(StringType),
      resolve = ctx => ctx.value.value.toString,
    )),
  )

  val ConnectionDefinition(_, contentTypeConnectionType) = ExtendedConnection.definition[DataContext, ExtendedConnection, ContentType](
    name = "ContentType",
    nodeType = ContentTypeType,
  )

  @GraphQLDescription("Properties by which content types can be ordered")
  object ContentTypeOrderField extends Enumeration {
    type ContentTypeOrderField = Value

    // @formatter:off
    @GraphQLDescription("Order content types by value")
    val VALUE:     ContentTypeOrderField = Value("VALUE")
    @GraphQLDescription("Order content types by timestamp")
    val TIMESTAMP: ContentTypeOrderField = Value("TIMESTAMP")
    // @formatter:on
  }
  implicit val ContentTypeOrderFieldType: EnumType[ContentTypeOrderField.Value] = deriveEnumType()

  case class ContentTypeOrder(field: ContentTypeOrderField.ContentTypeOrderField,
                              direction: OrderDirection.OrderDirection) extends Ordering[ContentType] {
    def compare(x: ContentType, y: ContentType): Int = {
      val orderByField: Ordering[ContentType] = field match {
        case ContentTypeOrderField.VALUE =>
          Ordering[ContentTypeValue].on(_.value)
        case ContentTypeOrderField.TIMESTAMP =>
          Ordering[Timestamp].on(_.timestamp)
      }

      direction.withOrder(orderByField).compare(x, y)
    }
  }
  implicit val ContentTypeOrderInputType: InputObjectType[ContentTypeOrder] = deriveInputObjectType(
    InputObjectTypeDescription("Ordering options for content types"),
    DocumentInputField("field", "The field to order content types by"),
    DocumentInputField("direction", "The ordering direction"),
  )
  implicit val ContentTypeOrderFromInput: FromInput[ContentTypeOrder] = fromInput(ad => ContentTypeOrder(
    field = ad("field").asInstanceOf[ContentTypeOrderField.Value],
    direction = ad("direction").asInstanceOf[OrderDirection.Value],
  ))
  val optContentTypeOrderArgument: Argument[Option[ContentTypeOrder]] = {
    Argument(
      name = "orderBy",
      argumentType = OptionInputType(ContentTypeOrderInputType),
      description = Some("Ordering options for the returned content types."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(ContentTypeOrderFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }
}
