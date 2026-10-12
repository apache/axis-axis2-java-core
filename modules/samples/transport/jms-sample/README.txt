Sample: JMS Sample
===============

Introduction
============

This sample demonstrate use of JMS transport in both server and client sides. Axis2 Simple Server Maven plug-in used as 
the web server and maven-activemq-plugin used as the JMS broker.    


Pre-Requisites
==============

Apache Maven 3.6.0+ and JDK 17 or later



Running the Sample Service
=========================

1.) In a command line move to "modules/samples/transport/jms-sample/jmsService" directory  and run " mvn clean package activemq:run axis2:run"




Running the Sample Client
=========================

This sample has no client module. The service listens on the JMS queue "SimpleService"
of the ActiveMQ broker at tcp://localhost:61616. Any JMS client can send it a SOAP
message, or an Axis2 ServiceClient configured with the JMS transport sender (see
jmsService/src/main/resources/axis2.xml) can use an endpoint such as:

  jms:/SimpleService?transport.jms.ConnectionFactoryJNDIName=QueueConnectionFactory&java.naming.factory.initial=org.apache.activemq.jndi.ActiveMQInitialContextFactory&java.naming.provider.url=tcp://localhost:61616


Help
====
Please contact java-user list (java-user@axis.apache.org) if you have any trouble running the sample.
