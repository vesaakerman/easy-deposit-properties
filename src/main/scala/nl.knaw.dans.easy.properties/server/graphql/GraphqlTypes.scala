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
package nl.knaw.dans.easy.properties.server.graphql

import java.util.UUID

import nl.knaw.dans.easy.properties.app.model.State.StateLabel
import nl.knaw.dans.easy.properties.app.model.State.StateLabel.StateLabel
import nl.knaw.dans.easy.properties.app.model._
import sangria.ast.StringValue
import sangria.macros.derive._
import sangria.schema._
import sangria.validation.{ StringCoercionViolation, ValueCoercionViolation, Violation }

import scala.util.Try

object GraphqlTypes {

  case object UUIDCoercionViolation extends ValueCoercionViolation("UUID value expected")
  
  trait Query {
    val repository: DemoRepository
    
    @GraphQLField
    def deposits: Seq[Deposit] = repository.getAllDeposits

    @GraphQLField
    def deposit(id: UUID): Option[Deposit] = repository.getDeposit(id)
  }
  
  trait Mutation {
    val repository: DemoRepository
    
    @GraphQLField
    def state(id: UUID, label: StateLabel, description: String): Option[Deposit] = repository.setState(id, State(label, description))
  }
  
  type SchemaType = Query with Mutation
  def SchemaType(repo: DemoRepository): SchemaType = new Query with Mutation {
    override val repository: DemoRepository = repo
  }

  private def parseUUID(s: String): Either[Violation, UUID] = {
    Try { UUID.fromString(s) }.fold(_ => Left(UUIDCoercionViolation), Right(_))
  }

  implicit val UUIDType: ScalarType[UUID] = ScalarType("String",
    description = Some("The `UUID` scalar type represents textual data, " +
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

  implicit val StateLabelType: EnumType[StateLabel.Value] = deriveEnumType()
  implicit val StateType: ObjectType[Unit, State] = deriveObjectType()
  implicit val DepositType: ObjectType[Unit, Deposit] = deriveObjectType()
  implicit val QueryType: ObjectType[SchemaType, Unit] = deriveContextObjectType[SchemaType, Query, Unit](identity)
  implicit val MutationType: ObjectType[SchemaType, Unit] = deriveContextObjectType[SchemaType, Mutation, Unit](identity)
  val DepositSchema: Schema[SchemaType, Unit] = Schema[SchemaType, Unit](QueryType, mutation = Option(MutationType))
}
