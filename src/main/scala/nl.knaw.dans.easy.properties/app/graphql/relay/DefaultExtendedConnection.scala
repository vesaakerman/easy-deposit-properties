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
package nl.knaw.dans.easy.properties.app.graphql.relay

import sangria.relay.{ Connection, DefaultConnection, Edge, PageInfo }

private[relay] case class DefaultExtendedConnection[T](pageInfo: PageInfo,
                                                       edges: Seq[Edge[T]],
                                                       totalCount: Int,
                                                      ) extends ExtendedConnection[T]

private[relay] object DefaultExtendedConnection {
  def apply[T](conn: Connection[T], size: Int): DefaultExtendedConnection[T] = {
    val DefaultConnection(pageInfo, edges) = conn

    DefaultExtendedConnection(pageInfo, edges, size)
  }
}
