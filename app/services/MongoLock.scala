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

import org.joda.time.Duration
import play.api.Logging
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DB
import uk.gov.hmrc.lock.{LockKeeper, LockMongoRepository, LockRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


class MongoLock(db: () => DB, lockId_ : String) extends LockKeeper with Logging{

  override val forceLockReleaseAfter:Duration = Duration.standardMinutes(5)

  override def repo: LockRepository = LockMongoRepository(db)

  override def lockId: String = lockId_

  override def tryLock[T](body: => Future[T])(implicit ec : ExecutionContext): Future[Option[T]] = {
    tryLockWithMaxDuration(body, forceLockReleaseAfter)
  }

  def tryLockWithMaxDuration[T](body: => Future[T], releaseLockAfter:Duration)(implicit ec : ExecutionContext): Future[Option[T]] = {
    repo.lock(lockId, serverId, releaseLockAfter)
      .flatMap { acquired =>
        if (acquired) {
          logger.info(s"Mongo lock acquired with id $lockId_.")
          body.flatMap { x =>
            logger.info(s"Process requiring mongo lock with id $lockId_ completed, releasing lock.")
            repo.releaseLock(lockId, serverId).map(_ => Some(x))
          }
        }
        else {
          logger.info(s"Mongo lock with id $lockId_ could not be acquired.")
          Future.successful(None)
        }
      }.recoverWith { case ex =>
        logger.error(s"Process requiring mongo lock with id $lockId_ failed: ${ex.getMessage}, releasing lock.", ex)
        repo.releaseLock(lockId, serverId).flatMap(_ => Future.failed(ex))
      }
  }
}


@Singleton
class MongoLocks @Inject()(mongo: ReactiveMongoComponent) {
  private val db = mongo.mongoConnector.db
  val dbPopulationLock = new MongoLock(db, "db-population-job")
}
