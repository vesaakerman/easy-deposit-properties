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

import nl.knaw.dans.easy.properties.app.model.{ DepositId, DoiRegisteredEvent }
import nl.knaw.dans.easy.properties.app.repository.{ DoiRegisteredDao, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoDoiRegisteredDao(implicit repo: DoiRegisteredRepo, depositRepo: DepositRepo) extends DoiRegisteredDao with DemoDao with DebugEnhancedLogging {

  override def getCurrent(id: DepositId): QueryErrorOr[Option[DoiRegisteredEvent]] = {
    trace(id)
    getCurrentObject(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiRegisteredEvent])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(id: DepositId): QueryErrorOr[Seq[DoiRegisteredEvent]] = {
    trace(id)
    getAllObjects(id)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[DoiRegisteredEvent])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, registered: DoiRegisteredEvent): MutationErrorOr[DoiRegisteredEvent] = {
    trace(id, registered)
    storeNonNode(id, registered)
  }
}
