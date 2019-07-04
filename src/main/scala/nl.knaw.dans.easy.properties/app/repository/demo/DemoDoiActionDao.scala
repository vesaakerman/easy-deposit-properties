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

import nl.knaw.dans.easy.properties.app.model.{ DepositId, DoiActionEvent }
import nl.knaw.dans.easy.properties.app.repository.{ DoiActionDao, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoDoiActionDao(implicit repo: DoiActionRepo, depositRepo: DepositRepo) extends DoiActionDao with DemoDao with DebugEnhancedLogging {

  override def getCurrent(id: DepositId): QueryErrorOr[Option[DoiActionEvent]] = {
    trace(id)
    getCurrentObject(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[DoiActionEvent])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(id: DepositId): QueryErrorOr[Seq[DoiActionEvent]] = {
    trace(id)
    getAllObjects(id)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[DoiActionEvent])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, action: DoiActionEvent): MutationErrorOr[DoiActionEvent] = {
    trace(id, action)
    storeNonNode(id, action)
  }
}
