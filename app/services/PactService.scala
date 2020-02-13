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

package services

import models.{Pact, PactWithVersion}

class PactService {

  def makePact(inputPact: PactWithVersion): Pact = {
    new Pact(inputPact.provider, inputPact.consumer, inputPact.interactions)
  }

  def getMostRecent(pactList: List[PactWithVersion]): Option[PactWithVersion] = {
    pactList.length match {
      case 0 => None
      case 1 => Some(pactList.head)
      case _ => Some(pactList.reduceLeft[PactWithVersion]((x, y) => findMostRecentOutOfTwo(x, y)))
    }
  }

  private def findMostRecentOutOfTwo(pactWithVersion1: PactWithVersion, pactWithVersion2: PactWithVersion): PactWithVersion = {
    val pact1Version: Array[Int] = pactWithVersion1.version.split("\\.").map(a => a.toInt)
    val pact2Version = pactWithVersion2.version.split("\\.").map(a => a.toInt)
    val normalisedDiff = pact1Version.zip(pact2Version).map { case (a, b) => a - b }.map {
      case x if x > 0 => 1
      case x if x < 0 => -1
      case _ => 0
    }
    normalisedDiff(0) match {
      case 1 => pactWithVersion1
      case -1 => pactWithVersion2
      case _ =>
        normalisedDiff(1) match {
          case 1 => pactWithVersion1
          case -1 => pactWithVersion2
          case _ =>
            normalisedDiff(2) match {
              case 1 => pactWithVersion1
              case -1 => pactWithVersion2
              case _ => pactWithVersion2
            }
        }
    }
  }
}
