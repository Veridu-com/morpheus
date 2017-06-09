Setup manual
============

# Requirements

* You will need Java 1.8. The recommended version is from Oracle, which can be downloaded [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). I have found problems when using `OpenJDK`, so it's recommended to use the Oracle version.
* To compile the project you will need [Maven](https://maven.apache.org/) installed on your system. Installation instructions can be found [here](https://maven.apache.org/install.html).

# Compilation

Run the following commands on the root folder of the project to compile it:

* `mvn clean`
* `mvn package`
* The file `morpheus-0.1.jar` will be generated inside the `target`

If you are using an IDE, such as IntelliJ, do not forget to set the `SPRING_APPLICATION_JSON` environment variable on the Run configurations.

# Setup of Google Cloud Vision API
The OCR task in Morpheus requires the use of Google Cloud Vision API.
A project has to be created in the [Google Cloud console](https://console.cloud.google.com).
Credentials have to be created as well, so Morpheus can access the service.
The recommended way to do that is by creating a [Google Application Default Credentials](A detailed guide on creating the credentials is available [here](https://cloud.google.com/docs/authentication#getting_credentials_for_server-centric_flow).
The environment variable `GOOGLE_APPLICATION_CREDENTIALS` should be used to point to the file that defines the credentials.

In summary, the steps are as follows to obtain and configure credentials:
1. Go to the [API Console Credentials page](https://console.developers.google.com/project/_/apis/credentials).
2. From the project drop-down, select your project.
3. On the Credentials page, select the Create credentials drop-down, then select Service account key.
4. From the Service account drop-down, select an existing service account or create a new one. **Set the role as Service Account Users**.
5. For Key type, select the JSON key option, then select Create. The file automatically downloads to your computer.
6. Put the *.json file you just downloaded in a directory of your choosing. This directory must be private (you can't let anyone get access to this), but accessible to your web server code.
7. Set the environment variable GOOGLE_APPLICATION_CREDENTIALS to the path of the JSON file downloaded.

You will also need to [enable Google Cloud Vision](https://cloud.google.com/vision/docs/before-you-begin?hl=pt) on project settings.

# Setup of Amazon Rekognition API
The Photo task in Morpheus requires Amazon Rekognition to perform face detection.

An IAM User credential needs to be created with permission to access the Rekognition service.
A guide on setting up that account is available [here](https://docs.aws.amazon.com/rekognition/latest/dg/setting-up.html#setting-up-iam).

Two variables have to be set in morpheus with the credentials: the access key and the secret.
Refer to the configuration section for these settings.
