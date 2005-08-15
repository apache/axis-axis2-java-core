#!/bin/sh

cd ..
AXIS2_CLASSPATH=./lib/axis2-0.91.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:./lib/axis-wsdl4j-1.2.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:./lib/commons-logging-1.0.3.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:./lib/log4j-1.2.8.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:./lib/stax-1.1.1-dev.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:./lib/geronimo-spec-activation-1.0.2-rc3.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:./lib/axis-wsdl4j-1.2.jar        
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:./lib/geronimo-spec-javamail-1.3.1-rc3.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:./lib/xbean-2.0.0-beta1.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:./lib/stax-api-1.0.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:./lib/commons-codec-1.3.jar

export AXIS2_CLASSPATH
echo the classpath $AXIS2_CLASSPATH
java -classpath $AXIS2_CLASSPATH org.apache.axis2.transport.tcp.TCPServer  $1 $2
