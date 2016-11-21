Morpheus
========

Morpheus is the next iteration of Skynet.

The project no longer depends on JBoss as an application server, instead 
it uses Spring Boot and other projects from the Spring framework to create
a lightweight version of Skynet.

#Configuration via environment variable
 
You need to set the `SPRING_APPLICATION_JSON` environment variable with the JSON configuration, such as in the following example:
 
```bash
 SPRING_APPLICATION_JSON='{"server":{"address":"0.0.0.0", "port": 8080}, "logging":{"file":"morpheus-spring.log"}, "morpheus":{"http":{"user":"cassio", "password":"god"}}}' java -jar myapp.jar
 ```
 
If you are using an IDE, do not forget to set the environment variable on the Run configurations.
