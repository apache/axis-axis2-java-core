Sample: JMS
===========

Introduction
============


Pre-Requisites
==============

Apache Ant 1.6.2 or later

Put repository\modules\addressing-1.1.mar into client_repository\modules and
server_repository\modules folders

Download incubator-activemq-4.0.1.jar[2] and put it into client_repository\lib
and server_repositor\modules folders.


Building the Service
====================

Type "ant clean prepare generate.service" from Axis2_HOME/samples/jms directory".


Running the Client
==================

Type "ant start.server" from Axis2_HOME/samples/jms directory to start the server.
Type "ant client.compile client.jar client.run" to run the client.


Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.