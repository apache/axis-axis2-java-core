#!/bin/sh

export AXIS2_HOME=..

for f in $AXIS2_HOME/lib/*.jar
do
  AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$f
done
export AXIS2_CLASSPATH

echo the classpath $AXIS2_CLASSPATH
java -classpath $AXIS2_CLASSPATH org.apache.axis2.transport.SimpleAxis2Server -repo $AXIS2_HOME/repository -conf $AXIS2_HOME/conf/axis2.xml $*
