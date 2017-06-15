Operations manual
=================

# Configuration

You need to set the `SPRING_APPLICATION_JSON` environment variable with the JSON configuration, such as in the following example:

```bash
export SPRING_APPLICATION_JSON='{"server":{"address":"127.0.0.1", "port": 8080}, "logging":{"file":"morpheus-spring.log"}, "morpheus":{"http":{"user":"cassio", "password":"god"}, "handlerPrivateKey": "XXXX", "handlerPublicKey": "XXXX", "useSSL": true, "IDOS_DEBUG": 1, "IDOS_API_URL": "https://api.idos.io/1.0"}, "aws": {"ACCESS_KEY": "XXXX", "SECRET": "XXXX"}}'
```

* The `server` variable controls the `address` and `port` where the service will run.
* The `logging` variable indicates the path of the logging file.
* The `morpheus` variable contains:
    * `http`: basic AUTH credentials;
    * `handlerPrivateKey`: which should be registered with `idOS`;
    * `handlerPublicKey`: which has the public key registered with `idOS`;
    * `useSSL`: indicates whether SSL checking happens when calling the `idOS` API;
    * `IDOS_DEBUG`: indicates whether the requests to `idOS` should be printed to stdout;
    * `IDOS_API_URL`: indicates the URL to connect to the `idOS` API.
* The `aws` variable contains:
    * `ACCESS_KEY`: access key for using the Amazon Rekognition service.
    * `SECRET`: secret for using the Amazon Rekognition service.

* You will also need to set an environment variable named `GOOGLE_APPLICATION_CREDENTIALS` that
points to the Google Cloud Vision credentials file, as explained in the Setup manual.

# Running the project

* Set the `SPRING_APPLICATION_JSON` environment variable, as explained in the [Configuration](#configuration) section.
* Move into the `target` folder and run the command:
* `java -jar morpheus-0.1.jar`
