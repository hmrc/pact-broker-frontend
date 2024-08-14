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

package controllers

import models.{Pact, PactWithVersion}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.AbstractPactBrokerRepository
import services.PactService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConsumerController @Inject() (
  override val controllerComponents: ControllerComponents,
  repo: AbstractPactBrokerRepository,
  pactService: PactService
)(implicit ec: ExecutionContext)
    extends BackendBaseController {

  def addPactTest(producerId: String, consumerId: String, version: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body
      .validate[Pact]
      .fold(
        errors => Future.successful(BadRequest(errors.mkString)),
        { pactBody =>

          val pactWithVersion: PactWithVersion = PactWithVersion(pactBody.provider, pactBody.consumer, version, pactBody.interactions)

          pactService.addPactTest(producerId, consumerId, pactWithVersion).map {
            case Right(_)          => Ok
            case Left(errorString) => InternalServerError(errorString)
          }
        }
      )
  }

  def getVersionedPact(producerId: String, consumerId: String, version: String): Action[AnyContent] = Action.async {
    if !version.matches("([0-9]+[.][0-9]+[.][0-9]+)") then Future.successful(BadRequest("incorrect version format"))
    else
      for {
        exists <- pactService.getVersionedPact(producerId, consumerId, version)
        result <- exists match {
                    case None =>
                      Future.successful(NotFound(s"no pact found for version: $version between producer: $producerId to consumer: $consumerId"))
                    case Some(pact) => Future.successful(Ok(Json.toJson(pactService.makePact(pact))))
                  }
      } yield result
  }

  def getLatestPact(producerId: String, consumerId: String): Action[AnyContent] = Action.async {
    pactService.getMostRecent(producerId, consumerId).map {
      case None      => NotFound
      case Some(pwv) => Ok(Json.toJson(pactService.makePact(pwv)))
    }
  }

  def deletePact(producerId: String, consumerId: String, version: String): Action[AnyContent] = Action.async {
    repo.removePact(producerId, consumerId, version) map { isSuccess =>
      if isSuccess then Ok else NotFound
    }
  }
}
