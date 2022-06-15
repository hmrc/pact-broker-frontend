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

package models

import play.api.libs.json.{JsArray, Json, OWrites, Reads}

import scala.util.matching.Regex

case class PactWithVersion(provider: MDTPService, consumer: MDTPService, version: String, interactions: JsArray) {
  require(PactWithVersion.isValid(version),s"version $version is invalid")
}

object PactWithVersion {
  implicit val reads: Reads[PactWithVersion] = Json.reads[PactWithVersion]
  implicit val writes: OWrites[PactWithVersion] = Json.writes[PactWithVersion]
  val versionRegex: Regex = "([0-9]+[.][0-9]+[.][0-9]+)".r
  def isValid(version:String): Boolean = version match {
    case versionRegex(_) => true
    case _ => false
  }
}
