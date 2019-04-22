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
package nl.knaw.dans.easy.properties.app.graphql

import java.util.UUID

import nl.knaw.dans.easy.properties.app.model.State.StateLabel
import nl.knaw.dans.easy.properties.app.model.State.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model._
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }
import sangria.ast.StringValue
import sangria.macros.derive._
import sangria.schema._
import sangria.validation.{ StringCoercionViolation, ValueCoercionViolation, Violation }

import scala.util.Try

object GraphqlTypes {

  case object UUIDCoercionViolation extends ValueCoercionViolation("UUID value expected")
  case object DateCoercionViolation extends ValueCoercionViolation("Date value expected")
  
  trait Query {
    val repository: DepositRepository
    
    @GraphQLField
    @GraphQLDescription("List all registered deposits.")
    def deposits: Seq[Deposit] = {
      repository.getAllDeposits
    }

    @GraphQLField
    @GraphQLDescription("Get the technical metadata of the deposit identified by 'id'.")
    def deposit(id: UUID): Option[Deposit] = {
      repository.getDeposit(id)
    }
  }
  
  trait Mutation {
    val repository: DepositRepository
    
    @GraphQLField
    @GraphQLDescription("Register a new deposit with 'id', 'creationTimestamp' and 'depositId'.")
    def registerDeposit(id: UUID, creationTimestamp: DateTime, depositorId: String): Option[Deposit] = {
      repository.registerDeposit(Deposit(id, creationTimestamp, depositorId))
    }

    @GraphQLField
    @GraphQLDescription("Update the state of the deposit identified by 'id'.")
    def updateState(id: UUID, label: StateLabel, description: String): Option[Deposit] = {
      repository.setState(id, State(label, description))
    }
  }
  
  type DataContext = Query with Mutation

  def DataContext(repo: DepositRepository): DataContext = new Query with Mutation {
    override val repository: DepositRepository = repo
  }

  private def parseUUID(s: String): Either[Violation, UUID] = {
    Try { UUID.fromString(s) }.fold(_ => Left(UUIDCoercionViolation), Right(_))
  }

  implicit private val UUIDType: ScalarType[UUID] = ScalarType("UUID",
    description = Some("The UUID scalar type represents textual data, " +
      "formatted as a universally unique identifier."),
    coerceOutput = (value, _) => value.toString,
    coerceUserInput = {
      case s: String => parseUUID(s)
      case _ => Left(StringCoercionViolation)
    },
    coerceInput = {
      case StringValue(s, _, _, _, _) => parseUUID(s)
      case _ => Left(StringCoercionViolation)
    }
  )

  private def parseDate(s: String): Either[Violation, DateTime] = {
    Try { new DateTime(s, DateTimeZone.UTC) }.fold(_ => Left(DateCoercionViolation), Right(_))
  }

  implicit private val DateTimeType: ScalarType[DateTime] = ScalarType("DateTime",
    description = Some("A DateTime scalar type represents textual data, " +
      "formatted as an ISO8601 date-time."),
    coerceOutput = (value, _) => ISODateTimeFormat.dateTime() print value,
    coerceUserInput = {
      case s: String => parseDate(s)
      case _ => Left(DateCoercionViolation)
    },
    coerceInput = {
      case StringValue(s, _, _, _, _) => parseDate(s)
      case _ => Left(DateCoercionViolation)
    }
  )

  implicit private val StateLabelType: EnumType[StateLabel.Value] = deriveEnumType()
  implicit private val StateType: ObjectType[Unit, State] = deriveObjectType(
    ObjectTypeDescription("The state of the deposit."),
    DocumentField("label", "The state label of the deposit."),
    DocumentField("description", "Additional information about the state.")
  )
  private val stateField: Field[DataContext, Deposit] = Field(
    name = "state",
    fieldType = OptionType(StateType),
    description = Option("The state of the deposit."),
    resolve = c => c.ctx.repository.getState(c.value.id),
  )

  implicit private val DepositType: ObjectType[DataContext, Deposit] = deriveObjectType(
    DocumentField("id", "The identifier of the deposit."),
    DocumentField("creationTimestamp", "The moment this deposit was created."),
    DocumentField("depositorId", "The EASY account of the depositor."),
    AddFields(stateField),
  )
  implicit private val QueryType: ObjectType[DataContext, Unit] = deriveContextObjectType[DataContext, Query, Unit](identity)
  implicit private val MutationType: ObjectType[DataContext, Unit] = deriveContextObjectType[DataContext, Mutation, Unit](identity)
  val DepositSchema: Schema[DataContext, Unit] = Schema[DataContext, Unit](QueryType, mutation = Option(MutationType))
}
