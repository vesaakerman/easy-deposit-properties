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

import cats.syntax.either._
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, InputIdentifier }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.repository.{ IdentifierAlreadyExistsError, IdentifierDao, MaxByOption, MutationErrorOr, NoSuchDepositError, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoIdentifierDao(implicit repo: IdentifierRepo, depositRepo: DepositRepo) extends IdentifierDao with DemoDao with DebugEnhancedLogging {

  override def getById(id: String): QueryErrorOr[Option[Identifier]] = {
    trace(id)
    repo.values.toStream.find(_.id == id).asRight
  }

  override def getByType(id: DepositId, idType: IdentifierType): QueryErrorOr[Option[Identifier]] = {
    trace(id)
    repo.get(id -> idType).asRight
  }

  override def getByType(ids: Seq[(DepositId, IdentifierType)]): QueryErrorOr[Seq[((DepositId, IdentifierType), Option[Identifier])]] = {
    trace(ids)
    ids.map {
      case key @ (depositId, idType) => key -> repo.get(depositId -> idType)
    }.asRight
  }

  override def getByTypeAndValue(idType: IdentifierType, idValue: String): QueryErrorOr[Option[Identifier]] = {
    trace(idType, idValue)
    repo.values.toStream.find(identifier => identifier.idType == idType && identifier.idValue == idValue).asRight
  }

  override def getAll(id: DepositId): QueryErrorOr[Seq[Identifier]] = {
    trace(id)
    repo.find { case ((depId, _), _) => depId == id }
      .map(_ => repo.collect { case ((`id`, _), identifiers) => identifiers }.toSeq)
      .getOrElse(Seq.empty)
      .asRight
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Identifier])]] = {
    trace(ids)
    ids.map(depositId => depositId ->
      repo.find { case ((depId, _), _) => depId == depositId }
        .map(_ => repo.collect { case ((`depositId`, _), identifiers) => identifiers }.toSeq).getOrElse(Seq.empty))
      .asRight
  }

  override def store(id: DepositId, identifier: InputIdentifier): MutationErrorOr[Identifier] = {
    trace(id, identifier)
    if (depositRepo contains id) {
      val InputIdentifier(idType, idValue, timestamp) = identifier

      val identifierId = id.toString.last + repo
        .collect { case ((`id`, _), ident) => ident }
        .maxByOption(_.id).fold(0)(_.id.last.toInt + 1)
        .toString
      val newIdentifier = Identifier(identifierId, idType, idValue, timestamp)

      if (repo contains(id, idType))
        IdentifierAlreadyExistsError(id, idType).asLeft
      else {
        repo += ((id, idType) -> newIdentifier)
        newIdentifier.asRight
      }
    }
    else NoSuchDepositError(id).asLeft
  }

  override def getDepositById(id: String): QueryErrorOr[Option[Deposit]] = {
    trace(id)
    repo
      .collectFirst { case ((depositId, _), identifier) if identifier.id == id => depositId }
      .flatMap(depositRepo.get)
      .asRight
  }
}
