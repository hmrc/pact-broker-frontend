/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import play.api.http.{MimeTypes, Status}
import support.PactBrokerScISpec

class ConsumerControllerISpec extends PactBrokerScISpec with MimeTypes with Status {
  import play.api.libs.json.Json

  override def additionalConfig: Map[String, _] = super.additionalConfig ++ Map(
    "application.router" -> "testOnlyDoNotUseInAppConf.Routes"
  )

  withClient { wsClient =>
    def jsonRequest(path: String) =
      wsClient
        .url(s"http://localhost:$port$path")
        .addHttpHeaders("Content-Type" -> JSON)

    "PUT on /pacts/provider/:producerId/consumer/:consumerId/version/:version" should {
      "return 200" when {
        "PACT in body is valid and version is correct format" in {
          val result = await(jsonRequest(putUrl()).put(Json.toJson(pact)))
          result.status shouldBe OK
          val deleteResult = await(jsonRequest(deleteUrl()).delete())
          deleteResult.status shouldBe OK
        }
      }

      "return 500" when {
        "PACT in body is valid but version is incorrect format" in {
          val result = await(jsonRequest(putUrl("1.0")).put(Json.toJson(pact)))
          result.status shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "return 200" when {
        "PACT in body is valid but has already been added" in {
          await(jsonRequest(putUrl()).put(Json.toJson(pact)))
          val result = await(jsonRequest(putUrl()).put(Json.toJson(pact)))
          result.status shouldBe OK
          val deleteResult = await(jsonRequest(deleteUrl()).delete())
          deleteResult.status shouldBe OK
        }
      }

      "return 200" when {
        "PACT in body is valid and a previous pact version exists" in {
          await(jsonRequest(putUrl()).put(Json.toJson(pact)))
          val result = await(jsonRequest(putUrl("1.1.0")).put(Json.toJson(alternativePact)))
          result.status shouldBe OK
          val deleteResult = await(jsonRequest(deleteUrl()).delete())
          deleteResult.status shouldBe OK
          val deleteAlternateResult = await(jsonRequest(deleteUrl("1.1.0")).delete())
          deleteAlternateResult.status shouldBe OK
        }
      }

      "return 200" when {
        "PACT in body is valid but it conflict with a pact that already exists" in {
          await(jsonRequest(putUrl()).put(Json.toJson(pact)))
          val result = await(jsonRequest(putUrl()).put(Json.toJson(alternativePact)))
          result.status shouldBe OK
          val deleteResult = await(jsonRequest(deleteUrl()).delete())
          deleteResult.status shouldBe OK
        }
      }

    }

    "GET on /pacts/provider/:producerId/consumer/:consumerId/version/:version " should {
      "return 200" when {
        "a pact in the database matches the provider, consumer and version " in {
          val putResult = await(jsonRequest(putUrl()).put(Json.toJson(pact)))
          putResult.status shouldBe OK
          val result = await(jsonRequest(getUrl()).get())
          result.status shouldBe OK
          val deleteResult = await(jsonRequest(deleteUrl()).delete())
          deleteResult.status shouldBe OK
        }
      }
      "return 404" when {
        "there is no matching pact in the database" in {
          await(jsonRequest(putUrl()).put(Json.toJson(pact)))
          val result = await(jsonRequest(getUrl("1.1.0")).get())
          result.status shouldBe NOT_FOUND
          val deleteResult = await(jsonRequest(deleteUrl()).delete())
          deleteResult.status shouldBe OK
        }
      }

    }

    "GET on /pacts/provider/:producerId/consumer/:consumerId/latest " should {
      "return 200" when {
        "a pact in the database matches the provider and consumer" in {
          await(jsonRequest(putUrl()).put(Json.toJson(pact)))
          val result = await(jsonRequest(getLatestUrl).get())
          result.status shouldBe OK
        }
      }

      "return 200 and the most recent pact" when {
        "there are multiple pacts that match" in {
          await(jsonRequest(putUrl()).put(Json.toJson(pact)))
          await(jsonRequest(putUrl("1.1.0")).put(Json.toJson(alternativePact)))
          val result = await(jsonRequest(getLatestUrl).get())
          result.status shouldBe OK
          val deleteResult = await(jsonRequest(deleteUrl()).delete())
          deleteResult.status shouldBe OK
          val deleteAlternateResult = await(jsonRequest(deleteUrl("1.1.0")).delete())
          deleteAlternateResult.status shouldBe OK
        }
      }

      "return 404" when {
        "there is no matching pact in the database" in {
          val result = await(jsonRequest(getLatestUrl).get())
          result.status shouldBe NOT_FOUND
        }
      }
    }

    "DELETE on /pacts/provider/:producerId/consumer/:consumerId/version/:version " should {

      "return 200" when {

        "deleting a pact from the database" in {
          val putResult = await(jsonRequest(putUrl()).put(Json.toJson(pact)))
          putResult.status shouldBe OK
          val result = await(jsonRequest(getUrl()).get())
          result.status shouldBe OK
          val deleteResult = await(jsonRequest(deleteUrl()).delete())
          deleteResult.status shouldBe OK
        }

        "there is no pact that matches to be deleted" in {
          val result = await(jsonRequest(getUrl()).get())
          result.status shouldBe NOT_FOUND
          val deleteResult = await(jsonRequest(deleteUrl()).delete())
          deleteResult.status shouldBe NOT_FOUND
        }
      }
    }
  }
}
