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

import org.scalatest.OptionValues
import support.BaseISpec

class PactJsonFilesExecutorISpec extends BaseISpec with OptionValues {
  import play.api.Application
  import play.api.inject.guice.GuiceApplicationBuilder

  override lazy val fakeApplication: Application = GuiceApplicationBuilder()
    .configure("pactFilesLoader.enabled" -> false)
    .build()

  "execute()" should {
    "Read parse and add in pacts from json files in conf/pacts folder" in {
      val executor = app.injector.instanceOf[PactJsonFilesExecutor]

      println(s"packFilesLoader.enabled = ${app.configuration.get[Boolean]("pactFilesLoader.enabled")}")

      val result = await(executor.executeWithLock()).value
      result.errorCount shouldBe 3
      result.successCount should be > 0
    }
  }
}
