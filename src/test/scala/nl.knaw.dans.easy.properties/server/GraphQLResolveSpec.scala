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
import nl.knaw.dans.easy.properties.app.model.State.StateLabel
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId, State }
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

  "graphql" should "resolve 'deposit/findDeposit/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "findDeposit" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      repository.getCurrentState _ expects depositId1 once() returning Some(state1)
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

  it should "resolve 'deposit/listDepositsWithSameDepositor/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithSameDepositor" / "plain.graphql"
    assume(input.exists, s"input file does not exist: $input")
    val query = input.contentAsString.replace(depositId1.toString, depositId2.toString)

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId2 once() returning Some(deposit2)
      repository.getDepositsByDepositor _ expects "user002" once() returning Seq(deposit2, deposit3)
      repository.getCurrentStates _ expects Seq(depositId2, depositId3) once() returning Seq(
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(query)
  }

  it should "resolve 'deposit/listDepositsWithSameState/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithSameState" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      repository.getCurrentState _ expects depositId1 once() returning Some(state1)
      repository.getDepositsByCurrentState _ expects StateLabel.ARCHIVED once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/listDepositsWithStateFilterAll/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "listDepositsWithStateFilterAll" / "plain.graphql"

    inSequence {
      (repository.getDeposit(_: DepositId)) expects depositId1 once() returning Some(deposit1)
      repository.getCurrentState _ expects depositId1 once() returning Some(state1)
      repository.getDepositsByAllStates _ expects StateLabel.ARCHIVED once() returning Seq(deposit1, deposit3)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/statePagination/firstPage.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "statePagination" / "firstPage.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllStates(_: DepositId)) expects depositId1 once() returning Seq(state1, state2, state3, state1, state2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/statePagination/secondPage.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "statePagination" / "secondPage.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllStates(_: DepositId)) expects depositId1 once() returning Seq(state1, state2, state3, state1, state2)
    }

    runQuery(input)
  }

  it should "resolve 'deposit/statePagination/thirdPage.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit" / "statePagination" / "thirdPage.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 once() returning Some(deposit1)
      (repository.getAllStates(_: DepositId)) expects depositId1 once() returning Seq(state1, state2, state3, state1, state2)
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithDepositor" / "plain.graphql"

    inSequence {
      repository.getDepositsByDepositor _ expects "user002" once() returning Seq(deposit2, deposit3)
      repository.getCurrentStates _ expects Seq(depositId2, depositId3) once() returning Seq(
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listAllDeposits/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listAllDeposits" / "plain.graphql"

    inSequence {
      repository.getAllDeposits _ expects() once() returning Seq(deposit1, deposit2, deposit3)
      repository.getCurrentStates _ expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listAllDepositsWithAllStates/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listAllDepositsWithAllStates" / "plain.graphql"

    inSequence {
      repository.getAllDeposits _ expects() once() returning Seq(deposit1, deposit2, deposit3)
      (repository.getAllStates(_: Seq[DepositId])) expects Seq(depositId1, depositId2, depositId3) once() returning Seq(
        depositId1 -> Seq(state1, state2),
        depositId2 -> Seq(state2, state3),
        depositId3 -> Seq(state3, state1),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithState/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithState" / "plain.graphql"

    inSequence {
      repository.getDepositsByCurrentState _ expects StateLabel.ARCHIVED once() returning Seq(deposit1, deposit3)
      repository.getCurrentStates _ expects Seq(depositId1, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithStateAndDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithStateAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDepositsByDepositorAndCurrentState _ expects("user001", StateLabel.ARCHIVED) once() returning Seq(deposit1, deposit3)
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
      repository.getDepositsByAllStates _ expects StateLabel.DRAFT once() returning Seq(deposit1, deposit3)
      repository.getCurrentStates _ expects Seq(depositId1, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/listDepositsWithStateFilterAllAndDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "listDepositsWithStateFilterAllAndDepositor" / "plain.graphql"

    inSequence {
      repository.getDepositsByDepositorAndAllStates _ expects("user001", StateLabel.DRAFT) once() returning Seq(deposit1, deposit3)
      repository.getCurrentStates _ expects Seq(depositId1, depositId3) once() returning Seq(
        depositId1 -> Some(state1),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input)
  }

  it should "resolve 'deposits/pagination/firstPage.graphql' with 1 call to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "pagination" / "firstPage.graphql"

    inSequence {
      repository.getAllDeposits _ expects () once() returning Seq(deposit1, deposit2, deposit3, deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'deposits/pagination/secondPage.graphql' with 1 call to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "pagination" / "secondPage.graphql"

    inSequence {
      repository.getAllDeposits _ expects () once() returning Seq(deposit1, deposit2, deposit3, deposit1, deposit2)
    }

    runQuery(input)
  }

  it should "resolve 'deposits/pagination/thirdPage.graphql' with 1 call to the repository" in {
    val input = graphqlExamplesDir / "deposits" / "pagination" / "thirdPage.graphql"

    inSequence {
      repository.getAllDeposits _ expects () once() returning Seq(deposit1, deposit2, deposit3, deposit1, deposit2)
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

  it should "resolve 'node/onState.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "node" / "onState.graphql"

    inSequence {
      repository.getStateById _ expects "15" once() returning Some(state2)
      repository.getDepositsByAllStates _ expects StateLabel.DRAFT once() returning Seq(deposit1, deposit3)
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
