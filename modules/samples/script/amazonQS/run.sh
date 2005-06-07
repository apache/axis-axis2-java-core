export AXIS2_HOME=../..
AXIS2_CLASSPATH=$AXIS2_HOME/lib/axis2-M2.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/axis-wsdl4j-1.2.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/commons-logging-1.0.3.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/log4j-1.2.8.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/stax-1.1.1-dev.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$AXIS2_HOME/lib/stax-api-1.0.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:amazonQS.jar
export AXIS2_CLASSPATH
echo the classpath $AXIS2_CLASSPATH
java -classpath $AXIS2_CLASSPATH sample.amazon.amazonSimpleQueueService.RunGUICQ &
java -classpath $AXIS2_CLASSPATH sample.amazon.amazonSimpleQueueService.RunGUIRQ &


