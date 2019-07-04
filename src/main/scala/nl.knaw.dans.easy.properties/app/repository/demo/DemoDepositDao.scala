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
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, Timestamp, timestampOrdering }
import nl.knaw.dans.easy.properties.app.repository.{ DepositAlreadyExistsError, DepositDao, DepositDoesNotExistError, DepositFilters, MaxByOption, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoDepositDao(implicit depositRepo: DepositRepo,
                     stateRepo: StateRepo,
                     ingestStepRepo: IngestStepRepo,
                     identifierRepo: IdentifierRepo,
                     doiRegisteredRepo: DoiRegisteredRepo,
                     doiActionRepo: DoiActionRepo,
                     curationRepo: CurationRepo,
                     springfieldRepo: SpringfieldRepo,
                     contentTypeRepo: ContentTypeRepo,
                    ) extends DepositDao with DebugEnhancedLogging {

  override def getAll: QueryErrorOr[Seq[Deposit]] = {
    trace(())
    depositRepo.values.toSeq.asRight
  }

  override def find(id: DepositId): QueryErrorOr[Deposit] = {
    trace(id)
    depositRepo.get(id)
      .map(_.asRight)
      .getOrElse(DepositDoesNotExistError(id).asLeft)
  }

  private def search(filters: DepositFilters): QueryErrorOr[Seq[Deposit]] = {
    getAll
      .map(deposits => {
        val fromDepositor = filters.depositorId
          .fold(deposits.withFilter(_ => true))(depositor => {
            deposits.withFilter(_.depositorId == depositor)
          })

        filters.bagName
          .fold(fromDepositor)(name => fromDepositor.withFilter(_.bagName.exists(_ == name)))
          .filter(filters.stateFilter, stateRepo)(_.label, _.label)
          .filter(filters.ingestStepFilter, ingestStepRepo)(_.label, _.step)
          .filter(filters.doiRegisteredFilter, doiRegisteredRepo)(_.value, _.value)
          .filter(filters.doiActionFilter, doiActionRepo)(_.value, _.value)
          .filter(filters.curatorFilter, curationRepo)(_.curator, _.datamanagerUserId)
          .filter(filters.isNewVersionFilter, curationRepo)(_.isNewVersion, _.isNewVersion)
          .filter(filters.curationRequiredFilter, curationRepo)(_.curationRequired, _.isRequired)
          .filter(filters.curationPerformedFilter, curationRepo)(_.curationPerformed, _.isPerformed)
          .filter(filters.contentTypeFilter, contentTypeRepo)(_.value, _.value)
          .map(identity)
      })
  }

  override def search(filters: Seq[DepositFilters]): QueryErrorOr[Seq[(DepositFilters, Seq[Deposit])]] = {
    trace(filters)
    filters.toList.traverse(filter => search(filter).tupleLeft(filter))
  }

  override def store(deposit: Deposit): MutationErrorOr[Deposit] = {
    trace(deposit)
    if (depositRepo contains deposit.id)
      DepositAlreadyExistsError(deposit.id).asLeft
    else {
      depositRepo += (deposit.id -> deposit)
      deposit.asRight
    }
  }

  private def getTimestamps(id: DepositId): QueryErrorOr[Seq[Timestamp]] = {
    if (depositRepo.contains(id)) {
      (depositRepo.get(id).map(_.creationTimestamp).toList :::
        stateRepo.get(id).toList.flatMap(_.map(_.timestamp)) :::
        ingestStepRepo.get(id).toList.flatMap(_.map(_.timestamp)) :::
        identifierRepo.collect { case ((`id`, _), identifier) => identifier.timestamp }.toList :::
        doiRegisteredRepo.get(id).toList.flatMap(_.map(_.timestamp)) :::
        doiActionRepo.get(id).toList.flatMap(_.map(_.timestamp)) :::
        curationRepo.get(id).toList.flatMap(_.map(_.timestamp)) :::
        springfieldRepo.get(id).toList.flatMap(_.map(_.timestamp)) :::
        contentTypeRepo.get(id).toList.flatMap(_.map(_.timestamp)) :::
        Nil).asRight
    }
    else
      DepositDoesNotExistError(id).asLeft
  }

  private def getLatestTimestamp(id: DepositId): QueryErrorOr[Option[Timestamp]] = {
    getTimestamps(id).map(_.maxByOption(identity))
  }

  override def lastModified(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Timestamp])]] = {
    trace(ids)
    ids.toList.traverse(id => getLatestTimestamp(id).tupleLeft(id))
  }
}
