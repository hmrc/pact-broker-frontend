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

package support

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, WsTestClient}

trait BaseISpec extends AnyWordSpec with should.Matchers with GuiceOneServerPerSuite with WsTestClient with FutureAwaits with DefaultAwaitTimeout {
  import play.api.Application
  import play.api.inject.guice.GuiceApplicationBuilder

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(additionalConfig)
    .build()

  def additionalConfig: Map[String, _] = Map("pactFilesLoader.enabled" -> false)
}
