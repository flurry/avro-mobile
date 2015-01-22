Avro on Mobile
===========

Avro Example with Java Server, iOS Client, Android Client

Apache Avro (http://avro.apache.org) is being used as the data serialization tool at Flurry for our ad serving over iOS and Android. This project serves as an illustration of our deployment. For more information into our use of Apache Avro see http://www.flurry.com/2012/07/12/apache-avro-at-flurry

This example uses an embedded Jetty Server for ease of deployment and testing (and because we think Jetty is awesome). Jetty is available here: 
http://www.eclipse.org/jetty/

Server Setup
=====

Clone the repo, which contains 3 projects: avro-java-server, avro-ios-client, and avro-android-client. 

Run Your Server
In avro-java-server run buildProtocol.xml followed by build.xml using ant (http://ant.apache.org/).

The build file puts all necessary libs in a single jar (flurry-avro-server-1.0.jar) for ease of running. Run this from the command line with one argument for the port:
java -jar flurry-avro-server-1.0.jar 8080

Testing
=====

Your server can be tested easily through the use of standard data transfer tools, such as curl. Simply run the following cmd (or the equivalent on your OS):

curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"adSpaceName":"splash_screen","location":{"lat":12.231212,"lon":23.3435},
}' http://localhost:8080

Note: To simulate an error, type the AdSpace Name as "throwError".

iOS Setup
=====

Open the xcode project in avro-ios-client. Run AvroClient > Sim or Device. When clicking Send Request, the client will attempt to send an Ad Request to http://localhost:8080 and will then parse and display the received Ad Response.

Android Setup
=====

This depends on the latest Android SDK with API level 10 installed. Open the avro-android-client project. Run "android update project --path ." from the avro-android-client directory. Modify the "server_url" string in res/values/strings.xml to be the IP address and port of your server. Run buildProtocol.xml followed by build.xml using ant (http://ant.apache.org/). Run your app on an Android device or emulator. The client will attempt to send an Ad Request to your server and will then parse and display the received Ad Response.

License 
=====
Copyright 2012 Flurry, Inc. (http://flurry.com)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
