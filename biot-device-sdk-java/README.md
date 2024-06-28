# Brightics IoT Device SDK for Java


The **Brightics IoT Device SDK for Java** enables Java developers to easily work with [Brighitcs IoT Services] and build scalable solutions with platform [Brighitcs IoT]. You can get
started in minutes using ***Maven***.



## Getting Started

#### Sign up for Brighitcs IoT ####

Before you begin, you need an AWS account. Please see the [Sign Up for Brightics IoT] section

#### Minimum requirements ####

To run the SDK you will need **Java 1.8.0_101**. For more information about the requirements and optimum settings for the SDK.

#### Install the SDK ####

The recommended way to use the Brighitcs IoT Device SDK for Java in your project is to consume it from Maven. Import the specify the SDK Maven modules that your project needs in the
dependencies.

##### Using the SDK Maven modules #####

```xml
    <dependency>
      <groupId>com.sds.ocp</groupId>
      <artifactId>biot-device-sdk-java</artifactId>
      <version>2.9.0-SNAPSHOT</version>
    </dependency>
```

### Build the SDK
You can build both the SDK and its sample applications from the source 
from Brightics IoT manual's site. 

```sh
$ unzip brighitcs-iot-device-x.x.x.zip
$ cd brighitcs-iot-device-x.x.x
$ mvn clean install
```

See the **[Set up the Brighitcs Device SDK for Java]** section of the developer guide for more
information about installing the SDK through other means.


## Features

* Provides easy-to-use MQTT clients for supported Brighitcs IoT services, regions, and authentication
    protocols.

* Client-Side Data Encryption for AES128

* Provides Thing Basic-Provisioning 

## Downloading From Source

[Download source file][single zip file]

## Getting Help

[Get started][public manual site]

## Supported Versions

* **2.9.x** - Recommended.

We have client library that can be used against **Brightics IoT API**, 
which is specified in 
[THIS] repository.

[Brighitcs IoT Services]: https://iot.insator.io/
[Brighitcs IoT]: http://www.insator.io/
[single zip file]: http://docs-brighticsiot.samsungsds.com:8090/display/V29KO/Device+SDK+Java
[Sign Up for Brightics IoT]: https://iot.insator.io/
[Set up the Brighitcs Device SDK for Java]: http://docs-brighticsiot.samsungsds.com:8090/display/V29KO/Getting+Started
[public manual site]: https://www.samsungsdsbiz.com/help/brighticsiot/V2/2-8/KR/dev 
[THIS]: http://devops.sdsdev.co.kr/confluence/pages/viewpage.action?pageId=34512008 
