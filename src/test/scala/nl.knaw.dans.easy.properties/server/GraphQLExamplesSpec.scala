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

import better.files.File
import better.files.File.currentWorkingDirectory
import nl.knaw.dans.easy.properties.app.repository.demo.DemoRepo
import nl.knaw.dans.easy.properties.fixture.TestSupportFixture
import org.json4s.JsonDSL._
import org.json4s.ext.UUIDSerializer
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._
import org.json4s.{ DefaultFormats, Formats }
import org.scalatest.BeforeAndAfterEach
import org.scalatra.test.EmbeddedJettyContainer
import org.scalatra.test.scalatest.ScalatraSuite

class GraphQLExamplesSpec extends TestSupportFixture
  with BeforeAndAfterEach
  with EmbeddedJettyContainer
  with ScalatraSuite {

  private val repo = new DemoRepo()
  private val servlet = DepositPropertiesGraphQLServlet(() => repo.repository)
  implicit val jsonFormats: Formats = new DefaultFormats {} + UUIDSerializer

  addServlet(servlet, "/*")

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    repo.resetRepository()
  }

  "graphQL examples" should behave like {
    val testDir = currentWorkingDirectory / "target" / "test" / getClass.getSimpleName
    val graphqlExamplesDir = testDir / "graphql"

    if (testDir.exists) testDir.delete()
    testDir.createDirectories()

    File(getClass.getResource("/graphql-examples")).copyTo(graphqlExamplesDir)

    def findJsonOutput(graphQLFile: File): File = {
      graphqlExamplesDir / graphqlExamplesDir.relativize(graphQLFile.parent).toString / s"${ graphQLFile.nameWithoutExtension }.json"
    }

    for (graphQLExample <- graphqlExamplesDir.walk()
         if graphQLExample.isRegularFile
         if graphQLExample.name.endsWith(".graphql");
         expectedJsonOutput = findJsonOutput(graphQLExample);
         relativeGraphQLPath = s"${ graphqlExamplesDir.relativize(graphQLExample.parent) }/${ graphQLExample.name }";
         relativeJsonOutputPath = s"${ graphqlExamplesDir.relativize(expectedJsonOutput.parent) }/${ expectedJsonOutput.name }") {
      it should s"check that the result of GraphQL example '$relativeGraphQLPath' is as expected in '$relativeJsonOutputPath'" in {
        assume(graphQLExample.exists, s"input file does not exist: $graphQLExample")
        assume(expectedJsonOutput.exists, s"output file does not exist: $expectedJsonOutput")

        val inputBody = compact(render("query" -> graphQLExample.contentAsString))
        val expectedOutput = writePretty(parse(expectedJsonOutput.contentAsString))

        post(uri = "/", body = inputBody.getBytes) {
          body shouldBe expectedOutput
          status shouldBe 200
        }
      }
    }
  }

  "graphQL" should "return the updated value after doing a sequence of 'query', 'mutation', 'query'" in {
    val query =
      """query {
        |  deposit(id: "00000000-0000-0000-0000-000000000001") {
        |    state {
        |      label
        |      description
        |      timestamp
        |    }
        |  }
        |}""".stripMargin
    val mutation =
      """mutation {
        |  updateState(input: {clientMutationId: "Hello Internet", depositId: "00000000-0000-0000-0000-000000000001", label: FEDORA_ARCHIVED, description: "the deposit is archived in Fedora as easy-dataset:13", timestamp: "2019-07-02T08:15:00.000+02:00"}) {
        |    clientMutationId
        |    state {
        |      label
        |      description
        |      timestamp
        |    }
        |  }
        |}""".stripMargin

    val queryBody = compact(render("query" -> query))
    val mutationBody = compact(render("query" -> mutation))

    val expectedQueryOutput1 = writePretty {
      "data" -> {
        "deposit" -> {
          "state" -> {
            ("label" -> "ARCHIVED") ~
              ("description" -> "deposit is archived") ~
              ("timestamp" -> "2019-01-01T05:05:00.000+01:00")
          }
        }
      }
    }
    val expectedMutationOutput = writePretty {
      "data" -> {
        "updateState" -> {
          ("clientMutationId" -> "Hello Internet") ~
            ("state" -> {
              ("label" -> "FEDORA_ARCHIVED") ~
                ("description" -> "the deposit is archived in Fedora as easy-dataset:13") ~
                ("timestamp" -> "2019-07-02T08:15:00.000+02:00")
            })
        }
      }
    }
    val expectedQueryOutput2 = writePretty {
      "data" -> {
        "deposit" -> {
          "state" -> {
            ("label" -> "FEDORA_ARCHIVED") ~
              ("description" -> "the deposit is archived in Fedora as easy-dataset:13") ~
              ("timestamp" -> "2019-07-02T08:15:00.000+02:00")
          }
        }
      }
    }
    
    post(uri = "/", body = queryBody.getBytes) {
      body shouldBe expectedQueryOutput1
      status shouldBe 200
    }

    post(uri = "/", body = mutationBody.getBytes) {
      body shouldBe expectedMutationOutput
      status shouldBe 200
    }
    
    post(uri = "/", body = queryBody.getBytes) {
      body shouldBe expectedQueryOutput2
      status shouldBe 200
    }
  }
}
