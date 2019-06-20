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
import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.repository.QueryError
import sangria.execution.deferred.Fetcher
import sangria.macros.derive._
import sangria.relay.Node
import sangria.schema.{ Context, EnumType, Field, ObjectType, OptionType }

import scala.util.Try

trait IdentifierGraphQLType {
  this: DepositType with NodeType with MetaTypes with Scalars =>

  implicit val IdentifierTypeType: EnumType[IdentifierType.Value] = deriveEnumType(
    EnumTypeDescription("The type of the identifier."),
    DocumentValue("DOI", "The doi identifier."),
    DocumentValue("URN", "The 'urn:nbn' identifier."),
    DocumentValue("FEDORA", "The Fedora identifier."),
    DocumentValue("BAG_STORE", "The bagstore identifier."),
  )

  val fetchIdentifiersByDepositId: AllFetcher[Identifier] = fetchAll(_.deposits.getIdentifiers, _.deposits.getIdentifiers)
  val fetchIdentifiersByType = Fetcher((ctx: DataContext, ids: Seq[(DepositId, IdentifierType.Value)]) => {
    ids match {
      case Seq() => Seq.empty.asRight[QueryError].toFuture
      case Seq((depositId, identifierType)) => ctx.deposits.getIdentifier(depositId, identifierType).map(optIdentifier => Seq((depositId, identifierType) -> optIdentifier)).toFuture
      case _ => ctx.deposits.getIdentifiersForTypes(ids).toFuture
    }
  })

  private val depositField: Field[DataContext, Identifier] = Field(
    name = "deposit",
    description = Some("Returns the deposit that is associated with this particular ingest step"),
    fieldType = OptionType(DepositType),
    resolve = getDepositByIdentifier,
  )

  private def getDepositByIdentifier(context: Context[DataContext, Identifier]): Try[Option[Deposit]] = {
    context.ctx.deposits
      .getDepositByIdentifierId(context.value.id)
      .toTry
  }

  implicit val IdentifierObjectType: ObjectType[DataContext, Identifier] = deriveObjectType(
    ObjectTypeDescription("An identifier related to a deposit."),
    Interfaces[DataContext, Identifier](nodeInterface),
    DocumentField("idType", "The type of identifier."),
    DocumentField("idValue", "The value of the identifier."),
    DocumentField("timestamp", "The timestamp at which the identifier got added to this deposit."),
    RenameField("idType", "type"),
    RenameField("idValue", "value"),
    AddFields(
      depositField,
    ),
    ReplaceField("id", Node.globalIdField[DataContext, Identifier]),
  )
}
