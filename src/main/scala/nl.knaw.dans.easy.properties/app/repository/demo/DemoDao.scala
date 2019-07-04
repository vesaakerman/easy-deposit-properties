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
package nl.knaw.dans.easy.properties.app.repository.demo

import cats.instances.either._
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.functor._
import cats.syntax.traverse._
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, Timestamped, timestampOrdering }
import nl.knaw.dans.easy.properties.app.repository.{ MaxByOption, MutationErrorOr, NoSuchDepositError, QueryErrorOr }
import sangria.relay.Node

import scala.collection.mutable

trait DemoDao {

  def getObjectById[T <: Node](id: String)(implicit repo: mutable.Map[_, Seq[T]]): QueryErrorOr[Option[T]] = {
    repo.values.toStream.flatten.find(_.id == id).asRight
  }

  private def getCurrentObject[T <: Timestamped](id: DepositId)(implicit repo: Repo[Seq[T]]): QueryErrorOr[Option[T]] = {
    repo.get(id).flatMap(_.maxByOption(_.timestamp)).asRight
  }

  def getCurrentObjects[T <: Timestamped](ids: Seq[DepositId])(implicit repo: Repo[Seq[T]]): QueryErrorOr[Seq[(DepositId, Option[T])]] = {
    ids.toList.traverse(id => getCurrentObject(id).tupleLeft(id))
  }

  private def getAllObjects[T](id: DepositId)(implicit repo: Repo[Seq[T]]): QueryErrorOr[Seq[T]] = {
    repo.getOrElse(id, Seq.empty).asRight
  }

  def getAllObjects[T](ids: Seq[DepositId])(implicit repo: Repo[Seq[T]]): QueryErrorOr[Seq[(DepositId, Seq[T])]] = {
    ids.toList.traverse(id => getAllObjects(id).tupleLeft(id))
  }

  def storeNode[I, O <: Node](id: DepositId, input: I)(conversion: (String, I) => O)(implicit repo: Repo[Seq[O]], depositRepo: DepositRepo): MutationErrorOr[O] = {
    if (depositRepo contains id) {
      val newId = id.toString.last + repo
        .collectFirst { case (`id`, os) => os }
        .fold(0)(_.maxByOption(_.id).fold(0)(_.id.last.toInt + 1))
        .toString
      val newObject = conversion(newId, input)

      if (repo contains id)
        repo.update(id, repo(id) :+ newObject)
      else
        repo += (id -> Seq(newObject))

      newObject.asRight
    }
    else NoSuchDepositError(id).asLeft
  }

  def storeNonNode[T](id: DepositId, t: T)(implicit repo: Repo[Seq[T]], depositRepo: DepositRepo): MutationErrorOr[T] = {
    if (depositRepo contains id) {
      if (repo contains id)
        repo.update(id, repo(id) :+ t)
      else
        repo += (id -> Seq(t))

      t.asRight
    }
    else NoSuchDepositError(id).asLeft
  }

  def getDepositByObjectId[T <: Node](id: String)(implicit repo: Repo[Seq[T]], depositRepo: DepositRepo): QueryErrorOr[Option[Deposit]] = {
    repo
      .collectFirst { case (depositId, ts) if ts.exists(_.id == id) => depositId }
      .flatMap(depositRepo.get)
      .asRight
  }
  
  def getDepositsByObjectId[T <: Node](ids: Seq[String])(implicit repo: Repo[Seq[T]], depositRepo: DepositRepo): QueryErrorOr[Seq[(String, Option[Deposit])]] = {
    ids.toList.traverse(id => getDepositByObjectId(id).tupleLeft(id))
  }
}
