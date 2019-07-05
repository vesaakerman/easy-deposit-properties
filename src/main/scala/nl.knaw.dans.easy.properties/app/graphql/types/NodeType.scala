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

import java.util.UUID

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.easy.properties.app.graphql.resolvers.{ ContentTypeResolver, CurationResolver, IdentifierResolver, IngestStepResolver, SpringfieldResolver, StateResolver }
import sangria.relay.{ GlobalId, Node, NodeDefinition }
import sangria.schema.Context

trait NodeType {
  this: DepositType
    with DepositorType
    with StateType
    with IngestStepType
    with IdentifierGraphQLType
    with CurationType
    with SpringfieldType
    with ContentTypeGraphQLType =>

  val NodeDefinition(nodeInterface, nodeField, nodesField) = Node.definition((id: GlobalId, ctx: Context[DataContext, Unit]) => {
    if (id.typeName == "Deposit") ctx.ctx.repo.deposits.find(UUID.fromString(id.id)).map(Option(_)).toTry
    else if (id.typeName == "State") StateResolver.stateById(id.id)(ctx.ctx)
    else if (id.typeName == "IngestStep") IngestStepResolver.ingestStepById(id.id)(ctx.ctx)
    else if (id.typeName == "Identifier") IdentifierResolver.identifierById(id.id)(ctx.ctx)
    else if (id.typeName == "Curator") CurationResolver.curationById(id.id)(ctx.ctx).map(_.map(_.getCurator))(ctx.ctx.executionContext)
    else if (id.typeName == "Springfield") SpringfieldResolver.springfieldById(id.id)(ctx.ctx)
    else if (id.typeName == "ContentType") ContentTypeResolver.contentTypeById(id.id)(ctx.ctx)
    else None
  }, Node.possibleNodeTypes[DataContext, Node](DepositType, StateType, IngestStepType, IdentifierObjectType, CurationType, SpringfieldType, ContentTypeType))
}
