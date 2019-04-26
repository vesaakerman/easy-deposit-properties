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

import nl.knaw.dans.easy.properties.app.graphql.example.repository.DemoRepositoryImpl
import nl.knaw.dans.easy.properties.fixture.TestSupportFixture
import org.scalatra.test.EmbeddedJettyContainer
import org.scalatra.test.scalatest.ScalatraSuite

class GraphQLServletSpec extends TestSupportFixture with EmbeddedJettyContainer with ScalatraSuite {

  private val repository = new DemoRepositoryImpl
  private val servlet = new GraphQLServlet(repository)

  addServlet(servlet, "/*")

  "POST GraphQL route" should "list the id's of all deposits" in {
    val query =
      """query {
        |  deposits {
        |    id
        |  }
        |}""".stripMargin
    val inputBody = s"""{"query": "$query"}"""

    post(uri = "/", body = inputBody.getBytes) {
      body shouldBe
        """{
          |  "data":{
          |    "deposits":[
          |      {
          |        "id":"00000000-0000-0000-0000-000000000002"
          |      },
          |      {
          |        "id":"00000000-0000-0000-0000-000000000005"
          |      },
          |      {
          |        "id":"00000000-0000-0000-0000-000000000004"
          |      },
          |      {
          |        "id":"00000000-0000-0000-0000-000000000001"
          |      },
          |      {
          |        "id":"00000000-0000-0000-0000-000000000003"
          |      }
          |    ]
          |  }
          |}""".stripMargin
      status shouldBe 200
    }
  }
  
  it should "list depositorId, creationDate and full state for a single deposit" in {
    val query =
      """query {
        |  deposit(id: \"00000000-0000-0000-0000-000000000001\") {
        |    depositor {
        |      depositorId
        |    }
        |    creationTimestamp
        |    state {
        |      label
        |      description
        |    }
        |  }
        |}""".stripMargin
    val inputBody = s"""{"query": "$query"}"""

    post(uri = "/", body = inputBody.getBytes) {
      body shouldBe
        """{
          |  "data":{
          |    "deposit":{
          |      "depositor":{
          |        "depositorId":"user001"
          |      },
          |      "creationTimestamp":"2019-04-22T15:58:00.000+02:00",
          |      "state":{
          |        "label":"SUBMITTED",
          |        "description":"await processing"
          |      }
          |    }
          |  }
          |}""".stripMargin
      status shouldBe 200
    }
  }
  
  it should "list the depositIds of all deposits with the same state as the state of the given depositid" in {
    val query =
      """query {
        |  deposit(id: \"00000000-0000-0000-0000-000000000001\") {
        |    state {
        |      deposit {
        |        id
        |      }
        |    }
        |  }
        |}""".stripMargin
    val inputBody = s"""{"query": "$query"}"""

    post(uri = "/", body = inputBody.getBytes) {
      body shouldBe
        """{
          |  "data":{
          |    "deposit":{
          |      "state":{
          |        "deposit":[
          |          {
          |            "id":"00000000-0000-0000-0000-000000000005"
          |          },
          |          {
          |            "id":"00000000-0000-0000-0000-000000000004"
          |          },
          |          {
          |            "id":"00000000-0000-0000-0000-000000000001"
          |          }
          |        ]
          |      }
          |    }
          |  }
          |}""".stripMargin
      status shouldBe 200
    }
  }
}
