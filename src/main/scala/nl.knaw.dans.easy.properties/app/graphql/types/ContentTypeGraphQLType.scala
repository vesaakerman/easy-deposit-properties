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
import nl.knaw.dans.easy.properties.app.model.SeriesFilter.SeriesFilter
import nl.knaw.dans.easy.properties.app.model.contentType.ContentTypeValue.ContentTypeValue
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, ContentTypeValue, DepositContentTypeFilter, InputContentType }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, SeriesFilter, Timestamp, timestampOrdering }
import sangria.macros.derive._
import sangria.marshalling.FromInput._
import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput, ResultMarshaller }
import sangria.relay._
import sangria.schema.{ Argument, Context, EnumType, Field, InputObjectType, ObjectType, OptionInputType, OptionType, StringType }

trait ContentTypeGraphQLType {
  this: DepositType
    with NodeType
    with MetaTypes
    with Scalars =>

  implicit val ContentTypeValueType: EnumType[ContentTypeValue.Value] = deriveEnumType(
    EnumTypeDescription("A SWORD2 internal property to record the type of messages sent by a client to create the deposit."),
    DocumentValue("ZIP", "content type 'application/zip'"),
    DocumentValue("OCTET", "content type 'application/octet-stream'"),
  )

  val fetchCurrentContentTypes: CurrentFetcher[ContentType] = fetchCurrent(_.deposits.getCurrentContentType, _.deposits.getCurrentContentTypes)
  val fetchAllContentTypes: AllFetcher[ContentType] = fetchAll(_.deposits.getAllContentTypes, _.deposits.getAllContentTypes)

  implicit val DepositContentTypeFilterType: InputObjectType[DepositContentTypeFilter] = deriveInputObjectType(
    InputObjectTypeDescription("The label and filter to be used in searching for deposits by content type."),
    DocumentInputField("value", "If provided, only show deposits with this content type."),
    DocumentInputField("filter", "Determine whether to search in current content types (`LATEST`, default) or all current and past content types (`ALL`)."),
  )
  implicit val DepositContentTypeFilterFromInput: FromInput[DepositContentTypeFilter] = new FromInput[DepositContentTypeFilter] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): DepositContentTypeFilter = {
      val ad = node.asInstanceOf[Map[String, Any]]

      DepositContentTypeFilter(
        value = ad("value").asInstanceOf[ContentTypeValue],
        filter = ad("filter").asInstanceOf[Option[SeriesFilter]].getOrElse(SeriesFilter.LATEST),
      )
    }
  }

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
    ) ++ Connection.Args.All,
    fieldType = OptionType(depositConnectionType),
    resolve = ctx => ExtendedConnection.connectionFromSeq(getDeposits(ctx), ConnectionArgs(ctx)),
  )

  private def getDepositByContentType(context: Context[DataContext, ContentType]): Option[Deposit] = {
    val repository = context.ctx.deposits

    val contentTypeId = context.value.id

    repository.getDepositByContentTypeId(contentTypeId)
  }

  private def getDeposits(context: Context[DataContext, ContentType]): Seq[Deposit] = {
    val repository = context.ctx.deposits

    val contentType = context.value.value
    val contentTypeFilter = context.arg(seriesFilterArgument)
    val orderBy = context.arg(optDepositOrderArgument)

    val result = repository.getDeposits(
      contentTypeFilter = Some(DepositContentTypeFilter(contentType, contentTypeFilter))
    )

    orderBy.fold(result)(order => result.sorted(order.ordering))
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

  implicit val InputContentTypeType: InputObjectType[InputContentType] = deriveInputObjectType(
    InputObjectTypeDescription("A SWORD2 internal property to record the type of messages sent by a client to create the deposit."),
    DocumentInputField("value", "The content type associated with this deposit."),
    DocumentInputField("timestamp", "The timestamp at which this springfield configuration was associated with the deposit."),
  )
  implicit val InputContentTypeFromInput: FromInput[InputContentType] = new FromInput[InputContentType] {
    override val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): InputContentType = {
      val ad = node.asInstanceOf[Map[String, Any]]

      InputContentType(
        value = ad("value").asInstanceOf[ContentTypeValue],
        timestamp = ad("timestamp").asInstanceOf[Timestamp],
      )
    }
  }

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
                              direction: OrderDirection.OrderDirection) {
    lazy val ordering: Ordering[ContentType] = {
      val orderByField: Ordering[ContentType] = field match {
        case ContentTypeOrderField.VALUE =>
          Ordering[ContentTypeValue].on(_.value)
        case ContentTypeOrderField.TIMESTAMP =>
          Ordering[Timestamp].on(_.timestamp)
      }

      direction match {
        case OrderDirection.ASC => orderByField
        case OrderDirection.DESC => orderByField.reverse
      }
    }
  }
  implicit val ContentTypeOrderInputType: InputObjectType[ContentTypeOrder] = deriveInputObjectType(
    InputObjectTypeDescription("Ordering options for content types"),
    DocumentInputField("field", "The field to order content types by"),
    DocumentInputField("direction", "The ordering direction"),
  )
  implicit val ContentTypeOrderFromInput: FromInput[ContentTypeOrder] = new FromInput[ContentTypeOrder] {
    override val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): ContentTypeOrder = {
      val ad = node.asInstanceOf[Map[String, Any]]

      ContentTypeOrder(
        field = ad("field").asInstanceOf[ContentTypeOrderField.Value],
        direction = ad("direction").asInstanceOf[OrderDirection.Value],
      )
    }
  }
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
