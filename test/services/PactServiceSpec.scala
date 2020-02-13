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

package services

import helpers.UnitSpec
import models.{MDTPService, Pact, PactWithVersion}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Results


class PactServiceSpec extends UnitSpec with MockitoSugar with GuiceOneAppPerSuite with Results {

  trait SetUp {
    val pactService: PactService = new PactService
    val pactWithVersion = new PactWithVersion(new MDTPService("Provider"), new MDTPService("Consumer"), "1.3.0", Json.arr("interactions", "abc"))
    val pactWithOlderVersion = new PactWithVersion(new MDTPService("Provider"), new MDTPService("Consumer"), "1.0.0", Json.arr("interactions", "def"))
    val pact = new Pact(new MDTPService("Provider"), new MDTPService("Consumer"), Json.arr("interactions", "abc"))
  }

  "making a pact" should {
    "create a pact when given a pactWithVersion" in new SetUp {
      val result: Pact = pactService.makePact(pactWithVersion)
      result shouldBe pact
    }
  }

  "given a list of pactWithVersion it" should {
    "return None if the list is empty" in new SetUp {
      val result: Option[PactWithVersion] = pactService.getMostRecent(List.empty)
      assert(result isEmpty)
    }
    "return the pact if only one is found" in new SetUp {
      val result: PactWithVersion = pactService.getMostRecent(List(pactWithVersion)).get
      assert(result == pactWithVersion)
    }
    "return the most recent pact if there are multiple" in new SetUp {
      val result: PactWithVersion = pactService.getMostRecent(List(pactWithVersion, pactWithOlderVersion)).get
      assert(result == pactWithVersion)
    }
  }
}
