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

import com.google.inject.Inject
import models.PactWithVersion
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.JsObjectDocumentWriter
import repositories.AbstractPactBrokerRepository.IsSuccess
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

class ReactivePactBrokerRepository @Inject() ()(implicit mongo: ReactiveMongoComponent, ec: ExecutionContext)
    extends ReactiveRepository[PactWithVersion, BSONObjectID]("pacts", mongo.mongoConnector.db, implicitly)
    with AbstractPactBrokerRepository {
  import AbstractPactBrokerRepository.WriteError

  def add(pact: PactWithVersion): Future[Either[WriteError, Unit]] = {
    collection.insert.one(pact).map(_.writeErrors.headOption.map(_.errmsg).toLeft(()))
  }

  def find(consumerId: String, providerId: String, version: String): Future[Option[PactWithVersion]] = {
    val criteria = Json.obj("consumer" -> Json.obj("name" -> consumerId), "provider" -> Json.obj("name" -> providerId), "version" -> version)

    collection.find(criteria, None).one[PactWithVersion]
  }

  def find(consumerId: String, providerId: String): Future[List[PactWithVersion]] = {
    val criteria = Json.obj("consumer" -> Json.obj("name" -> consumerId), "provider" -> Json.obj("name" -> providerId))

    collection
      .find(criteria, None)
      .cursor[PactWithVersion]()
      .collect[List](-1, Cursor.FailOnError[List[PactWithVersion]]())
  }

  def removePact(providerId: String, consumerId: String, version: String): Future[IsSuccess] = {
    val deleteBuilder = collection.delete(ordered = false)

    for {
      deleteOps <- Future.sequence(
                     Seq(
                       deleteBuilder.element(
                         q =
                           Json.obj("consumer" -> Json.obj("name" -> consumerId), "provider" -> Json.obj("name" -> providerId), "version" -> version),
                         limit     = Some(1),
                         collation = None
                       )
                     )
                   )
      deleteResult <- deleteBuilder.many(deleteOps)
    } yield deleteResult.ok
  }
}
