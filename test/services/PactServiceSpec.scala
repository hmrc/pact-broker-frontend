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

package services

import helpers.UnitSpec
import models.{MDTPService, Pact, PactWithVersion}
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PactServiceSpec extends UnitSpec with MockitoSugar {
  trait SetUp {
    import repositories.AbstractPactBrokerRepository
    import AbstractPactBrokerRepository.WriteError

    val mockRepository: AbstractPactBrokerRepository = mock[AbstractPactBrokerRepository]
    val pactService:    PactService = new PactService(mockRepository)
    val provider = "Provider"
    val consumer = "Consumer"
    val version = "1.3.0"
    val pactWithVersion: PactWithVersion = PactWithVersion(MDTPService(provider), MDTPService(consumer), "1.3.0", Json.arr("interactions", "abc"))
    val pactWithDifferentInteractions: PactWithVersion =
      PactWithVersion(MDTPService(provider), MDTPService(consumer), "1.3.0", Json.arr("interactions", "different"))
    val pactWithOlderVersion: PactWithVersion =
      PactWithVersion(MDTPService(provider), MDTPService(consumer), "1.0.0", Json.arr("interactions", "def"))
    val pact: Pact = Pact(MDTPService(provider), MDTPService(consumer), Json.arr("interactions", "abc"))

    protected val successWriteResult: Right[WriteError, Unit] = Future.successful(Right(()))
    protected val errorWriteResult:   Left[WriteError, Unit] = Future.successful(Left("Error"))
  }

  "makePact" should {
    "create a pact when given a pactWithVersion" in new SetUp {
      val result: Pact = pactService.makePact(pactWithVersion)
      result shouldBe pact
    }
  }

  "addPactTest" should {
    "return true when there is no existing pact in the database" in new SetUp {
      when(mockRepository.find(eqTo(consumer), eqTo(provider), eqTo(version))) thenReturn Future.successful(None)
      when(mockRepository.add(eqTo(pactWithVersion))) thenReturn successWriteResult
      val result = await(pactService.addPactTest(provider, consumer, pactWithVersion))
      assert(result.isRight)
    }

    "return true when there is an identical pact in the database" in new SetUp {
      when(mockRepository.find(eqTo(consumer), eqTo(provider), eqTo(version))).thenReturn(Future.successful(Some(pactWithVersion)))
      val result = await(pactService.addPactTest(provider, consumer, pactWithVersion))
      assert(result.isRight)
    }

    "return true when a pact has the same provider, consumer and version but has a different body in the database" in new SetUp {
      when(mockRepository.find(eqTo(consumer), eqTo(provider), eqTo(version))).thenReturn(Future.successful(Some(pactWithDifferentInteractions)))
      when(mockRepository.add(eqTo(pactWithVersion))) thenReturn successWriteResult
      val result = await(pactService.addPactTest(provider, consumer, pactWithVersion))
      assert(result.isRight)
    }

    "will return errors when there's a problem adding the new pact into mongo" in new SetUp {
      when(mockRepository.find(eqTo(consumer), eqTo(provider), eqTo(version))).thenReturn(Future.successful(Some(pactWithDifferentInteractions)))
      when(mockRepository.add(eqTo(pactWithVersion))) thenReturn errorWriteResult
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
      assert(result.isEmpty)
    }
    "return the pact if only one is found" in new SetUp {
      when(mockRepository.find(eqTo("consumer"), eqTo("provider"))).thenReturn(Future.successful(List(pactWithVersion)))
      val result: Option[PactWithVersion] = await(pactService.getMostRecent("provider", "consumer"))
      assert(result contains pactWithVersion)
    }
    "return the most recent pact if there are multiple" in new SetUp {
      when(mockRepository.find(eqTo("consumer"), eqTo("provider"))).thenReturn(Future.successful(List(pactWithVersion, pactWithOlderVersion)))
      val result: Option[PactWithVersion] = await(pactService.getMostRecent("provider", "consumer"))
      assert(result contains pactWithVersion)
    }
  }
}
