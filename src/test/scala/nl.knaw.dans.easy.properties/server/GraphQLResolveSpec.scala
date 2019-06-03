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
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentType, ContentTypeValue, DepositContentTypeFilter }
import nl.knaw.dans.easy.properties.app.model.curator.{ Curator, DepositCuratorFilter }
import nl.knaw.dans.easy.properties.app.model.identifier.IdentifierType.IdentifierType
import nl.knaw.dans.easy.properties.app.model.identifier.{ Identifier, IdentifierType }
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ DepositIngestStepFilter, IngestStep, IngestStepLabel }
import nl.knaw.dans.easy.properties.app.model.springfield.{ Springfield, SpringfieldPlayMode }
import nl.knaw.dans.easy.properties.app.model.state.{ DepositStateFilter, State, StateLabel }
import nl.knaw.dans.easy.properties.app.model.{ CurationPerformedEvent, CurationRequiredEvent, Deposit, DepositCurationPerformedFilter, DepositCurationRequiredFilter, DepositDoiActionFilter, DepositDoiRegisteredFilter, DepositId, DepositIsNewVersionFilter, DoiAction, DoiActionEvent, DoiRegisteredEvent, IsNewVersionEvent, SeriesFilter }
import nl.knaw.dans.easy.properties.app.repository.{ DepositFilters, DepositRepository }
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
    value = true,
    timestamp = DateTime.now(),
  )
  val doiRegisteredEvent2 = DoiRegisteredEvent(
    value = false,
    timestamp = DateTime.now(),
  )
  val doiActionEvent1 = DoiActionEvent(
    value = DoiAction.CREATE,
    timestamp = DateTime.now(),
  )
  val doiActionEvent2 = DoiActionEvent(
    value = DoiAction.UPDATE,
    timestamp = DateTime.now(),
  )
  val curator1 = Curator(
    id = "1",
    userId = "archie001",
    email = "does-not-exists1@dans.knaw.nl",
    timestamp = DateTime.now(),
  )
  val curator2 = Curator(
    id = "2",
    userId = "archie002",
    email = "does-not-exists2@dans.knaw.nl",
    timestamp = DateTime.now(),
  )
  val isNewVersion1 = IsNewVersionEvent(
    isNewVersion = true,
    timestamp = DateTime.now(),
  )
  val isNewVersion2 = IsNewVersionEvent(
    isNewVersion = false,
    timestamp = DateTime.now(),
  )
  val curationRequired1 = CurationRequiredEvent(
    curationRequired = true,
    timestamp = DateTime.now(),
  )
  val curationRequired2 = CurationRequiredEvent(
    curationRequired = false,
    timestamp = DateTime.now(),
  )
  val curationPerformed1 = CurationPerformedEvent(
    curationPerformed = true,
    timestamp = DateTime.now(),
  )
  val curationPerformed2 = CurationPerformedEvent(
    curationPerformed = false,
    timestamp = DateTime.now(),
  )
  val springfield1 = Springfield(
    id = "1",
    domain = "domain1",
    user = "user1",
    collection = "collection1",
    playmode = SpringfieldPlayMode.CONTINUOUS,
    timestamp = DateTime.now(),
  )
  val springfield2 = Springfield(
    id = "2",
    domain = "domain2",
    user = "user2",
    collection = "collection2",
    playmode = SpringfieldPlayMode.MENU,
    timestamp = DateTime.now(),
  )
  val contentType1 = ContentType(
    id = "1",
    value = ContentTypeValue.ZIP,
    timestamp = DateTime.now(),
  )
  val contentType2 = ContentType(
    id = "2",
    value = ContentTypeValue.OCTET,
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
  private val servlet = DepositPropertiesGraphQLServlet(() => repository)
  private implicit val jsonFormats: Formats = new DefaultFormats {}

  addServlet(servlet, "/*")

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    File(getClass.getResource("/graphql-examples"))
      .copyTo(graphqlExamplesDir)
  }

  "graphql" should "resolve 'deposit/findDeposit/plain.graphql' with 19 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "findDeposit" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
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
        repository.getCurrentCurator _ expects depositId1 once() returning Some(curator1)
        repository.getCurrentIsNewVersionAction _ expects depositId1 once() returning Some(isNewVersion1)
        repository.getAllIsNewVersionAction _ expects depositId1 once() returning Seq(
          isNewVersion1,
          isNewVersion2,
        )
        repository.getCurrentCurationRequiredAction _ expects depositId1 once() returning Some(curationRequired1)
        repository.getAllCurationRequiredAction _ expects depositId1 once() returning Seq(
          curationRequired1,
          curationRequired2,
        )
        repository.getCurrentCurationPerformedAction _ expects depositId1 once() returning Some(curationPerformed1)
        repository.getAllCurationPerformedAction _ expects depositId1 once() returning Seq(
          curationPerformed1,
          curationPerformed2,
        )
        repository.getCurrentSpringfield _ expects depositId1 once() returning Some(springfield1)
        (repository.getAllSpringfields(_: DepositId)) expects depositId1 once() returning Seq(
          springfield1,
          springfield2,
        )
        repository.getCurrentContentType _ expects depositId1 once() returning Some(contentType1)
        (repository.getAllContentTypes(_: DepositId)) expects depositId1 once() returning Seq(
          contentType1,
          contentType2,
        )
      }
    }

    runQuery(input)
  }

  it should "resolve 'deposit/findIdentifierWithType/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "findIdentifierWithType" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
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

  it should "resolve 'deposit/listAllContentTypesOfDeposit/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listAllContentTypesOfDeposit" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllContentTypes(_: DepositId)) expects depositId1 once() returning Seq(contentType2, contentType1)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listAllCuratorsOfDeposit/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listAllCuratorsOfDeposit" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllCurators(_: DepositId)) expects depositId1 once() returning Seq(curator1, curator2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listAllIngestStepsOfDeposit/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listAllIngestStepsOfDeposit" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllIngestSteps(_: DepositId)) expects depositId1 once() returning Seq(step1, step2, step3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listAllSpringfieldsOfDeposit/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listAllSpringfieldsOfDeposit" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId2 once() returning Some(deposit2)
      (repository.getAllSpringfields(_: DepositId)) expects depositId2 once() returning Seq(springfield1, springfield2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listAllStatesOfDeposit/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listAllStatesOfDeposit" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllStates(_: DepositId)) expects depositId1 once() returning Seq(state1, state2, state3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithContentTypeFilterAll/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithContentTypeFilterAll" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId2 once() returning Some(deposit2)
      repository.getCurrentContentType _ expects depositId2 once() returning Some(contentType2)
      repository.getDeposits _ expects DepositFilters(contentTypeFilter = Some(DepositContentTypeFilter(ContentTypeValue.OCTET, SeriesFilter.ALL))) once() returning Seq(deposit2, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithCuratorFilterAll/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithCuratorFilterAll" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      repository.getCurrentCurator _ expects depositId1 once() returning Some(curator1)
      repository.getDeposits _ expects DepositFilters(curatorFilter = Some(DepositCuratorFilter("archie001", SeriesFilter.ALL))) once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithIngestStepFilterAll/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithIngestStepFilterAll" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      repository.getCurrentIngestStep _ expects depositId1 once() returning Some(step1)
      repository.getDeposits _ expects DepositFilters(ingestStepFilter = Some(DepositIngestStepFilter(IngestStepLabel.VALIDATE, SeriesFilter.ALL))) once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithSameContentType/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithSameContentType" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId2 once() returning Some(deposit2)
      repository.getCurrentContentType _ expects depositId2 once() returning Some(contentType2)
      repository.getDeposits _ expects DepositFilters(contentTypeFilter = Some(DepositContentTypeFilter(ContentTypeValue.OCTET))) once() returning Seq(deposit3, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithSameCurator/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithSameCurator" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      repository.getCurrentCurator _ expects depositId1 once() returning Some(curator2)
      repository.getDeposits _ expects DepositFilters(curatorFilter = Some(DepositCuratorFilter("archie002"))) once() returning Seq(deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithSameDepositor/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithSameDepositor" / "plain.graphql"
    assume(input.exists, s"input file does not exist: $input")
    val query = input.contentAsString.replace(depositId1.toString, depositId2.toString)

    inSequence {
      repository.getDeposit _ expects depositId2 once() returning Some(deposit2)
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user002")) once() returning Seq(deposit2, deposit3)
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
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      repository.getCurrentIngestStep _ expects depositId1 once() returning Some(step1)
      repository.getDeposits _ expects DepositFilters(ingestStepFilter = Some(DepositIngestStepFilter(IngestStepLabel.VALIDATE, SeriesFilter.LATEST))) once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithSameState/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithSameState" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      repository.getCurrentState _ expects depositId1 once() returning Some(state1)
      repository.getDeposits _ expects DepositFilters(stateFilter = Some(DepositStateFilter(StateLabel.ARCHIVED, SeriesFilter.LATEST))) once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithStateFilterAll/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithStateFilterAll" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      repository.getCurrentState _ expects depositId1 once() returning Some(state1)
      repository.getDeposits _ expects DepositFilters(stateFilter = Some(DepositStateFilter(StateLabel.ARCHIVED, SeriesFilter.ALL))) once() returning Seq(deposit1, deposit3)
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

  // Currently 'once()' fails, because it is 'repeated 4'.
  // However, once we got caching working, we should see 'once()' working properly.
  it should "resolve 'deposit/nested.graphql' with n calls to the repository" in pendingUntilFixed {
    val input = graphqlExamplesDir / "deposit" / "nested.graphql"

    repository.getDeposits _ expects DepositFilters() once() returning Seq(deposit1, deposit2, deposit3)
    (repository.getAllStates(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
      depositId1 -> Seq(state1, state2),
      depositId2 -> Seq(state2, state3),
      depositId3 -> Seq.empty,
    )
    repository.getDepositsAggregated _ expects Seq(
      DepositFilters(stateFilter = Some(DepositStateFilter(StateLabel.DRAFT))),
      DepositFilters(stateFilter = Some(DepositStateFilter(StateLabel.ARCHIVED))),
    ) once() returning Seq(
      DepositFilters(stateFilter = Some(DepositStateFilter(StateLabel.DRAFT))) -> Seq(deposit1, deposit2, deposit3),
      DepositFilters(stateFilter = Some(DepositStateFilter(StateLabel.ARCHIVED))) -> Seq(deposit1, deposit2, deposit3),
    )

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithContentTypeAndDepositor/plain.graphql' with 1 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithContentTypeAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user001"), contentTypeFilter = Some(DepositContentTypeFilter(ContentTypeValue.ZIP))) once() returning Seq(deposit2, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithCurationPerformedAndDepositor/plain.graphql' with 1 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithCurationPerformedAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user001"), curationPerformedFilter = Some(DepositCurationPerformedFilter(curationPerformed = true))) once() returning Seq(deposit2, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithCurationRequiredAndDepositor/plain.graphql' with 1 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithCurationRequiredAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user001"), curationRequiredFilter = Some(DepositCurationRequiredFilter(curationRequired = true))) once() returning Seq(deposit2, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithCuratorAndDepositor/plain.graphql' with 1 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithCuratorAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user001"), curatorFilter = Some(DepositCuratorFilter("archie001"))) once() returning Seq(deposit2, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user002")) once() returning Seq(deposit2, deposit3)
      repository.getCurrentStates _ expects Seq(depositId2, depositId3) once() returning Seq(
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithDoiActionAndDepositor/plain.graphql' with 1 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithDoiActionAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user001"), doiActionFilter = Some(DepositDoiActionFilter(DoiAction.CREATE))) once() returning Seq(deposit2, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithDoiRegisteredAndDepositor/plain.graphql' with 1 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithDoiRegisteredAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user001"), doiRegisteredFilter = Some(DepositDoiRegisteredFilter(value = true))) once() returning Seq(deposit2, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithIngestStepAndDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithIngestStepAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user001"), ingestStepFilter = Some(DepositIngestStepFilter(IngestStepLabel.COMPLETED, SeriesFilter.LATEST))) once() returning Seq(deposit2, deposit3)
      repository.getCurrentStates _ expects Seq(depositId2, depositId3) once() returning Seq(
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithIsNewVersionAndDepositor/plain.graphql' with 1 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithIsNewVersionAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user001"), isNewVersionFilter = Some(DepositIsNewVersionFilter(isNewVersion = false))) once() returning Seq(deposit2, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'depositor/listDepositsWithStateAndDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "depositor" / "listDepositsWithStateAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user001"), stateFilter = Some(DepositStateFilter(StateLabel.ARCHIVED, SeriesFilter.LATEST))) once() returning Seq(deposit1, deposit3)
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
      repository.getDeposits _ expects DepositFilters(depositorId = Some("user001"), stateFilter = Some(DepositStateFilter(StateLabel.DRAFT, SeriesFilter.ALL))) once() returning Seq(deposit1, deposit3)
      repository.getCurrentStates _ expects Seq(depositId1, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listAllDeposits/plain.graphql' with 19 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listAllDeposits" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters() once() returning Seq(deposit1, deposit2, deposit3)
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
        repository.getCurrentCurators _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Some(curator1),
          depositId2 -> Some(curator2),
          depositId3 -> None,
        )
        (repository.getAllCurators(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Seq(curator1),
          depositId2 -> Seq(curator2, curator1),
          depositId3 -> Seq.empty,
        )
        repository.getCurrentIsNewVersionActions _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Some(isNewVersion1),
          depositId2 -> Some(isNewVersion2),
          depositId3 -> None
        )
        repository.getAllIsNewVersionActions _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Seq(isNewVersion1),
          depositId2 -> Seq(isNewVersion2),
          depositId3 -> Seq.empty
        )
        repository.getCurrentCurationRequiredActions _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Some(curationRequired1),
          depositId2 -> Some(curationRequired2),
          depositId3 -> None
        )
        repository.getAllCurationRequiredActions _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Seq(curationRequired1),
          depositId2 -> Seq(curationRequired2, curationRequired1),
          depositId3 -> Seq.empty
        )
        repository.getCurrentCurationPerformedActions _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Some(curationPerformed1),
          depositId2 -> Some(curationPerformed2),
          depositId3 -> None
        )
        repository.getAllCurationPerformedActions _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Seq(curationPerformed1),
          depositId2 -> Seq(curationPerformed2, curationPerformed1),
          depositId3 -> Seq.empty
        )
        repository.getCurrentSpringfields _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Some(springfield1),
          depositId2 -> Some(springfield2),
          depositId3 -> None,
        )
        (repository.getAllSpringfields(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Seq(springfield1),
          depositId2 -> Seq(springfield2, springfield1),
          depositId3 -> Seq.empty,
        )
        repository.getCurrentContentTypes _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Some(contentType1),
          depositId2 -> Some(contentType2),
          depositId3 -> None,
        )
        (repository.getAllContentTypes(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
          depositId1 -> Seq(contentType1),
          depositId2 -> Seq(contentType2, contentType1),
          depositId3 -> Seq.empty,
        )
      }
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listAllDepositsWithAllCurators/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listAllDepositsWithAllCurators" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters() once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getAllCurators(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Seq(curator2, curator1),
        depositId2 -> Seq(curator2, curator1),
        depositId3 -> Seq(curator2, curator1),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listAllDepositsWithAllIngestSteps/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listAllDepositsWithAllIngestSteps" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters() once() returning Seq(deposit1, deposit2, deposit3)
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
      repository.getDeposits _ expects DepositFilters() once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getAllStates(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Seq(state1, state2),
        depositId2 -> Seq(state2, state3),
        depositId3 -> Seq(state3, state1),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithContentType/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithContentType" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(contentTypeFilter = Some(DepositContentTypeFilter(ContentTypeValue.ZIP))) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getCurrentContentTypes(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Some(contentType1),
        depositId2 -> Some(contentType2),
        depositId3 -> None,
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithCurationPerformed/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithCurationPerformed" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(curationPerformedFilter = Some(DepositCurationPerformedFilter(curationPerformed = true))) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getAllCurationPerformedActions(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Seq(curationPerformed1),
        depositId2 -> Seq(curationPerformed2),
        depositId3 -> Seq.empty,
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithCurationRequired/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithCurationRequired" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(curationRequiredFilter = Some(DepositCurationRequiredFilter(curationRequired = true))) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getAllCurationRequiredActions(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Seq(curationRequired1),
        depositId2 -> Seq(curationRequired2),
        depositId3 -> Seq.empty,
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithCurator/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithCurator" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(curatorFilter = Some(DepositCuratorFilter("archie001"))) once() returning Seq(deposit1, deposit2, deposit3)
      repository.getCurrentCurators _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Some(curator1),
        depositId2 -> Some(curator2),
        depositId3 -> None,
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithDoiAction/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithDoiAction" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(doiActionFilter = Some(DepositDoiActionFilter(DoiAction.CREATE, SeriesFilter.LATEST))) once() returning Seq(deposit1, deposit2, deposit3)
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
      repository.getDeposits _ expects DepositFilters(doiRegisteredFilter = Some(DepositDoiRegisteredFilter(value = true, SeriesFilter.LATEST))) once() returning Seq(deposit1, deposit2, deposit3)
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
      repository.getDeposits _ expects DepositFilters(ingestStepFilter = Some(DepositIngestStepFilter(IngestStepLabel.VALIDATE, SeriesFilter.ALL))) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getCurrentStates(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithIsNewVersion/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithIsNewVersion" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(isNewVersionFilter = Some(DepositIsNewVersionFilter(isNewVersion = true))) once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getAllIsNewVersionActions(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Seq(isNewVersion1),
        depositId2 -> Seq(isNewVersion2),
        depositId3 -> Seq.empty,
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithState/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithState" / "plain.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters(stateFilter = Some(DepositStateFilter(StateLabel.ARCHIVED, SeriesFilter.LATEST))) once() returning Seq(deposit1, deposit3)
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
      repository.getDeposits _ expects DepositFilters(stateFilter = Some(DepositStateFilter(StateLabel.DRAFT, SeriesFilter.ALL))) once() returning Seq(deposit1, deposit3)
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
      repository.getDeposits _ expects DepositFilters(stateFilter = Some(DepositStateFilter(StateLabel.REJECTED, SeriesFilter.ALL)), ingestStepFilter = Some(DepositIngestStepFilter(IngestStepLabel.VALIDATE, SeriesFilter.ALL))) once() returning Seq(deposit1, deposit2, deposit3)
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
      repository.getDeposits _ expects DepositFilters() once() returning Seq(deposit1, deposit2, deposit3, deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'deposits/pagination/secondPage.graphql' with 1 call to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "pagination" / "secondPage.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters() once() returning Seq(deposit1, deposit2, deposit3, deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'deposits/pagination/thirdPage.graphql' with 1 call to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "pagination" / "thirdPage.graphql"

    inSequence {
      repository.getDeposits _ expects DepositFilters() once() returning Seq(deposit1, deposit2, deposit3, deposit1, deposit2)
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

  it should "resolve 'node/onContentType.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "node" / "onContentType.graphql"

    inSequence {
      repository.getContentTypeById _ expects "11" once() returning Some(contentType1)
      repository.getDepositByContentTypeId _ expects contentType1.id once() returning Some(deposit1)
      repository.getDeposits _ expects DepositFilters(contentTypeFilter = Some(DepositContentTypeFilter(ContentTypeValue.ZIP, SeriesFilter.ALL))) once() returning Seq(deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'node/onCurator.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "node" / "onCurator.graphql"

    inSequence {
      repository.getCuratorById _ expects "51" once() returning Some(curator1)
      repository.getDepositByCuratorId _ expects curator1.id once() returning Some(deposit1)
      repository.getDeposits _ expects DepositFilters(curatorFilter = Some(DepositCuratorFilter("archie001", SeriesFilter.ALL))) once() returning Seq(deposit1, deposit2)
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
      repository.getDeposits _ expects DepositFilters(ingestStepFilter = Some(DepositIngestStepFilter(IngestStepLabel.VALIDATE, SeriesFilter.ALL))) once() returning Seq(deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'node/onSpringfield.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "node" / "onSpringfield.graphql"

    inSequence {
      repository.getSpringfieldById _ expects "10" once() returning Some(springfield1)
      repository.getDepositBySpringfieldId _ expects springfield1.id once() returning Some(deposit1)
    }

    runQuery(input)
  }

  it should "resolve 'node/onState.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "node" / "onState.graphql"

    inSequence {
      repository.getStateById _ expects "15" once() returning Some(state2)
      repository.getDeposits _ expects DepositFilters(stateFilter = Some(DepositStateFilter(StateLabel.DRAFT, SeriesFilter.ALL))) once() returning Seq(deposit1, deposit3)
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
