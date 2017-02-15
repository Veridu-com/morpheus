Setup manual
=================

# Configuration via environment variable

You need to set the `SPRING_APPLICATION_JSON` environment variable with the JSON configuration, such as in the following example:

```bash
SPRING_APPLICATION_JSON="{"server": {  "address": "0.0.0.0",  "port": 8080},"logging": {  "file": "morpheus-spring.log"},"morpheus": {  "http": {    "user": "cassio",    "password": "god"  },  "handlerPrivateKey": "213b83392b80ee98c8eb2a9fed9bb84d",  "handlerPublicKey": "ef970ffad1f1253a2182a88667233991",  "useSSL": false,  "IDOS_DEBUG": 1,  "IDOS_API_URL": "http://127.0.0.1:8000/index.php/1.0"}}"
```

* The `server` variable controls the `address` and `port` where the service will run.
* The `logging` variable indicates the path of the logging file.
* The `morpheus` variable contains:
    * `http`: basic AUTH credentials;
    * `handlerPrivateKey`: which should be registered with `idOS`;
    * `handlerPublicKey`: which has the public key registered with `idOS`;
    * `useSSL`: indicates whether SSL checking happens when calling the `idOS` API;
    * `IDOS_DEBUG`: indicates whther the requests to `idOS` should be printed to stdout;
    * `IDOS_API_URL`: indicates the URL to connect to the `idOS` API.

If you are using an IDE, such as IntelliJ, do not forget to set the `SPRING_APPLICATION_JSON` environment variable on the Run configurations.
