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
import nl.knaw.dans.easy.properties.app.model.{ Deposit, State }
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
  val depositId1: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
  val depositId2: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
  val depositId3: UUID = UUID.fromString("00000000-0000-0000-0000-000000000003")
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
    label = StateLabel.SUBMITTED,
    description = "your deposit is submitted"
  )
  val state2 = State(
    label = StateLabel.DRAFT,
    description = "your deposit is in draft"
  )
  val state3 = State(
    label = StateLabel.SUBMITTED,
    description = "your deposit is submitted"
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

  "graphql" should "resolve 'deposit.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "deposit.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 returning Some(deposit1)
      repository.getStates _ expects Seq(depositId1) returning Seq(depositId1 -> Some(state1))
    }

    runQuery(input.contentAsString)
  }

  it should "resolve 'listAllDeposits/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "listAllDeposits" / "plain.graphql"

    inSequence {
      repository.getAllDeposits _ expects() returning Seq(deposit1, deposit2, deposit3)
      repository.getStates _ expects Seq(depositId1, depositId2, depositId3) returning Seq(
        depositId1 -> Some(state1),
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input.contentAsString)
  }

  it should "resolve 'listDepositsFromDepositor/plain.graphql' with 2 calls to the repository" in {
    val input = graphqlExamplesDir / "listDepositsFromDepositor" / "plain.graphql"

    inSequence {
      repository.getDepositByUserId _ expects "user002" returning Seq(deposit2, deposit3)
      repository.getStates _ expects Seq(depositId2, depositId3) returning Seq(
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input.contentAsString)
  }

  it should "resolve 'listDepositsWithSameDepositor/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "listDepositsWithSameDepositor" / "plain.graphql"
    input.writeText(input.contentAsString.replace(depositId1.toString, depositId2.toString))

    inSequence {
      repository.getDeposit _ expects depositId2 returning Some(deposit2)
      repository.getDepositByUserId _ expects "user002" returning Seq(deposit2, deposit3)
      repository.getStates _ expects Seq(depositId2, depositId3) returning Seq(
        depositId2 -> Some(state2),
        depositId3 -> Some(state3),
      )
    }

    runQuery(input.contentAsString)
  }

  it should "resolve 'listDepositsWithSameState/plain.graphql' with 3 calls to the repository" in {
    val input = graphqlExamplesDir / "listDepositsWithSameState" / "plain.graphql"

    inSequence {
      repository.getDeposit _ expects depositId1 returning Some(deposit1)
      repository.getStates _ expects Seq(depositId1) returning Seq(depositId1 -> Some(state1))
      repository.getDepositByState _ expects StateLabel.SUBMITTED returning Seq(deposit1, deposit3)
    }

    runQuery(input.contentAsString)
  }

  it should "resolve '/listDepositsWithStatelistDepositsWithState.graphql' with 1 calls to the repository" in {
    val input = graphqlExamplesDir / "listDepositsWithState" / "plain.graphql"

    inSequence {
      repository.getDepositByState _ expects StateLabel.SUBMITTED returning Seq(deposit1, deposit3)
    }

    runQuery(input.contentAsString)
  }

  private def runQuery(query: String) = {
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
