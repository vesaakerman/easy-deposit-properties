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
import nl.knaw.dans.easy.properties.app.graphql.example.repository.DemoRepositoryImpl
import nl.knaw.dans.easy.properties.fixture.{ FileSystemSupport, TestSupportFixture }
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatra.test.EmbeddedJettyContainer
import org.scalatra.test.scalatest.ScalatraSuite

class GraphQLServletSpec extends TestSupportFixture
  with FileSystemSupport
  with TableDrivenPropertyChecks
  with EmbeddedJettyContainer
  with ScalatraSuite {

  private val graphqlExamplesDir = testDir / "graphql"
  private val repository = new DemoRepositoryImpl
  private val servlet = DepositPropertiesGraphQLServlet(repository)

  addServlet(servlet, "/*")

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    File(getClass.getResource("/graphql-examples"))
      .copyTo(graphqlExamplesDir)
  }

  "GraphQL endpoint" should "run all examples defined in the test resources" in {
    val examples = Table(
      "input" -> "output",
      graphqlExamplesDir.list(_.name endsWith ".graphql")
        .map(input => input -> graphqlExamplesDir / s"${ input.nameWithoutExtension }.json")
        .toList: _*
    )

    forEvery(examples) { (input, output) =>
      assume(input.exists, s"input file does not exist: $input")
      assume(output.exists, s"output file does not exist: $output")

      val query = input.contentAsString.stripLineEnd.replace("\"", "\\\"") // " -> \"

      val inputBody =
        s"""{"query": "$query"}"""
      val expectedOutput = output.contentAsString.stripLineEnd.replaceAll(": ", ":") // remove some formatting

      post(uri = "/", body = inputBody.getBytes) {
        body shouldBe expectedOutput
        status shouldBe 200
      }
    }
  }
}
