/*
 * Copyright 2023 HM Revenue & Customs
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

import config.PactBrokerConfig
import helpers.UnitSpec
import org.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results

import scala.concurrent.ExecutionContext.Implicits.global

class PactJsonFilesModuleSpec extends UnitSpec with MockitoSugar with GuiceOneAppPerSuite with Results {

  "execute()" should {
    "Read parse and add in pacts from json files in conf/pacts folder" in {
      val mongoLocks:      MongoLocks = app.injector.instanceOf[MongoLocks]
      val pactFilesLoader: PactJsonLoader = app.injector.instanceOf[PactJsonLoader]
      val pactConfig:      PactBrokerConfig = mock[PactBrokerConfig]
      when(pactConfig.pactFilesLoaderEnabled).thenReturn(false)
      val pactService: PactService = app.injector.instanceOf[PactService]
      val executor = new PactJsonFilesExecutor(mongoLocks, pactFilesLoader, pactConfig, pactService)

      val result = await(executor.execute())
      result.errorCount shouldBe 3
      result.successCount should be > 0
    }
  }
}
