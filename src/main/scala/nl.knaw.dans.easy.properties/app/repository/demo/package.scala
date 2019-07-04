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
package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.contentType.ContentType
import nl.knaw.dans.easy.properties.app.model.curation.Curation
import nl.knaw.dans.easy.properties.app.model.identifier.Identifier
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStep
import nl.knaw.dans.easy.properties.app.model.springfield.Springfield
import nl.knaw.dans.easy.properties.app.model.state.State
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositFilter, DepositId, DoiActionEvent, DoiRegisteredEvent, SeriesFilter, Timestamped, timestampOrdering }

import scala.collection.generic.FilterMonadic
import scala.collection.mutable

package object demo {

  type Repo[T] = mutable.Map[DepositId, T]
  val Repo: mutable.Map.type = mutable.Map

  type DepositRepo = Repo[Deposit]
  type StateRepo = Repo[Seq[State]]
  type IngestStepRepo = Repo[Seq[IngestStep]]
  type IdentifierRepo = mutable.Map[(DepositId, IdentifierType), Identifier]
  type DoiRegisteredRepo = Repo[Seq[DoiRegisteredEvent]]
  type DoiActionRepo = Repo[Seq[DoiActionEvent]]
  type CurationRepo = Repo[Seq[Curation]]
  type SpringfieldRepo = Repo[Seq[Springfield]]
  type ContentTypeRepo = Repo[Seq[ContentType]]

  private[demo] implicit class FilterExt(val collection: FilterMonadic[Deposit, Seq[Deposit]]) extends AnyVal {
    def filter[T <: Timestamped, F <: DepositFilter, V](filter: Option[F], repo: mutable.Map[DepositId, Seq[T]])
                                                       (get: F => V, label: T => V): FilterMonadic[Deposit, Seq[Deposit]] = {
      filter.fold(collection)(depositFilter => {
        collection.withFilter(d => {
          val ts = repo.getOrElse(d.id, Seq.empty)
          val selectedTs = depositFilter.filter match {
            case SeriesFilter.LATEST => ts.maxByOption(_.timestamp).toSeq
            case SeriesFilter.ALL => ts
          }
          selectedTs.exists(t => label(t) == get(depositFilter))
        })
      })
    }
  }
}
