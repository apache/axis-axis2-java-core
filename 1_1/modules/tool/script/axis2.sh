#!/bin/sh

if [ $AXIS2_HOME="" ]; then
    export AXIS2_HOME=..
fi

# let's see we are pointing to the proper Axis2 home
if [ -e "${AXIS2_HOME}/bin/wsdl2java.sh" ]; then
 echo "Found the proper Axis2 Home"

else
    echo "I can not continue without getting an Axis2 Home"
fi

for f in $AXIS2_HOME/lib/*.jar
do
  AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$f
done
export AXIS2_CLASSPATH



echo the classpath $AXIS2_CLASSPATH
java -classpath $AXIS2_CLASSPATH org.apache.axis2.transport.http.SimpleHTTPServer $*
