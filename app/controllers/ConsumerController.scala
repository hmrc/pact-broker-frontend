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

package controllers

import javax.inject.{Inject, Singleton}
import models.{Pact, PactWithVersion}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import repositories.PactBrokerRepository
import services.PactService
import uk.gov.hmrc.play.bootstrap.controller.BackendBaseController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConsumerController @Inject()(override val controllerComponents:ControllerComponents, repo: PactBrokerRepository, pactService: PactService)(implicit ec: ExecutionContext)
  extends BackendBaseController {

  def addPactTest(producerId: String, consumerId: String, version: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Pact].fold(
      errors => Future.successful(BadRequest(errors.mkString)),
      { pactBody =>

        val pact: PactWithVersion = new PactWithVersion(pactBody.provider, pactBody.consumer, version, pactBody.interactions)

        def insertPact(pact: PactWithVersion): Future[Result] =
          repo.add(pact).map {
            case result if result.ok => Ok
            case result => InternalServerError(result.writeErrors.mkString)
          }

        for {
          exists <- repo.find(consumerId, producerId, version)
          result <- exists match {
            case Some(res) if res.interactions == pact.interactions => Future.successful(Ok)
            case _ => insertPact(pact)
          }
        } yield result
      }
    )
  }

  def getVersionedPact(producerId: String, consumerId: String, version: String): Action[AnyContent] = Action.async {
    if (!version.matches("([0-9]+[.][0-9]+[.][0-9]+)")) Future.successful(BadRequest("incorrect version format"))
    else {
      for {
        exists <- repo.find(consumerId, producerId, version)
        result <- exists match {
          case None => Future.successful(NotFound(s"no pact found for version: $version between producer: $producerId to consumer: $consumerId"))
          case Some(pact) => Future.successful(Ok(Json.toJson(pactService.makePact(pact))))
        }
      } yield result
    }
  }

  def getLatestPact(producerId: String, consumerId: String): Action[AnyContent] = Action.async {
    for {
      packList <- repo.find(consumerId, producerId)
      opwv = pactService.getMostRecent(packList)
      result <- opwv match {
        case None => Future.successful(NotFound)
        case Some(pwv) => Future.successful(Ok(Json.toJson(pactService.makePact(pwv))))
      }
    } yield result
  }

  def deletePact(producerId: String, consumerId: String, version: String) :Action[AnyContent] = Action.async {
    for {
      removeResult <- repo.removePact(producerId,consumerId,version)
      result <- if (removeResult.ok) {
        Future.successful(Ok)
      } else {
        Future.successful(NotFound)
      }
    } yield result
  }
}
