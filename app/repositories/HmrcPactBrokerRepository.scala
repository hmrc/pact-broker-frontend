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

package repositories

import com.google.inject.{Inject, Singleton}
import models.PactWithVersion
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.model.Indexes.ascending
import repositories.AbstractPactBrokerRepository._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HmrcPactBrokerRepository @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[PactWithVersion](
      mongoComponent,
      collectionName,
      implicitly,
      Seq(
        IndexModel(ascending("provider.name", "consumer.name", "version"))
      )
    )
    with AbstractPactBrokerRepository {
  import org.mongodb.scala.model.Filters.{and, equal}

  def add(pact: PactWithVersion): Future[Either[WriteError, Unit]] =
    collection
      .insertOne(pact)
      .toFuture()
      .map(_ => Right(()))
      .recover { case ex => Left(ex.toString) }

  def find(consumerId: String, providerId: String, version: String): Future[Option[PactWithVersion]] =
    collection
      .find(
        and(
          equal("consumer.name", consumerId),
          equal("provider.name", providerId),
          equal("version", version)
        )
      )
      .headOption()

  def find(consumerId: String, providerId: String): Future[Seq[PactWithVersion]] =
    collection
      .find(
        and(
          equal("consumer.name", consumerId),
          equal("provider.name", providerId)
        )
      )
      .toFuture()

  def removePact(providerId: String, consumerId: String, version: String): Future[IsSuccess] =
    collection
      .findOneAndDelete(
        and(
          equal("consumer.name", consumerId),
          equal("provider.name", providerId),
          equal("version", version)
        )
      )
      .toFuture()
      .map(_ != null)
}
