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
import play.api.libs.json.{Format, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.Cursor
import reactivemongo.api.commands.{MultiBulkWriteResult, WriteResult}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.JsObjectDocumentWriter
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}


class PactBrokerRepository @Inject() () (implicit mongo: ReactiveMongoComponent, ec: ExecutionContext)
  extends ReactiveRepository[PactWithVersion, BSONObjectID]("pacts", mongo.mongoConnector.db,PactBrokerFormats.pactBrokerFormat) {

  def add(pact: PactWithVersion):Future[WriteResult] = {
    collection.insert.one(pact)
  }

  def find(consumerId: String, providerId: String, version:String):Future[Option[PactWithVersion]] = {
    val criteria = Json.obj("consumer" -> Json.obj("name" -> consumerId),
      "provider" -> Json.obj("name" -> providerId),
      "version" -> version)

    collection.find(criteria,None).one[PactWithVersion]
  }

  def find(consumerId: String, providerId: String):Future[List[PactWithVersion]] = {
    val criteria = Json.obj("consumer" -> Json.obj("name" -> consumerId),
      "provider" -> Json.obj("name" -> providerId))

    collection.find(criteria,None)
      .cursor[PactWithVersion]()
      .collect[List](-1,Cursor.FailOnError[List[PactWithVersion]]())
  }

  def removePact(providerId: String, consumerId: String, version:String):Future[MultiBulkWriteResult] ={
    val deleteBuilder = collection.delete(ordered = false)

    val deletes = Future.sequence(Seq(
      deleteBuilder.element(
        q = Json.obj("consumer" -> Json.obj("name" -> consumerId),
          "provider" -> Json.obj("name" -> providerId),
          "version" -> version),
        limit = Some(1),
        collation = None)))

    deletes.flatMap { ops => deleteBuilder.many(ops) }  }
}

object PactBrokerFormats {
  implicit val pactBrokerFormat : Format[PactWithVersion] = Json.format[PactWithVersion]
}
