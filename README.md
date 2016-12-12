Morpheus
========

Morpheus is the next iteration of Skynet.

The project no longer depends on JBoss as an application server, instead 
it uses Spring Boot and other projects from the Spring framework to create
a lightweight version of Skynet.

#Configuration via environment variable
 
You need to set the `SPRING_APPLICATION_JSON` environment variable with the JSON configuration, such as in the following example:
 
```
{
  "server": {
    "address": "0.0.0.0",
    "port": 8080
  },
  "logging": {
    "file": "morpheus-spring.log"
  },
  "morpheus": {
    "http": {
      "user": "cassio",
      "password": "god"
    },
    "handlerPrivateKey": "213b83392b80ee98c8eb2a9fed9bb84d",
    "handlerPublicKey": "ef970ffad1f1253a2182a88667233991",
    "useAPISSLchecking": false,
    "IDOS_DEBUG": 1,
    "IDOS_API_URL": "http://127.0.0.1:8000/index.php/1.0"
  }
}
 ```
 
If you are using an IDE, do not forget to set the environment variable on the Run configurations.
