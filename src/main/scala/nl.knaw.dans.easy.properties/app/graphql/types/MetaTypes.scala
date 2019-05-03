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

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import org.joda.time.DateTime
import sangria.macros.derive._
import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput, ResultMarshaller }
import sangria.schema.{ EnumType, InputObjectType }

trait MetaTypes {

  @GraphQLDescription("Properties by which deposits can be ordered")
  object DepositOrderField extends Enumeration {
    type DepositOrderField = Value

    // @formatter:off
    @GraphQLDescription("Order deposits by depositId")
    val DEPOSIT_ID        : DepositOrderField = Value("DEPOSIT_ID")
    @GraphQLDescription("Order deposits by creation timestamp")
    val CREATION_TIMESTAMP: DepositOrderField = Value("CREATION_TIMESTAMP")
    // @formatter:on
  }
  implicit val DepositOrderFieldType: EnumType[DepositOrderField.Value] = deriveEnumType()

  @GraphQLDescription("Possible directions in which to order a list of items when provided an orderBy argument")
  object OrderDirection extends Enumeration {
    type OrderDirection = Value

    // @formatter:off
    @GraphQLDescription("Specifies an ascending order for a given orderBy argumen.")
    val ASC : OrderDirection = Value("ASC")
    @GraphQLDescription("Specifies a descending order for a given orderBy argument")
    val DESC: OrderDirection = Value("DESC")
    // @formatter:on
  }
  implicit val OrderDirectionType: EnumType[OrderDirection.Value] = deriveEnumType()

  case class DepositOrder(field: DepositOrderField.DepositOrderField,
                          direction: OrderDirection.OrderDirection) {

    lazy val ordering: Ordering[Deposit] = {
      val orderByField: Ordering[Deposit] = field match {
        case DepositOrderField.DEPOSIT_ID =>
          Ordering[DepositId].on(_.id)
        case DepositOrderField.CREATION_TIMESTAMP =>
          Ordering.fromLessThan[DateTime](_ isBefore _).on(_.creationTimestamp)
      }

      direction match {
        case OrderDirection.ASC => orderByField
        case OrderDirection.DESC => orderByField.reverse
      }
    }
  }
  implicit val DepositOrderInputType: InputObjectType[DepositOrder] = deriveInputObjectType(
    InputObjectTypeDescription("Ordering options for deposits"),
    DocumentInputField("field", "The field to order deposit by"),
    DocumentInputField("direction", "The ordering direction"),
  )
  implicit val DepositOrderFromInput: FromInput[DepositOrder] = new FromInput[DepositOrder] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): DepositOrder = {
      val ad = node.asInstanceOf[Map[String, Any]]

      DepositOrder(
        field = ad("field").asInstanceOf[DepositOrderField.Value],
        direction = ad("direction").asInstanceOf[OrderDirection.Value],
      )
    }
  }
}
