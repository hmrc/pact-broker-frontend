/*
 * Copyright 2020 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.api.commands.{DefaultWriteResult, WriteError}
import repositories.PactBrokerRepository
import services.PactService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ConsumerControllerSpec extends UnitSpec with MockitoSugar with GuiceOneAppPerSuite with Results {

  trait SetUp {
    val mockPactBrokerRepository: PactBrokerRepository = mock[PactBrokerRepository]
    val pactservice: PactService = app.injector.instanceOf[PactService]
    val controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]
    val consumerController = new ConsumerController(controllerComponents, mockPactBrokerRepository, pactservice)

    val goodPact = new Pact(new MDTPService("Provider"), new MDTPService("Consumer"), Json.arr("interactions", ""))
    val badPact: JsValue = Json.toJson("""{"provider" : {"name" : "Provider"},"consumer" : {"name" : "Consumer"}}""")

    val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
    val goodRequest: FakeRequest[JsValue] = FakeRequest("PUT", "/").withBody(Json.toJson(goodPact))
    val badRequest: FakeRequest[JsValue] = FakeRequest("PUT", "/").withBody(Json.toJson(badPact))

    val goodPactWithVersion = new PactWithVersion(new MDTPService("Provider"), new MDTPService("Consumer"), "1.0.0", Json.arr("interactions", ""))
    val newPactWithVersion = new PactWithVersion(new MDTPService("Provider"), new MDTPService("Consumer"), "1.5.3", Json.arr("interactions", "this is a new pact"))
    val differentPactWithVersion = new PactWithVersion(new MDTPService("Provider"), new MDTPService("Consumer"), "1.0.0", Json.arr("interactions", "a"))

    val successWriteResult = DefaultWriteResult(ok = true, n = 1, writeErrors = Seq(), None, None, None)
    val errorWriteResult = DefaultWriteResult(ok = false, n = 1, writeErrors = Seq(WriteError(1, 1, "Error")), None, None, None)

  }

  "Submitting a pact" should {
    "create a new pact when there is no matching pact in the database" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any(), any())).thenReturn(Future.successful(None))
      when(mockPactBrokerRepository.add(any())).thenReturn(Future.successful(successWriteResult))
      val result: Future[Result] = consumerController.addPactTest("Producer", "consumer", "1.0.0")(goodRequest)
      status(result) shouldBe OK
    }

    "will return ok when there is an identical pact in the database" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any(), any())).thenReturn(Future.successful(Some(goodPactWithVersion)))
      when(mockPactBrokerRepository.add(any())).thenReturn(Future.successful(successWriteResult))
      val result: Future[Result] = consumerController.addPactTest("Producer", "consumer", "1.0.0")(goodRequest)
      status(result) shouldBe OK
    }

    "will return OK when a pact has the same provider, consumer and version but has a different body in the database" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any(), any())).thenReturn(Future.successful(Some(differentPactWithVersion)))
      when(mockPactBrokerRepository.add(any())).thenReturn(Future.successful(successWriteResult))
      val result: Future[Result] = consumerController.addPactTest("Producer", "consumer", "1.0.0")(goodRequest)
      status(result) shouldBe OK
    }

    "will return InternalServerError when a pact can not be inserted" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any(), any())).thenReturn(Future.successful(None))
      when(mockPactBrokerRepository.add(any())).thenReturn(Future.successful(errorWriteResult))
      val result: Future[Result] = consumerController.addPactTest("Producer", "consumer", "1.0.0")(goodRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "will return IllegalArgumentException when a pact can not be created due to invalid version" in new SetUp {
      val caught: IllegalArgumentException = intercept[IllegalArgumentException](await(consumerController.addPactTest("Producer", "consumer", "1.0.")(goodRequest)))
      assert(caught.getMessage contains "version 1.0. is invalid")
    }

    "will return IllegalArgumentException when a pact can not be created due to invalid body" in new SetUp {
      val result: Future[Result] = consumerController.addPactTest("Producer", "consumer", "1.0.0")(badRequest)
      status(result) shouldBe BAD_REQUEST

    }
  }

  "retrieving a pact given a version" should {
    "return a pact when given correct provider, consumer and a match is found" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any(), any())).thenReturn(Future.successful(Some(goodPactWithVersion)))
      val result: Result = await(consumerController.getVersionedPact("provider","consumer","1.0.0")(getRequest))
      status(result) shouldBe OK
      val pact: Pact = contentAsJson(result).as[Pact]
      pact shouldBe goodPact
    }

    "return a None when given correct provider, consumer but no match is found" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any(), any())).thenReturn(Future.successful(None))
      val result: Future[Result] = consumerController.getVersionedPact("provider","consumer","1.0.0")(getRequest)
      status(result) shouldBe NOT_FOUND
    }

    "return a None when given an incorrect provider" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any(), any())).thenReturn(Future.successful(None))
      val result: Future[Result] = consumerController.getVersionedPact("provider","consumer","1.0.0")(getRequest)
      status(result) shouldBe NOT_FOUND
    }

    "return a None when given an incorrect consumer" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any(), any())).thenReturn(Future.successful(None))
      val result: Future[Result] = consumerController.getVersionedPact("provider","consumer","1.0.0")(getRequest)
      status(result) shouldBe NOT_FOUND
    }

    "return a BadRequest when given an incorrect version" in new SetUp {
      val result: Future[Result] = consumerController.getVersionedPact("provider","consumer","1.0.")(getRequest)
      status(result) shouldBe BAD_REQUEST
    }
  }

  "retrieving the latest pact" should {
    "return a 404 if none are found" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any())).thenReturn(Future.successful(List()))
      val result: Future[Result] = consumerController.getLatestPact("provider","consumer")(getRequest)
      status(result) shouldBe NOT_FOUND
    }
    "return a 200 if one pact is found" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any())).thenReturn(Future.successful(List(goodPactWithVersion)))
      val result: Future[Result] = consumerController.getLatestPact("provider","consumer")(getRequest)
      status(result) shouldBe OK
    }
    "return a 200 if two pacts are found and send back the most recent version" in new SetUp {
      when(mockPactBrokerRepository.find(any(), any())).thenReturn(Future.successful(List(goodPactWithVersion,newPactWithVersion)))
      val result: Future[Result] = consumerController.getLatestPact("provider","consumer")(getRequest)
      status(result) shouldBe OK
    }
  }
}