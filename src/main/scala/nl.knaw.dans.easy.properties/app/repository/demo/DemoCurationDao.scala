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

import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.model.curation.{ Curation, InputCuration }
import nl.knaw.dans.easy.properties.app.repository.{ CurationDao, MutationErrorOr, QueryErrorOr }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoCurationDao(implicit repo: CurationRepo, depositRepo: DepositRepo) extends CurationDao with DemoDao with DebugEnhancedLogging {

  override def getById(id: String): QueryErrorOr[Option[Curation]] = {
    trace(id)
    getObjectById(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Curation])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Curation])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, curation: InputCuration): MutationErrorOr[Curation] = {
    trace(id, curation)
    storeNode(id, curation) {
      case (curatorId, InputCuration(isNewVersion, isRequired, isPerformed, userId, email, timestamp)) => Curation(curatorId, isNewVersion, isRequired, isPerformed, userId, email, timestamp)
    }
  }

  override def getDepositsById(ids: Seq[String]): QueryErrorOr[Seq[(String, Option[Deposit])]] = {
    trace(ids)
    getDepositsByObjectId(ids)
  }
}
