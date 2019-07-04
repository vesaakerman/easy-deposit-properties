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

import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import nl.knaw.dans.easy.properties.app.repository.{ MutationErrorOr, QueryErrorOr, SpringfieldDao }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class DemoSpringfieldDao(implicit repo: SpringfieldRepo, depositRepo: DepositRepo) extends SpringfieldDao with DemoDao with DebugEnhancedLogging {

  override def getById(id: String): QueryErrorOr[Option[Springfield]] = {
    trace(id)
    getObjectById(id)
  }

  override def getCurrent(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Option[Springfield])]] = {
    trace(ids)
    getCurrentObjects(ids)
  }

  override def getAll(ids: Seq[DepositId]): QueryErrorOr[Seq[(DepositId, Seq[Springfield])]] = {
    trace(ids)
    getAllObjects(ids)
  }

  override def store(id: DepositId, springfield: InputSpringfield): MutationErrorOr[Springfield] = {
    trace(id, springfield)
    storeNode(id, springfield) {
      case (springfieldId, InputSpringfield(domain, user, collection, playmode, timestamp)) => Springfield(springfieldId, domain, user, collection, playmode, timestamp)
    }
  }

  override def getDepositsById(ids: Seq[String]): QueryErrorOr[Seq[(String, Option[Deposit])]] = {
    trace(ids)
    getDepositsByObjectId(ids)
  }
}
