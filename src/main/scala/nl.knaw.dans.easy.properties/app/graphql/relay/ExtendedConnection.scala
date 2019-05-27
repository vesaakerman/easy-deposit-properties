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

import sangria.relay._
import sangria.schema._

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.higherKinds
import scala.reflect.ClassTag

trait ExtendedConnection[T] extends Connection[T] {
  def totalCount: Int
}

object ExtendedConnection {

  def definition[Ctx, Conn[_], Val](name: String,
                                    nodeType: OutputType[Val],
                                    edgeFields: => List[Field[Ctx, Edge[Val]]] = Nil,
                                    connectionFields: => List[Field[Ctx, Conn[Val]]] = Nil
                                   )(implicit connEv: ExtendedConnectionLike[Conn, Val, Edge[Val]],
                                     classEv: ClassTag[Conn[Val]]): ConnectionDefinition[Ctx, Conn[Val], Val, Edge[Val]] = {
    definitionWithEdge[Ctx, Conn, Val, Edge[Val]](name, nodeType, edgeFields, connectionFields)
  }

  def definitionWithEdge[Ctx, Conn[_], Val, E <: Edge[Val]](name: String,
                                                            nodeType: OutputType[Val],
                                                            edgeFields: => List[Field[Ctx, E]] = Nil,
                                                            connectionFields: => List[Field[Ctx, Conn[Val]]] = Nil
                                                           )(implicit connEv: ExtendedConnectionLike[Conn, Val, E],
                                                             classEv: ClassTag[Conn[Val]],
                                                             classE: ClassTag[E]): ConnectionDefinition[Ctx, Conn[Val], Val, E] = {
    Connection.definitionWithEdge(
      name = name,
      nodeType = nodeType,
      edgeFields = edgeFields,
      connectionFields = connectionFields ++ fields[Ctx, Conn[Val]](
        Field(
          name = "totalCount",
          fieldType = IntType,
          description = Option("Identifies the total count of items in the connection."),
          resolve = ctx => connEv.totalCount(ctx.value),
        ),
      ),
    )
  }

  def empty[T]: ExtendedConnection[T] = {
    DefaultExtendedConnection(Connection.empty[T], 0)
  }

  def connectionFromFutureSeq[T](seq: Future[Seq[T]], args: ConnectionArgs)(implicit ec: ExecutionContext): Future[ExtendedConnection[T]] = {
    seq.map(connectionFromSeq(_, args))
  }

  def connectionFromSeq[T](seq: Seq[T], args: ConnectionArgs): ExtendedConnection[T] = {
    connectionFromSeq(seq, args, SliceInfo(0, seq.size))
  }

  def connectionFromFutureSeq[T](seq: Future[Seq[T]], args: ConnectionArgs, sliceInfo: SliceInfo)(implicit ec: ExecutionContext): Future[ExtendedConnection[T]] = {
    seq.map(connectionFromSeq(_, args, sliceInfo))
  }

  def connectionFromSeq[T](seqSlice: Seq[T], args: ConnectionArgs, sliceInfo: SliceInfo): ExtendedConnection[T] = {
    DefaultExtendedConnection(Connection.connectionFromSeq(seqSlice, args, sliceInfo), seqSlice.size)
  }
}
