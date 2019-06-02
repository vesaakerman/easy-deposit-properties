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
import nl.knaw.dans.easy.properties.app.model.springfield.SpringfieldPlayMode.SpringfieldPlayMode
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield, SpringfieldPlayMode }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, Timestamp, timestampOrdering }
import sangria.macros.derive._
import sangria.marshalling.FromInput._
import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput, ResultMarshaller }
import sangria.relay._
import sangria.schema.{ Argument, Context, EnumType, Field, InputObjectType, ObjectType, OptionInputType, OptionType }

trait SpringfieldType {
  this: DepositType
    with NodeType
    with MetaTypes
    with Scalars =>

  implicit val SpringfieldPlayModeType: EnumType[SpringfieldPlayMode.Value] = deriveEnumType(
    EnumTypeDescription("The playmode in Springfield for this deposit."),
    DocumentValue("CONTINUOUS", "Play audio/video continuously in Springfield."),
    DocumentValue("MENU", "Play audio/video in Springfield as selected in a menu."),
  )

  val fetchCurrentSpringfields: CurrentFetcher[Springfield] = fetchCurrent(_.deposits.getCurrentSpringfield, _.deposits.getCurrentSpringfields)
  val fetchAllSpringfields: AllFetcher[Springfield] = fetchAll(_.deposits.getAllSpringfields, _.deposits.getAllSpringfields)

  private val depositField: Field[DataContext, Springfield] = Field(
    name = "deposit",
    description = Some("Returns the deposit that is associated with this particular springfield configuration."),
    fieldType = OptionType(DepositType),
    resolve = getDepositBySpringfield,
  )

  private def getDepositBySpringfield(context: Context[DataContext, Springfield]): Option[Deposit] = {
    val repository = context.ctx.deposits

    val springfieldId = context.value.id

    repository.getDepositBySpringfieldId(springfieldId)
  }

  implicit val SpringfieldType: ObjectType[DataContext, Springfield] = deriveObjectType(
    ObjectTypeDescription("Springfield configuration associated with this deposit."),
    Interfaces[DataContext, Springfield](nodeInterface),
    DocumentField("domain", "The domain of Springfield"),
    DocumentField("user", "The user of Springfield"),
    DocumentField("collection", "The collection of Springfield"),
    DocumentField("playmode", "The playmode used in Springfield"),
    DocumentField("timestamp", "The timestamp at which this springfield configuration was associated with the deposit."),
    AddFields(
      depositField,
    ),
    ReplaceField("id", Node.globalIdField[DataContext, Springfield]),
  )

  implicit val InputSpringfieldType: InputObjectType[InputSpringfield] = deriveInputObjectType(
    InputObjectTypeDescription("Springfield configuration associated with this deposit."),
    DocumentInputField("domain", "The domain of Springfield"),
    DocumentInputField("user", "The user of Springfield"),
    DocumentInputField("collection", "The collection of Springfield"),
    DocumentInputField("playmode", "The playmode used in Springfield"),
    DocumentInputField("timestamp", "The timestamp at which this springfield configuration was associated with the deposit."),
  )
  implicit val inputSpringfieldFromInput: FromInput[InputSpringfield] = new FromInput[InputSpringfield] {
    val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    def fromResult(node: marshaller.Node): InputSpringfield = {
      val ad = node.asInstanceOf[Map[String, Any]]

      InputSpringfield(
        domain = ad("domain").asInstanceOf[String],
        user = ad("user").asInstanceOf[String],
        collection = ad("collection").asInstanceOf[String],
        playmode = ad("playmode").asInstanceOf[SpringfieldPlayMode],
        timestamp = ad("timestamp").asInstanceOf[Timestamp],
      )
    }
  }

  @GraphQLDescription("Properties by which springfields can be ordered")
  object SpringfieldOrderField extends Enumeration {
    type SpringfieldOrderField = Value

    // @formatter:off
    @GraphQLDescription("Order springfields by timestamp")
    val TIMESTAMP: SpringfieldOrderField = Value("TIMESTAMP")
    // @formatter:on
  }
  implicit val SpringfieldOrderFieldType: EnumType[SpringfieldOrderField.Value] = deriveEnumType()

  case class SpringfieldOrder(field: SpringfieldOrderField.SpringfieldOrderField,
                              direction: OrderDirection.OrderDirection) {
    lazy val ordering: Ordering[Springfield] = {
      val orderByField: Ordering[Springfield] = field match {
        case SpringfieldOrderField.TIMESTAMP =>
          Ordering[Timestamp].on(_.timestamp)
      }

      direction match {
        case OrderDirection.ASC => orderByField
        case OrderDirection.DESC => orderByField.reverse
      }
    }
  }
  implicit val SpringfieldOrderInputType: InputObjectType[SpringfieldOrder] = deriveInputObjectType(
    InputObjectTypeDescription("Ordering options for springfields"),
    DocumentInputField("field", "The field to order springfields by"),
    DocumentInputField("direction", "The ordering direction"),
  )
  implicit val SpringfieldOrderFromInput: FromInput[SpringfieldOrder] = new FromInput[SpringfieldOrder] {
    override val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): SpringfieldOrder = {
      val ad = node.asInstanceOf[Map[String, Any]]

      SpringfieldOrder(
        field = ad("field").asInstanceOf[SpringfieldOrderField.Value],
        direction = ad("direction").asInstanceOf[OrderDirection.Value],
      )
    }
  }
  val optSpringfieldOrderArgument: Argument[Option[SpringfieldOrder]] = {
    Argument(
      name = "orderBy",
      argumentType = OptionInputType(SpringfieldOrderInputType),
      description = Some("Ordering options for the returned springfields."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(SpringfieldOrderFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }
}
