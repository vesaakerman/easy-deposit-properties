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
package nl.knaw.dans.easy.properties.app.graphql.model

import nl.knaw.dans.easy.properties.app.model.{ IsNewVersionEvent, Timestamp }
import sangria.macros.derive.{ GraphQLDescription, GraphQLField, GraphQLName }

@GraphQLName("IsNewVersionEvent")
@GraphQLDescription("State whether this deposit is a new version, requiring a new DOI and deposit agreement to be generated by easy-ingest-flow.")
class GraphQLIsNewVersion(isNewVersion: IsNewVersionEvent) {

  @GraphQLField
  @GraphQLDescription("True if the deposit is a new version.")
  val value: Option[Boolean] = isNewVersion.isNewVersion

  @GraphQLField
  @GraphQLDescription("The timestamp at which was decided that this is a new version.")
  val timestamp: Timestamp = isNewVersion.timestamp
}
