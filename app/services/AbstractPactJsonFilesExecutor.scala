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

import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractPactJsonFilesExecutor(isEnabled: Boolean)(implicit ec: ExecutionContext) extends Logging {
  import util.Failure

  if (isEnabled) {
    executeWithLock() map {
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

  def executeWithLock(): Future[Option[PactJsonFilesExecutorResult]]
}
