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
import nl.knaw.dans.easy.properties.app.repository._
import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.SeriesFilter.SeriesFilter
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, SeriesFilter, Timestamp, timestampOrdering }
import nl.knaw.dans.easy.properties.app.repository.QueryErrorOr
import sangria.execution.deferred.{ Fetcher, HasId }
import sangria.macros.derive._
import sangria.marshalling.FromInput._
import sangria.marshalling.ToInput.ScalarToInput
import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput, ResultMarshaller, ToInput }
import sangria.relay.{ Identifiable, Node }
import sangria.schema.{ Argument, EnumType, InputObjectType, OptionInputType }

import scala.language.implicitConversions

trait MetaTypes {

  implicit def depositIdTupleHasId[T]: HasId[(DepositId, T), DepositId] = HasId { case (id, _) => id }

  implicit def depositIdCompositeKeyTupleHasId[K, T]: HasId[((DepositId, K), T), (DepositId, K)] = HasId { case (id, _) => id }

  implicit def nodeIdentifiable[T <: Node]: Identifiable[T] = _.id

  type CurrentFetcher[T] = Fetcher[DataContext, (DepositId, Option[T]), (DepositId, Option[T]), DepositId]

  def fetchCurrent[T](currentOne: DataContext => DepositId => QueryErrorOr[Option[T]],
                      currentMany: DataContext => Seq[DepositId] => QueryErrorOr[Seq[(DepositId, Option[T])]]): CurrentFetcher[T] = {
    Fetcher((ctx: DataContext, ids: Seq[DepositId]) => {
      ids match {
        case Seq() => Seq.empty.asRight[QueryError].toFuture
        case Seq(id) => currentOne(ctx)(id).map(optT => Seq(id -> optT)).toFuture
        case _ => currentMany(ctx)(ids).toFuture
      }
    })
  }

  type AllFetcher[T] = Fetcher[DataContext, (DepositId, Seq[T]), (DepositId, Seq[T]), DepositId]

  def fetchAll[T](currentOne: DataContext => DepositId => QueryErrorOr[Seq[T]],
                  currentMany: DataContext => Seq[DepositId] => QueryErrorOr[Seq[(DepositId, Seq[T])]]): AllFetcher[T] = {
    Fetcher((ctx: DataContext, ids: Seq[DepositId]) => {
      ids match {
        case Seq() => Seq.empty.asRight[QueryError].toFuture
        case Seq(id) => currentOne(ctx)(id).map(seqT => Seq(id -> seqT)).toFuture
        case _ => currentMany(ctx)(ids).toFuture
      }
    })
  }

  implicit def fromInput[T](create: Map[String, Any] => T): FromInput[T] = new FromInput[T] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): T = {
      val ad = node.asInstanceOf[Map[String, Any]]

      create(ad)
    }
  }

  implicit val SeriesFilterType: EnumType[SeriesFilter.Value] = deriveEnumType(
    EnumTypeDescription("Mark a query to only search through current states, or also to include past states."),
    DocumentValue("LATEST", "Only search through current states."),
    DocumentValue("ALL", "Search through both current and past states."),
  )
  implicit val SeriesFilterToInput: ToInput[SeriesFilter, _] = new ScalarToInput

  @GraphQLDescription("Possible directions in which to order a list of items when provided an orderBy argument")
  object OrderDirection extends Enumeration {
    type OrderDirection = Value

    // @formatter:off
    @GraphQLDescription("Specifies an ascending order for a given orderBy argumen.")
    val ASC : OrderDirection = Value("ASC")
    @GraphQLDescription("Specifies a descending order for a given orderBy argument")
    val DESC: OrderDirection = Value("DESC")
    // @formatter:on

    case class OrderDirectionValue(value: OrderDirection) {
      def withOrder[T](ordering: Ordering[T]): Ordering[T] = {
        value match {
          case ASC => ordering
          case DESC => ordering.reverse
        }
      }
    }
    implicit def value2OrderDirectionValue(value: OrderDirection): OrderDirectionValue = OrderDirectionValue(value)
  }
  implicit val OrderDirectionType: EnumType[OrderDirection.Value] = deriveEnumType()

  @GraphQLDescription("Properties by which deposits can be ordered")
  object DepositOrderField extends Enumeration {
    type DepositOrderField = Value

    // @formatter:off
    @GraphQLDescription("Order deposits by depositId")
    val DEPOSIT_ID        : DepositOrderField = Value("DEPOSIT_ID")
    @GraphQLDescription("Order deposits by bag name")
    val BAG_NAME          : DepositOrderField = Value("BAG_NAME")
    @GraphQLDescription("Order deposits by creation timestamp")
    val CREATION_TIMESTAMP: DepositOrderField = Value("CREATION_TIMESTAMP")
    // @formatter:on
  }
  implicit val DepositOrderFieldType: EnumType[DepositOrderField.Value] = deriveEnumType()

  case class DepositOrder(field: DepositOrderField.DepositOrderField,
                          direction: OrderDirection.OrderDirection) extends Ordering[Deposit] {
    def compare(x: Deposit, y: Deposit): Int = {
      val orderByField: Ordering[Deposit] = field match {
        case DepositOrderField.DEPOSIT_ID =>
          Ordering[DepositId].on(_.id)
        case DepositOrderField.BAG_NAME =>
          Ordering[Option[String]].on(_.bagName)
        case DepositOrderField.CREATION_TIMESTAMP =>
          Ordering[Timestamp].on(_.creationTimestamp)
      }

      direction.withOrder(orderByField).compare(x, y)
    }
  }
  implicit val DepositOrderInputType: InputObjectType[DepositOrder] = deriveInputObjectType(
    InputObjectTypeDescription("Ordering options for deposits"),
    DocumentInputField("field", "The field to order deposit by"),
    DocumentInputField("direction", "The ordering direction"),
  )
  implicit val DepositOrderFromInput: FromInput[DepositOrder] = new FromInput[DepositOrder] {
    override val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): DepositOrder = {
      val ad = node.asInstanceOf[Map[String, Any]]

      DepositOrder(
        field = ad("field").asInstanceOf[DepositOrderField.Value],
        direction = ad("direction").asInstanceOf[OrderDirection.Value],
      )
    }
  }
  val optDepositOrderArgument: Argument[Option[DepositOrder]] = {
    Argument(
      name = "orderBy",
      argumentType = OptionInputType(DepositOrderInputType),
      description = Some("Ordering options for the returned deposits."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(DepositOrderFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }
}
