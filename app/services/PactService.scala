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

import models.{Pact, PactWithVersion}
import play.api.Logging
import repositories.AbstractPactBrokerRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PactService @Inject() (repo: AbstractPactBrokerRepository)(implicit ec: ExecutionContext) extends Logging {

  def makePact(inputPact: PactWithVersion): Pact = Pact(inputPact.provider, inputPact.consumer, inputPact.interactions)

  def addPactTest(producerId: String, consumerId: String, pactWithVersion: PactWithVersion): Future[Either[String, Unit]] = for {
    optPact <- repo.find(consumerId, producerId, pactWithVersion.version.toString)
    result <- optPact match {
                case Some(res) if res.interactions == pactWithVersion.interactions =>
                  logger.info(s"[GG-5850] addPactTest: Identical PACT Found ${res.provider.name}/${res.consumer.name}/${res.version}")
                  Future.successful(Right(()))
                case _ =>
                  repo.add(pactWithVersion).map { result =>
                    result.fold(
                      error =>
                        logger.error(
                          s"[GG-5850] addPactTest: Error adding PACT ${pactWithVersion.provider.name}/${pactWithVersion.consumer.name}/${pactWithVersion.version}: $error"
                        ),
                      _ =>
                        logger.info(
                          s"[GG-5850] addPactTest: PACT Added ${pactWithVersion.provider.name}/${pactWithVersion.consumer.name}/${pactWithVersion.version}"
                        )
                    )
                    result
                  }
              }
  } yield result

  def getVersionedPact(producerId: String, consumerId: String, version: String): Future[Option[PactWithVersion]] =
    repo.find(consumerId, producerId, version)

  def getMostRecent(producerId: String, consumerId: String): Future[Option[PactWithVersion]] =
    repo.find(consumerId, producerId).map { pacts =>
      pacts.maxByOption(_.version)
    }
}
