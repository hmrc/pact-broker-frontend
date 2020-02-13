package controllers

import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.Json
import support.PactBrokerScISpec

class ConsumerControllerISpec extends PactBrokerScISpec{

  override def additionalConfig: Map[String, _] = Map( "application.router" -> "testOnlyDoNotUseInAppConf.Routes")

  "PUT on /pacts/provider/:producerId/consumer/:consumerId/version/:version" should {

    "return 200" when {
      "PACT in body is valid and version is correct format" in {
        withClient {
          wsClient => {
            val result = await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            result.status shouldBe OK
            val deleteResult = await(wsClient.url(resource(deleteUrl())).addHttpHeaders(contentAsJson).delete())
            deleteResult.status shouldBe OK
          }
        }
      }
    }

    "return 500" when {
      "PACT in body is valid but version is incorrect format" in {
        withClient {
          wsClient => {
            val result = await(wsClient.url(resource(putUrl("1.0"))).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            result.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }
    }

    "return 200" when {
      "PACT in body is valid but has already been added" in {
        withClient {
          wsClient => {
            await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            val result = await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            result.status shouldBe OK
            val deleteResult = await(wsClient.url(resource(deleteUrl())).addHttpHeaders(contentAsJson).delete())
            deleteResult.status shouldBe OK
          }
        }
      }
    }

    "return 200" when {
      "PACT in body is valid and a previous pact version exists" in {
        withClient {
          wsClient => {
            await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            val result = await(wsClient.url(resource(putUrl("1.1.0"))).addHttpHeaders(contentAsJson).put(Json.toJson(alternativePact)))
            result.status shouldBe OK
            val deleteResult = await(wsClient.url(resource(deleteUrl())).addHttpHeaders(contentAsJson).delete())
            deleteResult.status shouldBe OK
            val deleteAlternateResult = await(wsClient.url(resource(deleteUrl("1.1.0"))).addHttpHeaders(contentAsJson).delete())
            deleteAlternateResult.status shouldBe OK
          }
        }
      }
    }

    "return 409" when {
      "PACT in body is valid but it conflict with a pact that already exists" in {
        withClient {
          wsClient => {
            await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            val result = await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(alternativePact)))
            result.status shouldBe CONFLICT
            val deleteResult = await(wsClient.url(resource(deleteUrl())).addHttpHeaders(contentAsJson).delete())
            deleteResult.status shouldBe OK
          }
        }
      }
    }

  }


  "GET on /pacts/provider/:producerId/consumer/:consumerId/version/:version " should {

    "return 200" when {
      "a pact in the database matches the provider, consumer and version " in {
        withClient {
          wsClient => {
            await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            val result = await(wsClient.url(resource(getUrl())).addHttpHeaders(contentAsJson).get())
            result.status shouldBe OK
            val deleteResult = await(wsClient.url(resource(deleteUrl())).addHttpHeaders(contentAsJson).delete())
            deleteResult.status shouldBe OK
          }
        }
      }
    }

    "return 404" when {
      "there is no matching pact in the database" in {
        withClient {
          wsClient => {
            await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            val result = await(wsClient.url(resource(getUrl("1.1.0"))).addHttpHeaders(contentAsJson).get())
            result.status shouldBe NOT_FOUND
            val deleteResult = await(wsClient.url(resource(deleteUrl())).addHttpHeaders(contentAsJson).delete())
            deleteResult.status shouldBe OK
          }
        }
      }
    }

  }

  "GET on /pacts/provider/:producerId/consumer/:consumerId/latest " should {

    "return 200" when {
      "a pact in the database matches the provider and consumer" in {
        withClient {
          wsClient => {
            await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            val result = await(wsClient.url(resource(getLatestUrl)).addHttpHeaders(contentAsJson).get())
            result.status shouldBe OK
          }
        }
      }
    }

    "return 200 and the most recent pact" when {
      "there are multiple pacts that match" in {
        withClient {
          wsClient => {
            await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            await(wsClient.url(resource(putUrl("1.1.0"))).addHttpHeaders(contentAsJson).put(Json.toJson(alternativePact)))
            val result = await(wsClient.url(resource(getLatestUrl)).addHttpHeaders(contentAsJson).get())
            result.status shouldBe OK
            val deleteResult = await(wsClient.url(resource(deleteUrl())).addHttpHeaders(contentAsJson).delete())
            deleteResult.status shouldBe OK
            val deleteAlternateResult = await(wsClient.url(resource(deleteUrl("1.1.0"))).addHttpHeaders(contentAsJson).delete())
            deleteAlternateResult.status shouldBe OK
          }
        }
      }
    }

    "return 404" when {
      "there is no matching pact in the database" in {
        withClient {
          wsClient => {
            val result = await(wsClient.url(resource(getLatestUrl)).addHttpHeaders(contentAsJson).get())
            result.status shouldBe NOT_FOUND
          }
        }
      }
    }

  }


  "DELETE on /pacts/provider/:producerId/consumer/:consumerId/version/:version " should {

    "return 200" when {

      "deleting a pact from the database" in {
        withClient {
          wsClient => {
            await(wsClient.url(resource(putUrl())).addHttpHeaders(contentAsJson).put(Json.toJson(pact)))
            val result = await(wsClient.url(resource(getUrl())).addHttpHeaders(contentAsJson).get())
            result.status shouldBe OK
            val deleteResult = await(wsClient.url(resource(deleteUrl())).addHttpHeaders(contentAsJson).delete())
            deleteResult.status shouldBe OK
          }
        }
      }

      "there is no pact that matches to be deleted" in {
        withClient {
          wsClient => {
            val result = await(wsClient.url(resource(getUrl())).addHttpHeaders(contentAsJson).get())
            result.status shouldBe NOT_FOUND
            val deleteResult = await(wsClient.url(resource(deleteUrl())).addHttpHeaders(contentAsJson).delete())
            deleteResult.status shouldBe OK
          }
        }
      }

    }

  }

}
