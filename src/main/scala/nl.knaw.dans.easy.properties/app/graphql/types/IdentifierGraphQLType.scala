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
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, Timestamp }
import sangria.execution.deferred.Fetcher
import sangria.macros.derive._
import sangria.marshalling.FromInput
import sangria.relay.Node
import sangria.schema.{ Context, EnumType, Field, InputObjectType, ObjectType, OptionType }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
  val fetchIdentifiersByType = Fetcher((ctx: DataContext, ids: Seq[(DepositId, IdentifierType.Value)]) => Future {
    ids match {
      case Seq() => Seq.empty
      case Seq((depositId, identifierType)) => Seq((depositId, identifierType) -> ctx.deposits.getIdentifier(depositId, identifierType))
      case _ => ctx.deposits.getIdentifiersForTypes(ids)
    }
  })

  private val depositField: Field[DataContext, Identifier] = Field(
    name = "deposit",
    description = Some("Returns the deposit that is associated with this particular ingest step"),
    fieldType = OptionType(DepositType),
    resolve = getDepositByIdentifier,
  )

  private def getDepositByIdentifier(context: Context[DataContext, Identifier]): Option[Deposit] = {
    val repository = context.ctx.deposits

    val identifierId = context.value.id

    repository.getDepositByIdentifierId(identifierId)
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

  implicit val InputIdentifierType: InputObjectType[InputIdentifier] = deriveInputObjectType(
    InputObjectTypeName("InputIdentifier"),
    InputObjectTypeDescription("An identifier related to a deposit."),
    DocumentInputField("idType", "The type of identifier."),
    DocumentInputField("idValue", "The value of the identifier."),
    DocumentInputField("timestamp", "The timestamp at which the identifier got added to this deposit."),
    RenameInputField("idType", "type"),
    RenameInputField("idValue", "value"),
  )
  implicit val InputIdentifierFromInput: FromInput[InputIdentifier] = fromInput(ad => InputIdentifier(
    idType = ad("type").asInstanceOf[IdentifierType.Value],
    idValue = ad("value").asInstanceOf[String],
    timestamp = ad("timestamp").asInstanceOf[Timestamp],
  ))
}
