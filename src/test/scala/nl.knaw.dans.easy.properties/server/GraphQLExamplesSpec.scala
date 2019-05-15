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
import nl.knaw.dans.easy.properties.app.graphql.example.repository.DemoRepositoryImpl
import nl.knaw.dans.easy.properties.fixture.TestSupportFixture
import org.json4s.JsonDSL._
import org.json4s.ext.UUIDSerializer
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra.test.EmbeddedJettyContainer
import org.scalatra.test.scalatest.ScalatraSuite

class GraphQLExamplesSpec extends TestSupportFixture
  with EmbeddedJettyContainer
  with ScalatraSuite {

  private val repository = new DemoRepositoryImpl
  private val servlet = DepositPropertiesGraphQLServlet(repository)

  addServlet(servlet, "/*")

  "graphQL examples" should behave like {
    val testDir = currentWorkingDirectory / "target" / "test" / getClass.getSimpleName
    val graphqlExamplesDir = testDir / "graphql"
    implicit val jsonFormats: Formats = new DefaultFormats {} + UUIDSerializer

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

        repository.resetRepository()

        post(uri = "/", body = inputBody.getBytes) {
          body shouldBe expectedOutput
          status shouldBe 200
        }
      }
    }
  }
}
