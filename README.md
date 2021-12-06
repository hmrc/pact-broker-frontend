
# pact-broker-frontend

##Pact Broker
A pact broker allows many consumers of a provider service to store pacts (json describing a consumer request and expected provider response) in a central location.
A producer can then read these pacts to verify its responses match consumers expectations.

This prevents the teams responsible for the consumer services having to send updated pacts to the provider services teams to update their projects or raising pull requests against the services.

##Sbt with pact broker
Consuming services can generate pact files and push them to the pact broker server with the **`sbt pactPush`** command.

A service that wishes to utilise the **`sbt pactPush`**  command will require a file called pact.sbt that contains  **`pact broker host environment`** variables: 
* pactBrokerAddress
* allowSnapshotPublish
* pactContractVersion 

For an example of how to generate pacts, configure pact.sbt and push pacts to the broker see [auth-team-contract-tests-maker](https://github.com/hmrc/auth-team-contract-tests-maker)

##Pre-loaded pacts
Pact json files in this projects conf/pacts folder (with a valid version suffix in the filename) will be automatically stored in the database when the service starts up.

## How to build
uses default JVM settings.
- ```mongoDB```  must be running
- ```smserver``` needs to be running for it:test

```sbtshell
sbt 'run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
```
## API

    | Path                                                                                        | Supported Methods | Description                                              |
    |---------------------------------------------------------------------------------------------|-------------------|----------------------------------------------------------|
    | /pact-broker/pacts/provider/:producerId/consumer/:consumerId/version/:version            | PUT               | inserts a pact into the database                         |
    | /pact-broker/pacts/provider/:producerId/consumer/:consumerId/version/:version            | GET               | retrieve a specified pact between two services           |
    | /pact-broker/pacts/provider/:producerId/consumer/:consumerId/latest                      | GET               | retrieve the most up to date pact between two services   |
    | /pact-broker/test-only/pacts/provider/:producerId/consumer/:consumerId/version/:version  | DELETE            | removes a pact from the database                         |

## PUT /pact-request

Create a new pact

**Request body**

   ```json
{
  "provider" : {
    "name" : "bas-proxy"
  },
  "consumer" : {
    "name" : "auth-contract-tests"
  },
  "interactions" : [
    {
      "request" : {
        "method" : "POST",
        "body" : {
          "credId" : "a-cred-id",
          "email" : "default@example.com"
        },
        "path" : "/bas-proxy/resetPassword",
        "matchingRules" : {
          "$.body.credId" : {
            "match" : "regex",
            "regex" : "^([a-zA-Z0-9-]+)$"
          },
          "$.body.email" : {
            "match" : "regex",
            "regex" : "(?=[^\\s]+)"
          }
        },
        "headers" : {
          "Content-Type" : "application/json"
        }
      },
      "description" : "reset password",
      "response" : {
        "status" : 204
      }
    },
    {
      "request" : {
        "method" : "GET",
        "path" : "/bas-proxy/credentials/cred-id-1234",
        "matchingRules" : {
          "$.body.credId" : {
            "match" : "regex",
            "regex" : "^([a-zA-Z0-9-]+)$"
          }
        },
        "headers" : {
          "Content-Type" : "application/json"
        }
      },
      "description" : "get account info",
      "response" : {
        "status" : 200,
        "body" : {
          "name" : "TestName",
          "emailVerified" : true,
          "email" : "default@example.com",
          "suspended" : false,
          "groupId" : "FISH",
          "roles" : [
            "Administrator",
            "User"
          ],
          "sub" : "cred-id-1234",
          "trustId" : "missing trust id"
        },
        "matchingRules" : {
          "$.body.groupId" : {
            "match" : "regex",
            "regex" : "^([a-zA-Z0-9-]+)$"
          },
          "$.body.email" : {
            "match" : "regex",
            "regex" : "(?=[^\\s]+)(?=(\\w+)@([\\w\\.]+))"
          },
          "$.body.emailVerified" : {
            "match" : "regex",
            "regex" : "true|false"
          },
          "$.body.credId" : {
            "match" : "regex",
            "regex" : "^([a-zA-Z0-9-]+)$"
          },
          "$.body.suspended" : {
            "match" : "regex",
            "regex" : "true|false"
          }
        }
      }
    },
    {
      "request" : {
        "method" : "POST",
        "body" : {
          "credId" : "a-cred-id",
          "email" : "default@example.com"
        },
        "path" : "/bas-proxy/resendUserId",
        "matchingRules" : {
          "$.body.credId" : {
            "match" : "regex",
            "regex" : "^([a-zA-Z0-9-]+)$"
          },
          "$.body.email" : {
            "match" : "regex",
            "regex" : "(?=[^\\s]+)(?=(\\w+)@([\\w\\.]+))"
          }
        },
        "headers" : {
          "Content-Type" : "application/json"
        }
      },
      "description" : "resend user id",
      "response" : {
        "status" : 204
      }
    }
  ]
}
```

### Success Response

    | Status    |  Description                      |
    |-----------|-----------------------------------|
    | 200       | pact was created                  |
    
### Failure Responses
    
    | Status    |  Description                                  |  Code                    |  Note                    |
    |-----------|-----------------------------------------------|--------------------------|--------------------------|
    | 400       | Invalid request                               | VALIDATION_ERROR         |                          |
    | 409       | pact conflicts with an already existing pact  | CONFLICT                 |                          |
    | 500       | Unexpected error                              | UNEXPECTED_ERROR         |                          |


## GET

### Success Response

    | Status    |  Description                      |
    |-----------|-----------------------------------|
    | 200       | pact was found                    |
    
### Failure Responses
    
    | Status    |  Description                                  |  Code                    |  Note                    |
    |-----------|-----------------------------------------------|--------------------------|--------------------------|
    | 404       | requested pact  not found                     | NOT_FOUND                |                          |
    | 400       | bad pact request                              | BAD_REQUEST              |                          |

## DELETE 

### Success Response

    | Status    |  Description                      |
    |-----------|-----------------------------------|
    | 200       | pact was found                    |
    
### Failure Responses
    
    | Status    |  Description                                  |  Code                    |  Note                    |
    |-----------|-----------------------------------------------|--------------------------|--------------------------|
    | 404       | mongoDB not found                             | NOT_FOUND                |                          |


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
