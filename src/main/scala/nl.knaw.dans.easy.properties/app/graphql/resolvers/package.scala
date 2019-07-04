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

import cats.syntax.either._
import nl.knaw.dans.easy.properties.app.model.DepositId
import nl.knaw.dans.easy.properties.app.repository.{ QueryError, QueryErrorOr }
import sangria.execution.deferred.{ Fetcher, HasId }

import scala.concurrent.ExecutionContext

package object resolvers {

  private[resolvers] implicit def executionContext(implicit ctx: DataContext): ExecutionContext = ctx.executionContext

  private[resolvers] implicit def keyBasedHasId[K, V]: HasId[(K, V), K] = HasId { case (id, _) => id }

  type CurrentFetcher[T] = Fetcher[DataContext, (DepositId, Option[T]), (DepositId, Option[T]), DepositId]

  private[resolvers] def fetchCurrent[T](currentOne: DataContext => DepositId => QueryErrorOr[Option[T]],
                                         currentMany: DataContext => Seq[DepositId] => QueryErrorOr[Seq[(DepositId, Option[T])]]): CurrentFetcher[T] = {
    Fetcher((ctx: DataContext, ids: Seq[DepositId]) => ids match {
      case Seq() => Seq.empty.asRight[QueryError].toFuture
      case Seq(id) => currentOne(ctx)(id).map(optT => Seq(id -> optT)).toFuture
      case _ => currentMany(ctx)(ids).toFuture
    })
  }

  type AllFetcher[T] = Fetcher[DataContext, (DepositId, Seq[T]), (DepositId, Seq[T]), DepositId]

  private[resolvers] def fetchAll[T](currentOne: DataContext => DepositId => QueryErrorOr[Seq[T]],
                                     currentMany: DataContext => Seq[DepositId] => QueryErrorOr[Seq[(DepositId, Seq[T])]]): AllFetcher[T] = {
    Fetcher((ctx: DataContext, ids: Seq[DepositId]) => ids match {
      case Seq() => Seq.empty.asRight[QueryError].toFuture
      case Seq(id) => currentOne(ctx)(id).map(seqT => Seq(id -> seqT)).toFuture
      case _ => currentMany(ctx)(ids).toFuture
    })
  }

  private[resolvers] implicit class CollectionExtensions[T](val xs: Seq[T]) extends AnyVal {
    def distinctUntilChanged: Seq[T] = distinctUntilChanged(identity)

    def distinctUntilChanged[S](f: T => S): Seq[T] = {
      var latest: Option[S] = None
      val builder = Seq.newBuilder[T]

      for (x <- xs;
           s = f(x)) {
        latest match {
          case Some(`s`) => // do nothing
          case _ =>
            builder += x
            latest = Some(s)
        }
      }

      builder.result()
    }
  }
}
