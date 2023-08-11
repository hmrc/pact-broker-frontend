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

import models.{Pact, PactWithVersion}
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.io.File
import java.util.regex.Pattern
import java.util.zip.ZipFile
import javax.inject.{Inject, Singleton}
import scala.io.Source
import scala.util.{Failure, Success, Try}

@Singleton
class PactJsonLoader @Inject() extends Logging {
  import PactJsonLoader._

  import scala.jdk.CollectionConverters._

  private val jsonPactFileNameVersionRegex = raw".*-([0-9]+\.[0-9]+\.[0-9]+)\.json".r
  private val jsonPactsPackageName = "pacts"
  private val jsonPactResourcePathRegex = s".*$jsonPactsPackageName/.*\\.json".r

  /** for all elements of java.class.path get a Collection of resources Pattern pattern = Pattern.compile(".*"); gets all resources
    *
    * @param pattern
    *   the pattern to match
    * @return
    *   the resources in the order they are found
    */
  private def getResources(pattern: String): Seq[PactJsonLocation] = {
    logger.info(s"[GG-5850] Searching for resources matching $pattern")

    val resourceNamesInJar = Source.fromResource(s"$jsonPactsPackageName/").mkString.split('\n').toSeq.filter(_.endsWith("json"))
    val resourcePathsInJar = resourceNamesInJar.map(name => ResourcePath(s"$jsonPactsPackageName/$name"))
    logger.info(s"[GG-5850] Found resources with path $jsonPactsPackageName/ - ${resourceNamesInJar.map(entry => s"\n   $entry").mkString}")

    val classPath = System.getProperty("java.class.path", ".")
    val classPathElements = classPath.split(System.getProperty("path.separator")).toSet
    val classPathResources = classPathElements.flatMap(getResourcesFromClassPathElement(_, Pattern.compile(pattern))).toSeq
    val classPathResourcesNotFoundAlready =
      classPathResources.filter(_.location.split("/").reverse.headOption.fold(false)(!resourceNamesInJar.contains(_)))

    classPathResourcesNotFoundAlready ++ resourcePathsInJar
  }

  private def getResourcesFromClassPathElement(element: String, pattern: Pattern): Seq[PactJsonLocation] = {
    val file = new File(element)
    if (file.isDirectory) {
      findFilesInDirectory(file, pattern)
    } else if (file.isFile) {
      if (element.endsWith("jar")) findResourcesInJarFile(file, pattern)
      else if (element.matches(pattern.pattern())) {
        Seq(FilePath(element))
      } else {
        Seq.empty[PactJsonLocation]
      }
    } else {
      Seq.empty[PactJsonLocation]
    }
  }

  private def findResourcesInJarFile(jarFile: File, pattern: Pattern): Seq[ResourcePath] = {
    Try {
      val zipFile = new ZipFile(jarFile)
      val entries = zipFile.entries.asScala.toSeq.map(_.getName)
      val filteredEntries = entries.filter(_.matches(pattern.pattern()))
      (filteredEntries, entries)
    } match {
      case Success((found, total)) if found.nonEmpty =>
        logger.info(s"[GG-5850] ${found.size}/${total.size} entries matched in ${jarFile.getName}."); found.map(ResourcePath)
      case Success((found, _)) => found.map(ResourcePath)
      case Failure(e)          => logger.error(s"[GG-5850] Error reading file: ${jarFile.getName}", e); Seq.empty[ResourcePath]
    }
  }

  private def findFilesInDirectory(directory: File, pattern: Pattern): Seq[FilePath] = {
    val fileList = directory.listFiles.toSeq
    val files = fileList
      .filter { file =>
        val fileName = file.getCanonicalPath
        file.isFile && fileName.matches(pattern.pattern())
      }
      .map(f => FilePath(f.getCanonicalPath))
    files ++ fileList.filter(_.isDirectory).flatMap(findFilesInDirectory(_, pattern))
  }

  def loadPacts(): Seq[Either[String, PactWithVersion]] = {
    val resourcePaths = getResources(jsonPactResourcePathRegex.pattern.pattern())
    logger.info(s"[GG-5850] ${resourcePaths.size} pact json files found, parsing..")
    resourcePaths.map { resourcePath =>
      val resourceName = resourcePath.location.split("/").reverse.headOption.getOrElse(resourcePath)
      val result = (for {
        version <- resourcePath.location match {
                     case jsonPactFileNameVersionRegex(s) => Right(s)
                     case _                               => Left(s"PACT JSON filename with missing/invalid version suffix - $resourceName")
                   }
        source <- Try {
                    resourcePath match {
                      case ResourcePath(path) => logger.info(s"[GG-5850] Reading $path from jar.."); Source.fromResource(path)
                      case FilePath(path)     => logger.info(s"[GG-5850] Reading $path from file.."); Source.fromFile(path)
                    }
                  } match {
                    case Failure(e: Throwable)   => Left(s"PACT JSON could not be read - $resourceName - ${e.getMessage}")
                    case Success(s) if s == null => Left(s"PACT JSON could not be read (Source is null) - $resourceName")
                    case Success(s)              => Right(s)
                  }
        jsonText <- Try(source.mkString) match {
                      case Success(text)         => Right(text)
                      case Failure(e: Throwable) => Left(s"PACT JSON file could not be read - $resourceName. ${e.getMessage}")
                    }
        pact <- Json.parse(jsonText).validate[Pact] match {
                  case JsSuccess(pact, _) => Right(pact)
                  case JsError(errors)    => Left(s"PACT JSON error in $resourceName - ${errors.head._1} - ${errors.head._2.head.message}")
                }
      } yield {
        PactWithVersion(pact.provider, pact.consumer, version, pact.interactions)
      }) match {
        case Left(errorMessage) =>
          logger.error(errorMessage)
          Left(errorMessage)
        case Right(pactWithVersion) => Right(pactWithVersion)
      }
      result
    }
  }
}
object PactJsonLoader {
  private sealed abstract class PactJsonLocation(val location: String)
  private case class ResourcePath(entry: String) extends PactJsonLocation(entry)
  private case class FilePath(path: String)      extends PactJsonLocation(path)
}
