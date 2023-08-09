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

package support

import models.{MDTPService, Pact}
import play.api.libs.json.{JsArray, Json}

class PactBrokerScISpec extends BaseISpec {

  val provider: String = "ProviderService"
  val consumer: String = "ConsumerService"

  def putUrl(version: String = "1.0.0"): String = s"/pact-broker/pacts/provider/$provider/consumer/$consumer/version/$version"
  def getUrl(version: String = "1.0.0"): String = s"/pact-broker/pacts/provider/$provider/consumer/$consumer/version/$version"
  def getLatestUrl: String = s"/pact-broker/pacts/provider/$provider/consumer/$consumer/latest"
  def deleteUrl(version: String = "1.0.0"): String = s"/pact-broker/pacts/provider/$provider/consumer/$consumer/version/$version"

  lazy val pact: Pact = new Pact(new MDTPService("ProviderService"), new MDTPService("ConsumerService"), Json.parse(jsonInteraction).as[JsArray])
  lazy val alternativePact: Pact =
    new Pact(new MDTPService("ProviderService"), new MDTPService("ConsumerService"), Json.parse(alternativeJsonInteraction).as[JsArray])

  val jsonInteraction: String =
    """
      |  [
      |    {
      |      "request" : {
      |        "method" : "POST",
      |        "body" : {
      |          "credId" : "a-cred-id",
      |          "email" : "default@example.com"
      |        },
      |        "path" : "/bas-proxy/resetPassword",
      |        "matchingRules" : {
      |          "$.body.credId" : {
      |            "match" : "regex",
      |            "regex" : "^([a-zA-Z0-9-]+)$"
      |          },
      |          "$.body.email" : {
      |            "match" : "regex",
      |            "regex" : "(?=[^\\s]+)(?=(\\w+)@([\\w\\.]+))"
      |          }
      |        },
      |        "headers" : {
      |          "Content-Type" : "application/json"
      |        }
      |      },
      |      "description" : "reset password",
      |      "response" : {
      |        "status" : 204
      |      }
      |    },
      |    {
      |      "request" : {
      |        "method" : "GET",
      |        "path" : "/bas-proxy/credentials/cred-id-1234",
      |        "matchingRules" : {
      |          "$.body.credId" : {
      |            "match" : "regex",
      |            "regex" : "^([a-zA-Z0-9-]+)$"
      |          }
      |        },
      |        "headers" : {
      |          "Content-Type" : "application/json"
      |        }
      |      },
      |      "description" : "get account info",
      |      "response" : {
      |        "status" : 200,
      |        "body" : {
      |          "name" : "TestName",
      |          "emailVerified" : true,
      |          "email" : "default@example.com",
      |          "suspended" : false,
      |          "groupId" : "9F9416A1-3977-4FC1-AB5E-0352417FD5B7",
      |          "roles" : [
      |            "Administrator",
      |            "User"
      |          ],
      |          "sub" : "cred-id-1234",
      |          "trustId" : "missing trust id"
      |        },
      |        "matchingRules" : {
      |          "$.body.groupId" : {
      |            "match" : "regex",
      |            "regex" : "^([a-zA-Z0-9-]+)$"
      |          },
      |          "$.body.email" : {
      |            "match" : "regex",
      |            "regex" : "(?=[^\\s]+)(?=(\\w+)@([\\w\\.]+))"
      |          },
      |          "$.body.emailVerified" : {
      |            "match" : "regex",
      |            "regex" : "true|false"
      |          },
      |          "$.body.credId" : {
      |            "match" : "regex",
      |            "regex" : "^([a-zA-Z0-9-]+)$"
      |          },
      |          "$.body.suspended" : {
      |            "match" : "regex",
      |            "regex" : "true|false"
      |          }
      |        }
      |      }
      |    },
      |    {
      |      "request" : {
      |        "method" : "POST",
      |        "body" : {
      |          "credId" : "a-cred-id",
      |          "email" : "default@example.com"
      |        },
      |        "path" : "/bas-proxy/resendUserId",
      |        "matchingRules" : {
      |          "$.body.credId" : {
      |            "match" : "regex",
      |            "regex" : "^([a-zA-Z0-9-]+)$"
      |          },
      |          "$.body.email" : {
      |            "match" : "regex",
      |            "regex" : "(?=[^\\s]+)(?=(\\w+)@([\\w\\.]+))"
      |          }
      |        },
      |        "headers" : {
      |          "Content-Type" : "application/json"
      |        }
      |      },
      |      "description" : "resend user id",
      |      "response" : {
      |        "status" : 204
      |      }
      |    }
      |  ]
      |""".stripMargin

  val alternativeJsonInteraction: String =
    """
      |  [
      |    {
      |      "request" : {
      |        "method" : "POST",
      |        "body" : {
      |          "credId" : "a-cred-id",
      |          "email" : "default@example.com"
      |        },
      |        "path" : "/bas-proxy/resetPassword",
      |        "matchingRules" : {
      |          "$.body.credId" : {
      |            "match" : "regex",
      |            "regex" : "^([BASIC REGEX])$"
      |          },
      |          "$.body.email" : {
      |            "match" : "regex",
      |            "regex" : "([BASIC REGEX])"
      |          }
      |        },
      |        "headers" : {
      |          "Content-Type" : "application/json"
      |        }
      |      },
      |      "description" : "reset password",
      |      "response" : {
      |        "status" : 204
      |      }
      |    },
      |    {
      |      "request" : {
      |        "method" : "GET",
      |        "path" : "/bas-proxy/credentials/cred-id-1234",
      |        "matchingRules" : {
      |          "$.body.credId" : {
      |            "match" : "regex",
      |            "regex" : "^([BASIC REGEX])$"
      |          }
      |        },
      |        "headers" : {
      |          "Content-Type" : "application/json"
      |        }
      |      },
      |      "description" : "get account info",
      |      "response" : {
      |        "status" : 200,
      |        "body" : {
      |          "name" : "TestName",
      |          "emailVerified" : true,
      |          "email" : "default@example.com",
      |          "suspended" : false,
      |          "groupId" : "9F9416A1-3977-4FC1-AB5E-0352417FD5B7",
      |          "roles" : [
      |            "Administrator",
      |            "User"
      |          ],
      |          "sub" : "cred-id-1234",
      |          "trustId" : "missing trust id"
      |        },
      |        "matchingRules" : {
      |          "$.body.groupId" : {
      |            "match" : "regex",
      |            "regex" : "^([BASIC REGEX])$"
      |          },
      |          "$.body.email" : {
      |            "match" : "regex",
      |            "regex" : "([BASIC REGEX])"
      |          },
      |          "$.body.emailVerified" : {
      |            "match" : "regex",
      |            "regex" : "true|false"
      |          },
      |          "$.body.credId" : {
      |            "match" : "regex",
      |            "regex" : "^([BASIC REGEX])$"
      |          },
      |          "$.body.suspended" : {
      |            "match" : "regex",
      |            "regex" : "true|false"
      |          }
      |        }
      |      }
      |    },
      |    {
      |      "request" : {
      |        "method" : "POST",
      |        "body" : {
      |          "credId" : "a-cred-id",
      |          "email" : "default@example.com"
      |        },
      |        "path" : "/bas-proxy/resendUserId",
      |        "matchingRules" : {
      |          "$.body.credId" : {
      |            "match" : "regex",
      |            "regex" : "^([BASIC REGEX])$"
      |          },
      |          "$.body.email" : {
      |            "match" : "regex",
      |            "regex" : "([BASIC REGEX])"
      |          }
      |        },
      |        "headers" : {
      |          "Content-Type" : "application/json"
      |        }
      |      },
      |      "description" : "resend user id",
      |      "response" : {
      |        "status" : 204
      |      }
      |    }
      |  ]
      |""".stripMargin
}
