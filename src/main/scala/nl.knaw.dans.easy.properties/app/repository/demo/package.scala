package nl.knaw.dans.easy.properties.app.repository

import nl.knaw.dans.easy.properties.app.model.contentType.ContentType
import nl.knaw.dans.easy.properties.app.model.curation.Curation
import nl.knaw.dans.easy.properties.app.model.identifier.Identifier
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStep
import nl.knaw.dans.easy.properties.app.model.springfield.Springfield
import nl.knaw.dans.easy.properties.app.model.state.State
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, DoiActionEvent, DoiRegisteredEvent }

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
}
