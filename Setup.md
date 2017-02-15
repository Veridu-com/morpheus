Setup manual
============

# Requirements

* To compile the project you will need [Maven](https://maven.apache.org/) installed on your system. Installation instructions can be found [here](https://maven.apache.org/install.html).
* You will also need Java 1.8. The recommended version is from Oracle, which can be downloaded [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

# Compilation

Run the following commands on the root folder of the project to compile it:

* `mvn clean`
* `mvn package`
* The file `morpheus-0.1.jar` will be generated inside the `target`

If you are using an IDE, such as IntelliJ, do not forget to set the `SPRING_APPLICATION_JSON` environment variable on the Run configurations.
