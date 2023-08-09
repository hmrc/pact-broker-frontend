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

import com.google.inject.AbstractModule
import config.PactBrokerConfig
import models.PactWithVersion
import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class PactJsonFilesModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[PactJsonFilesExecutor]).asEagerSingleton()
  }
}

case class PactJsonFilesExecutorResult(errorCount: Int, successCount: Int)

@Singleton
class PactJsonFilesExecutor @Inject() (
  mongoLocks:      MongoLocks,
  pactFilesLoader: PactJsonLoader,
  pactConfig:      PactBrokerConfig,
  pactService:     PactService
)(implicit executionContext: ExecutionContext)
    extends Logging {

  if (pactConfig.pactFilesLoaderEnabled) {
    mongoLocks.dbPopulationLock.tryLock[PactJsonFilesExecutorResult] {
      logger.info(s"[GG-5850] Starting pact json file loader.")
      execute()
    } map {
      case Some(result) =>
        logger.info(s"[GG-5850] Completed loading pact json files. ${result.successCount} pacts were added and ${result.errorCount} failed.")
      case None =>
        logger.warn(s"[GG-5850] Mongo locked by another instance... skipping pact json file loader.")
    } onComplete {
      case Failure(e) => logger.error(s"[GG-5850] Error while running pact json file loader.", e)
      case _          => ()
    }
  } else {
    logger.warn(s"[GG-5850] Pact files loader is disabled in config.")
  }

  def execute(): Future[PactJsonFilesExecutorResult] = {
    val pactFileLoadResults = pactFilesLoader.loadPacts()
    type ResultLists = Tuple2[List[String], List[PactWithVersion]]
    val (errors, pactsWithVersion) = pactFileLoadResults.foldLeft[ResultLists](List[String]() -> List[PactWithVersion]()) {
      (resultLists: ResultLists, result: Either[String, PactWithVersion]) =>
        result match {
          case Left(error)            => (resultLists._1 :+ error) -> resultLists._2
          case Right(pactWithVersion) => resultLists._1            -> (resultLists._2 :+ pactWithVersion)
        }
    }

    Future
      .sequence(pactsWithVersion.map { pactWithVersion =>
        pactService.addPactTest(pactWithVersion.provider.name, pactWithVersion.consumer.name, pactWithVersion).map {
          case result @ Right(_) =>
            logger.info(
              s"[GG-5850] Pact added.. provider: ${pactWithVersion.provider.name}, consumer: ${pactWithVersion.consumer.name}, version: ${pactWithVersion.version}"
            ); result
          case result @ Left(error) =>
            logger.error(
              s"[GG-5850] Failed to add Pact.. provider: ${pactWithVersion.provider.name}, consumer: ${pactWithVersion.consumer.name}, version: ${pactWithVersion.version}, error:$error"
            ); result
        }
      })
      .map(results => PactJsonFilesExecutorResult(errors.size + results.count(_.isLeft), results.count(_.isRight)))
  }

}
