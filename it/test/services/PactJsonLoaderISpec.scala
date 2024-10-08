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

import support.BaseISpec
import models.{MDTPService, PactWithVersion}
import play.api.libs.json.JsArray

class PactJsonLoaderISpec extends BaseISpec {

  private val pactJsonLoader = app.injector.instanceOf[PactJsonLoader]

  "loadPactsFromClasspath" should {
    "load pacts, rejecting/accepting as appropriate" in {
      val results = pactJsonLoader.loadPacts()
      type ResultLists = (List[String], List[PactWithVersion])
      val (errors, pacts) = results.foldLeft[ResultLists](List[String]() -> List[PactWithVersion]()) {
        (resultLists: ResultLists, result: Either[String, PactWithVersion]) =>
          result match {
            case Left(error)            => (resultLists._1 :+ error) -> resultLists._2
            case Right(pactWithVersion) => resultLists._1            -> (resultLists._2 :+ pactWithVersion.copy(interactions = new JsArray()))
          }
      }

      pacts should contain(
        PactWithVersion(
          provider     = MDTPService("some-provider-in-jar"),
          consumer     = MDTPService("some-consumer-in-jar"),
          version      = "1.2.0",
          interactions = new JsArray()
        )
      )
      pacts should contain(
        PactWithVersion(
          provider     = MDTPService("some-provider"),
          consumer     = MDTPService("some-consumer"),
          version      = "1.0.0",
          interactions = new JsArray()
        )
      )
      pacts should contain(
        PactWithVersion(
          provider     = MDTPService("some-provider"),
          consumer     = MDTPService("some-consumer"),
          version      = "1.1.0",
          interactions = new JsArray()
        )
      )
      errors should contain("PACT JSON filename with missing/invalid version suffix - valid-pact-file-with-no-version-suffix.json")
      errors should contain("PACT JSON error in invalid-pact-file-with-version-1.0.0.json - /consumer - error.path.missing")
    }
  }
}
