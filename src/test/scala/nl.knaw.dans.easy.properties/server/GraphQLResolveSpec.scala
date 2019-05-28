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
package nl.knaw.dans.easy.properties.server

import java.util.UUID

import better.files.File
import nl.knaw.dans.easy.properties.app.graphql.DepositRepository
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ DepositIngestStepFilter, IngestStep, IngestStepLabel }
import nl.knaw.dans.easy.properties.app.model.state.{ DepositStateFilter, State, StateLabel }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositDoiActionFilter, DepositDoiRegisteredFilter, DepositId, DoiActionEvent, DoiRegisteredEvent, SeriesFilter }
import nl.knaw.dans.easy.properties.fixture.{ FileSystemSupport, TestSupportFixture }
import org.joda.time.DateTime
import org.json4s.JsonAST.JNothing
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._
import org.json4s.{ DefaultFormats, Formats }
import org.scalamock.scalatest.MockFactory
import org.scalatra.test.EmbeddedJettyContainer
import org.scalatra.test.scalatest.ScalatraSuite

trait GraphQLResolveSpecTestObjects {
  val depositId1: DepositId = UUID.fromString("00000000-0000-0000-0000-000000000001")
  val depositId2: DepositId = UUID.fromString("00000000-0000-0000-0000-000000000002")
  val depositId3: DepositId = UUID.fromString("00000000-0000-0000-0000-000000000003")
  val deposit1 = Deposit(
    id = depositId1,
    creationTimestamp = DateTime.now(),
    depositorId = "user001",
  )
  val deposit2 = Deposit(
    id = depositId2,
    creationTimestamp = DateTime.now(),
    depositorId = "user002",
  )
  val deposit3 = Deposit(
    id = depositId3,
    creationTimestamp = DateTime.now(),
    depositorId = "user002",
  )
  val state1 = State(
    id = "1",
    label = StateLabel.ARCHIVED,
    description = "your deposit is submitted",
    timestamp = DateTime.now(),
  )
  val state2 = State(
    id = "2",
    label = StateLabel.DRAFT,
    description = "your deposit is in draft",
    timestamp = DateTime.now(),
  )
  val state3 = State(
    id = "3",
    label = StateLabel.ARCHIVED,
    description = "your deposit is submitted",
    timestamp = DateTime.now(),
  )
  val step1 = IngestStep(
    id = "1",
    step = IngestStepLabel.VALIDATE,
    timestamp = DateTime.now(),
  )
  val step2 = IngestStep(
    id = "2",
    step = IngestStepLabel.FEDORA,
    timestamp = DateTime.now(),
  )
  val step3 = IngestStep(
    id = "3",
    step = IngestStepLabel.COMPLETED,
    timestamp = DateTime.now(),
  )
  val identifier1 = Identifier(
    id = "1",
    idType = IdentifierType.URN,
    idValue = "abcdef",
    timestamp = DateTime.now(),
  )
  val identifier2 = Identifier(
    id = "2",
    idType = IdentifierType.DOI,
    idValue = "123456",
    timestamp = DateTime.now(),
  )
  val identifier3 = Identifier(
    id = "3",
    idType = IdentifierType.FEDORA,
    idValue = "easy-dataset:1",
    timestamp = DateTime.now(),
  )
  val doiRegisteredEvent1 = DoiRegisteredEvent(
    value = "yes",
    timestamp = DateTime.now(),
  )
  val doiRegisteredEvent2 = DoiRegisteredEvent(
    value = "no",
    timestamp = DateTime.now(),
  )
  val doiActionEvent1 = DoiActionEvent(
    value = "create",
    timestamp = DateTime.now(),
  )
  val doiActionEvent2 = DoiActionEvent(
    value = "update",
    timestamp = DateTime.now(),
  )
}

class GraphQLResolveSpec extends TestSupportFixture
  with MockFactory
  with FileSystemSupport
  with GraphQLResolveSpecTestObjects
  with EmbeddedJettyContainer
  with ScalatraSuite {

  private val graphqlExamplesDir = testDir / "graphql"
  private val repository = mock[DepositRepository]
  private val servlet = DepositPropertiesGraphQLServlet(repository)
  private implicit val jsonFormats: Formats = new DefaultFormats {}

  addServlet(servlet, "/*")

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    File(getClass.getResource("/graphql-examples"))
      .copyTo(graphqlExamplesDir)
  }

  "graphql" should "resolve 'deposit/findDeposit/plain.graphql' with 8 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "findDeposit" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      inAnyOrder {
        repository.getCurrentState _ expects depositId1 once() returning Some(state1)
        repository.getCurrentIngestStep _ expects depositId1 once() returning Some(step1)
        (repository.getIdentifiers(_: DepositId)) expects depositId1 once() returning Seq(identifier1, identifier2)
        repository.getCurrentDoiRegistered _ expects depositId1 once() returning Some(doiRegisteredEvent1)
        repository.getAllDoiRegistered _ expects depositId1 once() returning Seq(
          doiRegisteredEvent1,
          doiRegisteredEvent2,
        )
        repository.getCurrentDoiAction _ expects depositId1 once() returning Some(doiActionEvent1)
        repository.getAllDoiAction _ expects depositId1 once() returning Seq(
          doiActionEvent1,
          doiActionEvent2,
        )
      }
    }

    runQuery(input)
  }

  it should "resolve 'deposit/findIdentifierWithType/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "findIdentifierWithType" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      repository.getIdentifiersForTypes _ expects Seq((depositId1, IdentifierType.DOI), (depositId1, IdentifierType.BAG_STORE)) once() returning Seq(
        (depositId1, IdentifierType.DOI) -> Some(identifier2),
        (depositId1, IdentifierType.BAG_STORE) -> Some(identifier1),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposit/ingestStepPagination/firstPage.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "ingestStepPagination" / "firstPage.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllIngestSteps(_: DepositId)) expects depositId1 once() returning Seq(step1, step2, step3, step1, step2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/ingestStepPagination/secondPage.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "ingestStepPagination" / "secondPage.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllIngestSteps(_: DepositId)) expects depositId1 once() returning Seq(step1, step2, step3, step1, step2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/ingestStepPagination/thirdPage.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "ingestStepPagination" / "thirdPage.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllIngestSteps(_: DepositId)) expects depositId1 once() returning Seq(step1, step2, step3, step1, step2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listAllIngestStepsOfDeposit/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listAllIngestStepsOfDeposit" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      (repository.getAllIngestSteps(_: DepositId)) expects depositId1 once() returning Seq(step1, step2, step3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listAllStatesOfDeposit/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listAllStatesOfDeposit" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      (repository.getAllStates(_: DepositId)) expects depositId1 once() returning Seq(state1, state2, state3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithIngestStepFilterAll/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithIngestStepFilterAll" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      repository.getCurrentIngestStep _ expects depositId1 once() returning Some(step1)
      repository.getDeposits _ expects(None, None, Some(DepositIngestStepFilter(IngestStepLabel.VALIDATE, SeriesFilter.ALL)), None, None) once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithSameDepositor/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithSameDepositor" / "plain.graphql"
    assume(input.exists, s"input file does not exist: $input")
    val query = input.contentAsString.replace(depositId1.toString, depositId2.toString)

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId2 once() returning Some(deposit2)
      repository.getDeposits _ expects(Some("user002"), None, None, None, None) once() returning Seq(deposit2, deposit3)
      repository.getCurrentStates _ expects Seq(depositId2, depositId3) once() returning Seq(
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(query)
  }

  it should "resolve 'deposit/listDepositsWithSameIngestStep/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithSameIngestStep" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      repository.getCurrentIngestStep _ expects depositId1 once() returning Some(step1)
      repository.getDeposits _ expects(None, None, Some(DepositIngestStepFilter(IngestStepLabel.VALIDATE, SeriesFilter.LATEST)), None, None) once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithSameState/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithSameState" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      repository.getCurrentState _ expects depositId1 once() returning Some(state1)
      repository.getDeposits _ expects(None, Some(DepositStateFilter(StateLabel.ARCHIVED, SeriesFilter.LATEST)), None, None, None) once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithStateFilterAll/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithStateFilterAll" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      repository.getCurrentState _ expects depositId1 once() returning Some(state1)
      repository.getDeposits _ expects(None, Some(DepositStateFilter(StateLabel.ARCHIVED, SeriesFilter.ALL)), None, None, None) once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/statePagination/firstPage.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "statePagination" / "firstPage.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllStates(_: DepositId)) expects depositId1 once() returning Seq(state1, state2, state3, state1, state2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/statePagination/secondPage.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "statePagination" / "secondPage.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllStates(_: DepositId)) expects depositId1 once() returning Seq(state1, state2, state3, state1, state2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/statePagination/thirdPage.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "statePagination" / "thirdPage.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllStates(_: DepositId)) expects depositId1 once() returning Seq(state1, state2, state3, state1, state2)
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(Some("user002"), None, None, None, None) once() returning Seq(deposit2, deposit3)
      repository.getCurrentStates _ expects Seq(depositId2, depositId3) once() returning Seq(
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithIngestStepAndDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithIngestStepAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(Some("user001"), None, Some(DepositIngestStepFilter(IngestStepLabel.COMPLETED, SeriesFilter.LATEST)), None, None) once() returning Seq(deposit2, deposit3)
      repository.getCurrentStates _ expects Seq(depositId2, depositId3) once() returning Seq(
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithStateAndDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithStateAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(Some("user001"), Some(DepositStateFilter(StateLabel.ARCHIVED, SeriesFilter.LATEST)), None, None, None) once() returning Seq(deposit1, deposit3)
      repository.getCurrentStates _ expects Seq(depositId1, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithStateFilterAllAndDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithStateFilterAllAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(Some("user001"), Some(DepositStateFilter(StateLabel.DRAFT, SeriesFilter.ALL)), None, None, None) once() returning Seq(deposit1, deposit3)
      repository.getCurrentStates _ expects Seq(depositId1, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listAllDeposits/plain.graphql' with 8 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listAllDeposits" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(None, None, None, None, None) once() returning Seq(deposit1, deposit2, deposit3)
      inAnyOrder {
        repository.getCurrentStates _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Some(state1),
          depositId2 -> Some(state2),
          depositId3 -> Some(state3),
        )
        repository.getCurrentIngestSteps _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Some(step1),
          depositId2 -> Some(step2),
          depositId3 -> Some(step3),
        )
        (repository.getIdentifiers(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Seq(identifier1, identifier2),
          depositId2 -> Seq(identifier2, identifier3),
          depositId3 -> Seq.empty,
        )
        repository.getCurrentDoisRegistered _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Some(doiRegisteredEvent1),
          depositId2 -> Some(doiRegisteredEvent2),
          depositId3 -> None,
        )
        repository.getAllDoisRegistered _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Seq(doiRegisteredEvent1, doiRegisteredEvent2),
          depositId2 -> Seq(doiRegisteredEvent2),
          depositId3 -> Seq.empty,
        )
        repository.getCurrentDoisAction _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Some(doiActionEvent1),
          depositId2 -> Some(doiActionEvent2),
          depositId3 -> None,
        )
        repository.getAllDoisAction _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Seq(doiActionEvent1, doiActionEvent2),
          depositId2 -> Seq(doiActionEvent2),
          depositId3 -> Seq.empty,
        )
      }
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listAllDepositsWithAllIngestSteps/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listAllDepositsWithAllIngestSteps" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(None, None, None, None, None) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getAllIngestSteps(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Seq(step1, step2),
        depositId2 -> Seq(step2, step3),
        depositId3 -> Seq(step3, step1),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listAllDepositsWithAllStates/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listAllDepositsWithAllStates" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(None, None, None, None, None) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getAllStates(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Seq(state1, state2),
        depositId2 -> Seq(state2, state3),
        depositId3 -> Seq(state3, state1),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithDoiAction/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithDoiAction" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(None, None, None, None, Some(DepositDoiActionFilter("create", SeriesFilter.LATEST))) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getCurrentDoisAction(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Some(doiActionEvent1),
        depositId2 -> Some(doiActionEvent2),
        depositId3 -> None,
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithDoiRegistered/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithDoiRegistered" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(None, None, None, Some(DepositDoiRegisteredFilter("yes", SeriesFilter.LATEST)), None) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getCurrentDoisRegistered(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Some(doiRegisteredEvent1),
        depositId2 -> Some(doiRegisteredEvent2),
        depositId3 -> None,
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithIngestStepFilterAll/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithIngestStepFilterAll" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(None, None, Some(DepositIngestStepFilter(IngestStepLabel.VALIDATE, SeriesFilter.ALL)), None, None) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getCurrentStates(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithState/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithState" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(None, Some(DepositStateFilter(StateLabel.ARCHIVED, SeriesFilter.LATEST)), None, None, None) once() returning Seq(deposit1, deposit3)
      repository.getCurrentStates _ expects Seq(depositId1, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithStateFilterAll/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithStateFilterAll" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(None, Some(DepositStateFilter(StateLabel.DRAFT, SeriesFilter.ALL)), None, None, None) once() returning Seq(deposit1, deposit3)
      repository.getCurrentStates _ expects Seq(depositId1, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithStateFilterAllAndIngestStepFilterAll/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithStateFilterAllAndIngestStepFilterAll" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects(None, Some(DepositStateFilter(StateLabel.REJECTED, SeriesFilter.ALL)), Some(DepositIngestStepFilter(IngestStepLabel.VALIDATE, SeriesFilter.ALL)), None, None) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getCurrentStates(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/pagination/firstPage.graphql' with 1 call to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "pagination" / "firstPage.graphql"

    inSequence {
      repository.getDeposits _ expects(None, None, None, None, None) once() returning Seq(deposit1, deposit2, deposit3, deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'deposits/pagination/secondPage.graphql' with 1 call to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "pagination" / "secondPage.graphql"

    inSequence {
      repository.getDeposits _ expects(None, None, None, None, None) once() returning Seq(deposit1, deposit2, deposit3, deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'deposits/pagination/thirdPage.graphql' with 1 call to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "pagination" / "thirdPage.graphql"

    inSequence {
      repository.getDeposits _ expects(None, None, None, None, None) once() returning Seq(deposit1, deposit2, deposit3, deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'identifier/findWithTypeAndValue/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "identifier" / "findWithTypeAndValue" / "plain.graphql"

    inSequence {
      (repository.getIdentifier(_: IdentifierType, _: String)) expects(IdentifierType.DOI, "10.5072/dans-a1b-cde2") once() returning Some(identifier2)
      repository.getDepositByIdentifierId _ expects identifier2.id once() returning Some(deposit1)
    }

    runQuery(input)
  }

  it should "resolve 'node/onDeposit.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "node" / "onDeposit.graphql"

    inSequence {
      repository.getDeposit _ expects depositId2 once() returning Some(deposit2)
      repository.getCurrentState _ expects depositId2 once() returning Some(state2)
    }

    runQuery(input)
  }

  it should "resolve 'node/onIdentifier.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "node" / "onIdentifier.graphql"

    inSequence {
      repository.getIdentifierById _ expects "12" once() returning Some(identifier2)
      repository.getDepositByIdentifierId _ expects identifier2.id once() returning Some(deposit1)
    }

    runQuery(input)
  }

  it should "resolve 'node/onIngestStep.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "node" / "onIngestStep.graphql"

    inSequence {
      repository.getIngestStepById _ expects "10" once() returning Some(step1)
      repository.getDepositByIngestStepId _ expects step1.id once() returning Some(deposit1)
      repository.getDeposits _ expects(None, None, Some(DepositIngestStepFilter(IngestStepLabel.VALIDATE, SeriesFilter.ALL)), None, None) once() returning Seq(deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'node/onState.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "node" / "onState.graphql"

    inSequence {
      repository.getStateById _ expects "15" once() returning Some(state2)
      repository.getDeposits _ expects(None, Some(DepositStateFilter(StateLabel.DRAFT, SeriesFilter.ALL)), None, None, None) once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  private def runQuery(graphQLFile: File): Unit = {
    assume(graphQLFile.exists, s"input file does not exist: $graphQLFile")
    runQuery(graphQLFile.contentAsString)
  }

  private def runQuery(query: String): Unit = {
    val inputBody = compact(render("query" -> query))
    post(uri = "/", body = inputBody.getBytes) {
      parse(body) \ "errors" match {
        case JNothing => // do nothing, no errors
        case jsonErrors => read[Seq[Error]](compact(render(jsonErrors))) shouldBe empty
      }

      status shouldBe 200
    }
  }

  case class Location(line: Int, column: Int)
  case class Error(message: String, path: Seq[String], locations: Seq[Location])
}
