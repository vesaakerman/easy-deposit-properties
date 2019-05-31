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

import nl.knaw.dans.easy.properties.app.model.SeriesFilter.SeriesFilter
import nl.knaw.dans.easy.properties.app.model.curator.Curator
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStep
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStepLabel.IngestStepLabel
import nl.knaw.dans.easy.properties.app.model.state.State
import nl.knaw.dans.easy.properties.app.model.state.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, SeriesFilter, Timestamp, timestampOrdering }
import sangria.macros.derive._
import sangria.marshalling.FromInput._
import sangria.marshalling.ToInput.ScalarToInput
import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput, ResultMarshaller, ToInput }
import sangria.schema.{ Argument, EnumType, InputObjectType, OptionInputType }

trait MetaTypes {

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
  }
  implicit val OrderDirectionType: EnumType[OrderDirection.Value] = deriveEnumType()

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

  case class DepositOrder(field: DepositOrderField.DepositOrderField,
                          direction: OrderDirection.OrderDirection) {

    lazy val ordering: Ordering[Deposit] = {
      val orderByField: Ordering[Deposit] = field match {
        case DepositOrderField.DEPOSIT_ID =>
          Ordering[DepositId].on(_.id)
        case DepositOrderField.CREATION_TIMESTAMP =>
          Ordering[Timestamp].on(_.creationTimestamp)
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

  @GraphQLDescription("Properties by which states can be ordered")
  object StateOrderField extends Enumeration {
    type StateOrderField = Value

    // @formatter:off
    @GraphQLDescription("Order states by label")
    val LABEL    : StateOrderField = Value("LABEL")
    @GraphQLDescription("Order states by timestamp")
    val TIMESTAMP: StateOrderField = Value("TIMESTAMP")
    // @formatter:on
  }
  implicit val StateOrderFieldType: EnumType[StateOrderField.Value] = deriveEnumType()

  case class StateOrder(field: StateOrderField.StateOrderField,
                        direction: OrderDirection.OrderDirection) {
    lazy val ordering: Ordering[State] = {
      val orderByField: Ordering[State] = field match {
        case StateOrderField.LABEL =>
          Ordering[StateLabel].on(_.label)
        case StateOrderField.TIMESTAMP =>
          Ordering[Timestamp].on(_.timestamp)
      }

      direction match {
        case OrderDirection.ASC => orderByField
        case OrderDirection.DESC => orderByField.reverse
      }
    }
  }
  implicit val StateOrderInputType: InputObjectType[StateOrder] = deriveInputObjectType(
    InputObjectTypeDescription("Ordering options for states"),
    DocumentInputField("field", "The field to order state by"),
    DocumentInputField("direction", "The ordering direction"),
  )
  implicit val StateOrderFromInput: FromInput[StateOrder] = new FromInput[StateOrder] {
    override val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): StateOrder = {
      val ad = node.asInstanceOf[Map[String, Any]]

      StateOrder(
        field = ad("field").asInstanceOf[StateOrderField.Value],
        direction = ad("direction").asInstanceOf[OrderDirection.Value],
      )
    }
  }
  val optStateOrderArgument: Argument[Option[StateOrder]] = {
    Argument(
      name = "orderBy",
      argumentType = OptionInputType(StateOrderInputType),
      description = Some("Ordering options for the returned states."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(StateOrderFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }

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
                             direction: OrderDirection.OrderDirection) {
    lazy val ordering: Ordering[IngestStep] = {
      val orderByField: Ordering[IngestStep] = field match {
        case IngestStepOrderField.STEP =>
          Ordering[IngestStepLabel].on(_.step)
        case IngestStepOrderField.TIMESTAMP =>
          Ordering[Timestamp].on(_.timestamp)
      }

      direction match {
        case OrderDirection.ASC => orderByField
        case OrderDirection.DESC => orderByField.reverse
      }
    }
  }
  implicit val IngestStepOrderInputType: InputObjectType[IngestStepOrder] = deriveInputObjectType(
    InputObjectTypeDescription("Ordering options for ingest steps"),
    DocumentInputField("field", "The field to order ingest steps by"),
    DocumentInputField("direction", "The ordering direction"),
  )
  implicit val IngestStepOrderFromInput: FromInput[IngestStepOrder] = new FromInput[IngestStepOrder] {
    override val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): IngestStepOrder = {
      val ad = node.asInstanceOf[Map[String, Any]]

      IngestStepOrder(
        field = ad("field").asInstanceOf[IngestStepOrderField.Value],
        direction = ad("direction").asInstanceOf[OrderDirection.Value],
      )
    }
  }
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

  @GraphQLDescription("Properties by which curators can be ordered")
  object CuratorOrderField extends Enumeration {
    type CuratorOrderField = Value

    // @formatter:off
    @GraphQLDescription("Order curators by step")
    val USERID   : CuratorOrderField = Value("USERID")
    @GraphQLDescription("Order curators by timestamp")
    val TIMESTAMP: CuratorOrderField = Value("TIMESTAMP")
    // @formatter:on
  }
  implicit val CuratorOrderFieldType: EnumType[CuratorOrderField.Value] = deriveEnumType()

  case class CuratorOrder(field: CuratorOrderField.CuratorOrderField,
                          direction: OrderDirection.OrderDirection) {
    lazy val ordering: Ordering[Curator] = {
      val orderByField: Ordering[Curator] = field match {
        case CuratorOrderField.USERID =>
          Ordering[String].on(_.userId)
        case CuratorOrderField.TIMESTAMP =>
          Ordering[Timestamp].on(_.timestamp)
      }

      direction match {
        case OrderDirection.ASC => orderByField
        case OrderDirection.DESC => orderByField.reverse
      }
    }
  }
  implicit val CuratorOrderInputType: InputObjectType[CuratorOrder] = deriveInputObjectType(
    InputObjectTypeDescription("Ordering options for curators"),
    DocumentInputField("field", "The field to order curators by"),
    DocumentInputField("direction", "The ordering direction"),
  )
  implicit val CuratorOrderFromInput: FromInput[CuratorOrder] = new FromInput[CuratorOrder] {
    override val marshaller: ResultMarshaller = CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): CuratorOrder = {
      val ad = node.asInstanceOf[Map[String, Any]]

      CuratorOrder(
        field = ad("field").asInstanceOf[CuratorOrderField.Value],
        direction = ad("direction").asInstanceOf[OrderDirection.Value],
      )
    }
  }
  val optCuratorOrderArgument: Argument[Option[CuratorOrder]] = {
    Argument(
      name = "orderBy",
      argumentType = OptionInputType(CuratorOrderInputType),
      description = Some("Ordering options for the returned curators."),
      defaultValue = None,
      fromInput = optionInput(inputObjectResultInput(CuratorOrderFromInput)),
      astDirectives = Vector.empty,
      astNodes = Vector.empty,
    )
  }
}
