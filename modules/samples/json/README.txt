Sample for JSON implementation first approach
=============================================

Introduction
============

This is an example for new JSON implementation which provide capability to send pure JSON string instead of any
convention.

In this sample it sends

{"echoUser":{"user":{"name":"My_Name","surname":"MY_Surname","middleName":"My_MiddleName","age":123,
    "address":{"country":"My_Country","city":"My_City","street":"My_Street","building":"My_Building","flat":"My_Flat","zipCode":"My_ZipCode"}}}}

JSON request to the echoUser method and get the response as

{"Response":{"name":"My_Name","surname":"MY_Surname","middleName":"My_MiddleName","age":123,
    "address":{"country":"My_Country","city":"My_City","street":"My_Street","building":"My_Building","flat":"My_Flat","zipCode":"My_ZipCode"}}}


Pre-Requisites
==============

Apache Ant 1.6.2 or later

Running The Sample
==================

First of all add following message builder and message formatter to axis2.xml configuration file. You can find this
configuration file in AXIS2_HOME/conf/ directory.

<messageBuilder contentType="application/json-impl"
			class="org.apache.axis2.json.gson.JsonBuilder" />

<messageFormatter contentType="application/json-impl"
			class="org.apache.axis2.json.gson.JsonFormatter" />

Goto AXIS2_HOME/sample/json/ directory and

Type "ant" or "ant generate.service" to generate sample JsonService.aar and cp it to AXIS2_HOME/repository/services/

Then type "ant run.client" to compile client code and run the client

Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.