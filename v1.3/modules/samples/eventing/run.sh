#!/bin/sh
export AXIS2_HOME=../..
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:SGCCalculator.jar
for f in $AXIS2_HOME/lib/*.jar
do
  AXIS2_CLASSPATH=$AXIS2_CLASSPATH:$f
done
export AXIS2_CLASSPATH
echo classpath: $AXIS2_CLASSPATH

java -classpath $AXIS2_CLASSPATH -Daxis2.repo=$AXIS2_HOME  sample.sgccalculator.CalcClient
