# Samples for the Brightics IoT Device SDK for Java

This directory contains samples showing how to use the various features of the Brightics IoT Platform from a device running Java code.

## List of samples

* [Sample App]: ITA 인증 방법으로 IoT 서버와 Connect 하는 예제
* [Sample AppActivation]: Root thing (부모 사물)을 활성화 시키는 예제
* [Sample AppActivationLeafThing]: Leaf thing (자식 사물)을 활성화 시키는 예제
* [Sample AppAttribute]: 사물의 속성 데이터를 B.IoT 에 업데이트하는 예제
* [Sample AppFirmware]: 능동형 펌웨어 최신 버전을 요청하거나 수동형 펌웨어 요청을 처리하는 예제
* [Sample AppMessage]: 메시지 헤더 타입 설정, 메시지 데이터 압축 및 암호화 설정 예제
* [Sample AppNotification]: IoT 서버에서 오는 Request 혹은 Notify 메시지를 처리하는 예제
* [Sample AppRegistration]: Leaf Thing 등록 및 활성화하는 예제
* [Sample AppSSL]: Two-way SSL 인증 방법으로 IoT 서버와 Connect 하는 예제
* [Sample AppUsingFileINI]: ini 파일을 이용하여 유저 정보를 획득하고 IoT 서버와 Connect 하는 예제

## Get Started

Refer to following the instructions [here][Get started] to get started.

### Get and run the samples
1. Download the sample
	```sh
	{sample root}/>mvn clean install
	```
1. You can use the following commands to execute the sample applications.
* To run the Connect sample (App), use the following command:
	```sh
	{sample root}/>mvn exec:java -Dexec.mainClass="com.sample.app.App"
	```

[Get started]: http://docs-brighticsiot.samsungsds.com:8090/pages/viewpage.action?spaceKey=V29KO&title=Get+Started
