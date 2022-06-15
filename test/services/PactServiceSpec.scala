/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import helpers.UnitSpec
import models.{MDTPService, Pact, PactWithVersion}
import org.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Results
import repositories.PactBrokerRepository
import org.mockito.ArgumentMatchers.{eq => eqTo}
import reactivemongo.api.commands.{DefaultWriteResult, WriteError}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PactServiceSpec extends UnitSpec with MockitoSugar with GuiceOneAppPerSuite with Results {

  trait SetUp {
    val mockRepository = mock[PactBrokerRepository]
    val pactService: PactService = new PactService(mockRepository)
    val provider = "Provider"
    val consumer = "Consumer"
    val version = "1.3.0"
    val pactWithVersion = new PactWithVersion(new MDTPService(provider), new MDTPService(consumer), "1.3.0", Json.arr("interactions", "abc"))
    val pactWithDifferentInteractions = new PactWithVersion(new MDTPService(provider), new MDTPService(consumer), "1.3.0", Json.arr("interactions", "different"))
    val pactWithOlderVersion = new PactWithVersion(new MDTPService(provider), new MDTPService(consumer), "1.0.0", Json.arr("interactions", "def"))
    val pact = new Pact(new MDTPService(provider), new MDTPService(consumer), Json.arr("interactions", "abc"))

    val successWriteResult: DefaultWriteResult = DefaultWriteResult(ok = true, n = 1, writeErrors = Seq(), None, None, Some("successWriteResult"))
    val errorWriteResult: DefaultWriteResult = DefaultWriteResult(ok = false, n = 1, writeErrors = Seq(WriteError(1, 1, "Error")), None, None, Some("errorWriteResult"))

  }


  "makePact" should {
    "create a pact when given a pactWithVersion" in new SetUp {
      val result: Pact = pactService.makePact(pactWithVersion)
      result shouldBe pact
    }
  }

  "addPactTest" should {
    "will return true when there is no existing pact in the database" in new SetUp {
      when(mockRepository.find(eqTo(consumer), eqTo(provider), eqTo(version))).thenReturn(Future.successful(None))
      when(mockRepository.add(eqTo(pactWithVersion))).thenReturn(successWriteResult)
      val result = await(pactService.addPactTest(provider, consumer, pactWithVersion))
      result shouldBe Right(true)
    }

    "will return true when there is an identical pact in the database" in new SetUp {
      when(mockRepository.find(eqTo(consumer), eqTo(provider), eqTo(version))).thenReturn(Future.successful(Some(pactWithVersion)))
      val result = await(pactService.addPactTest(provider, consumer, pactWithVersion))
      result shouldBe Right(true)
    }

    "will return true when a pact has the same provider, consumer and version but has a different body in the database" in new SetUp {
      when(mockRepository.find(eqTo(consumer), eqTo(provider), eqTo(version))).thenReturn(Future.successful(Some(pactWithDifferentInteractions)))
      when(mockRepository.add(eqTo(pactWithVersion))).thenReturn(successWriteResult)
      val result = await(pactService.addPactTest(provider, consumer, pactWithVersion))
      result shouldBe Right(true)
    }

    "will return errors when there's a problem adding the new pact into mongo" in new SetUp {
      when(mockRepository.find(eqTo(consumer), eqTo(provider), eqTo(version))).thenReturn(Future.successful(Some(pactWithDifferentInteractions)))
      when(mockRepository.add(eqTo(pactWithVersion))).thenReturn(errorWriteResult)
      val result = await(pactService.addPactTest(provider, consumer, pactWithVersion))
      result shouldBe Left("Error")
    }
  }

  "getVersionedPact" should {
    "return the pact with the corresponding version from the pact repository" in new SetUp {
      when(mockRepository.find(eqTo(consumer), eqTo(provider), eqTo(version))).thenReturn(Future.successful(Some(pactWithVersion)))
      val result: Option[PactWithVersion] = await(pactService.getVersionedPact(provider, consumer, version))
      result should contain(pactWithVersion)
    }
  }

  "getMostRecent" should {
    "return None if none are found in mongo" in new SetUp {
      when(mockRepository.find(eqTo("consumer"), eqTo("provider"))).thenReturn(Future.successful(List.empty))
      val result: Option[PactWithVersion] = await(pactService.getMostRecent("provider", "consumer"))
      assert(result isEmpty)
    }
    "return the pact if only one is found" in new SetUp {
      when(mockRepository.find(eqTo("consumer"), eqTo("provider"))).thenReturn(Future.successful(List(pactWithVersion)))
      val result: Option[PactWithVersion] = await(pactService.getMostRecent("provider", "consumer"))
      assert(result.contains(pactWithVersion))
    }
    "return the most recent pact if there are multiple" in new SetUp {
      when(mockRepository.find(eqTo("consumer"), eqTo("provider"))).thenReturn(Future.successful(List(pactWithVersion, pactWithOlderVersion)))
      val result: Option[PactWithVersion] = await(pactService.getMostRecent("provider", "consumer"))
      assert(result.contains(pactWithVersion))
    }
  }
}
