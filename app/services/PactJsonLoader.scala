/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{Pact, PactWithVersion}
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, Json}

import javax.inject.{Inject, Singleton}
import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

@Singleton
class PactJsonLoader @Inject()(config: PactBrokerConfig) extends Logging {

  val jsonPactFileNameVersionRegex:Regex = raw".*-([0-9]+\.[0-9]+\.[0-9]+)\.json".r

  def loadPacts(): List[Either[String, PactWithVersion]] = {
    val jsonFiles = config.pactFilesFolder.listFiles.filter(f => f.isFile && f.getName.endsWith(".json")).toList
    jsonFiles.map { file =>
      val result:Either[String, PactWithVersion] = (for {
        version <- file.getName match {
          case jsonPactFileNameVersionRegex(s) => Right(s)
          case _ => Left(s"PACT JSON filename with missing/invalid version suffix - ${file.getName}")
        }
        source <- Try(Source.fromFile(file, "UTF-8")) match {
          case Success(s) => Right(s)
          case Failure(e: Throwable)  => Left(s"PACT JSON file could not be read - ${file.getName} - ${e.getMessage}")
        }
        pact <- Json.parse(source.mkString).validate[Pact] match {
          case JsSuccess(pact, _) => Right(pact)
          case JsError(errors) => Left(s"PACT JSON error in ${file.getName} - ${errors.head._1} - ${errors.head._2.head.message}")
        }
      } yield {
        PactWithVersion(pact.provider, pact.consumer, version, pact.interactions)
      }) match {
        case Left(errorMessage) => {
          logger.error(errorMessage)
          Left(errorMessage)
        }
        case Right(pactWithVersion) => Right(pactWithVersion)
      }
      result
    }
  }
}
