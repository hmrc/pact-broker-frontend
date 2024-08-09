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

import helpers.UnitSpec
import models.{MDTPService, Pact, PactWithVersion}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.PactService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsumerControllerSpec extends UnitSpec with MockitoSugar with Results {
  trait SetUp {
    import repositories.AbstractPactBrokerRepository

    val mockPactBrokerRepository: AbstractPactBrokerRepository = mock[AbstractPactBrokerRepository]
    val mockPactService:          PactService = mock[PactService]
    val consumerController = new ConsumerController(stubControllerComponents(), mockPactBrokerRepository, mockPactService)

    val goodPact = Pact(MDTPService("Provider"), MDTPService("Consumer"), Json.arr("interactions", ""))
    val badPact: JsValue = Json.toJson("""{"provider" : {"name" : "Provider"},"consumer" : {"name" : "Consumer"}}""")

    val getRequest:  FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
    val goodRequest: FakeRequest[JsValue] = FakeRequest("PUT", "/").withBody(Json.toJson(goodPact))
    val badRequest:  FakeRequest[JsValue] = FakeRequest("PUT", "/").withBody(Json.toJson(badPact))

    val goodPactWithVersion = PactWithVersion(MDTPService("Provider"), MDTPService("Consumer"), "1.0.0", Json.arr("interactions", ""))
    val newPactWithVersion =
      PactWithVersion(MDTPService("Provider"), MDTPService("Consumer"), "1.5.3", Json.arr("interactions", "this is a new pact"))
    val differentPactWithVersion =
      PactWithVersion(MDTPService("Provider"), MDTPService("Consumer"), "1.0.0", Json.arr("interactions", "a"))
  }

  "addPactTest" should {
    "return OK when pact service successfully added the pact into the database" in new SetUp {
      when {
        mockPactService.addPactTest(eqTo("Producer"), eqTo("consumer"), eqTo(goodPactWithVersion))
      } thenReturn Future.successful(Right(()))
      val result: Future[Result] = consumerController.addPactTest("Producer", "consumer", "1.0.0")(goodRequest)
      status(result) shouldBe OK
    }

    "will return InternalServerError when a pact can not be inserted" in new SetUp {
      when {
        mockPactService.addPactTest(eqTo("Producer"), eqTo("consumer"), eqTo(goodPactWithVersion))
      } thenReturn Future.successful(Left("something went wrong"))
      val result: Future[Result] = consumerController.addPactTest("Producer", "consumer", "1.0.0")(goodRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "will return IllegalArgumentException when a pact can not be created due to invalid version" in new SetUp {
      val caught: IllegalArgumentException =
        intercept[IllegalArgumentException](await(consumerController.addPactTest("Producer", "consumer", "1.0.")(goodRequest)))
      assert(caught.getMessage contains "version 1.0. is invalid")
    }

    "will return IllegalArgumentException when a pact can not be created due to invalid body" in new SetUp {
      val result: Future[Result] = consumerController.addPactTest("Producer", "consumer", "1.0.0")(badRequest)
      status(result) shouldBe BAD_REQUEST
    }
  }

  "getVersionedPact" should {
    "return a pact when given correct provider, consumer and a match is found" in new SetUp {
      when(mockPactService.getVersionedPact(any(), any(), any())) thenReturn Future.successful(Some(goodPactWithVersion))
      when(mockPactService.makePact(goodPactWithVersion)).thenAnswer((inputPact: PactWithVersion) => {
        new Pact(inputPact.provider, inputPact.consumer, inputPact.interactions)
      })
      val result: Result = await(consumerController.getVersionedPact("provider", "consumer", "1.0.0")(getRequest))
      status(result) shouldBe OK
      val pact: Pact = contentAsJson(result).as[Pact]
      pact shouldBe goodPact
    }

    "return a None when no match is found" in new SetUp {
      when(mockPactService.getVersionedPact(any(), any(), any())) thenReturn Future.successful(None)
      val result: Future[Result] = consumerController.getVersionedPact("provider", "consumer", "1.0.0")(getRequest)
      status(result) shouldBe NOT_FOUND
    }

    "return a BadRequest when given an incorrect version" in new SetUp {
      val result: Future[Result] = consumerController.getVersionedPact("provider", "consumer", "1.0.")(getRequest)
      status(result) shouldBe BAD_REQUEST
    }
  }

  "getLatestPact" should {
    "return a 404 if none are found" in new SetUp {
      when {
        mockPactService.getMostRecent(eqTo("provider"), eqTo("consumer"))
      } thenReturn Future.successful(None)
      val result: Future[Result] = consumerController.getLatestPact("provider", "consumer")(getRequest)
      status(result) shouldBe NOT_FOUND
    }
    "return a 200 if one pact is found" in new SetUp {
      when {
        mockPactService.getMostRecent(eqTo("provider"), eqTo("consumer"))
      } thenReturn Future.successful(Some(goodPactWithVersion))
      when(mockPactService.makePact(goodPactWithVersion)).thenAnswer((inputPact: PactWithVersion) => {
        new Pact(inputPact.provider, inputPact.consumer, inputPact.interactions)
      })
      val result: Future[Result] = consumerController.getLatestPact("provider", "consumer")(getRequest)
      status(result) shouldBe OK
    }
  }
}
