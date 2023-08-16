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

package repositories

import models.MDTPService
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import support.BaseISpec

class PactBrokerRepositoryISpec extends BaseISpec with ScalaCheckPropertyChecks with OptionValues {
  import PactBrokerRepositoryISpec._

  private val repository = app.injector.instanceOf[AbstractPactBrokerRepository]

  "PackBrokerRepository" should {
    "be able to insert, retrieve, and delete a PactWithVersion" in
      forAll(pactsWithVersion) { pact =>
        val insertResult = await(repository.add(pact))
        assert(insertResult.isRight)
        val findResult = await(repository.find(pact.consumer.name, pact.provider.name, pact.version.toString))
        findResult.value shouldBe pact
        val deleteIsSuccess = await(repository.removePact(pact.provider.name, pact.consumer.name, pact.version.toString))
        assert(deleteIsSuccess)
      }
  }
}
object PactBrokerRepositoryISpec {
  import models.{PactWithVersion, Version}
  import org.scalacheck.Gen
  import play.api.libs.json.Json

  private val mdtpServices = Gen.alphaNumStr.filter(_.nonEmpty) map MDTPService.apply

  private val versions = for {
    major <- Gen.chooseNum(0, 255)
    minor <- Gen.chooseNum(0, 255)
    patch <- Gen.chooseNum(0, 255)
  } yield Version(major, minor, patch)

  private val pactsWithVersion: Gen[PactWithVersion] = for {
    provider     <- mdtpServices
    consumer     <- mdtpServices
    version      <- versions
    interactions <- Gen const Json.arr()
  } yield PactWithVersion(provider, consumer, version, interactions)
}
