@echo off
rem ---------------------------------------------------------------------------
rem Start script for running the Simple Axis Server
rem
rem ---------------------------------------------------------------------------

rem get the classes for the simple axis server
set AXIS2_CLASS_PATH="%AXIS_HOME%";"%AXIS_HOME%\lib\axis2-0.91.jar";"%AXIS_HOME%\lib\axis-wsdl4j-1.2.jar";"%AXIS_HOME%\lib\commons-logging-1.0.3.jar";"%AXIS_HOME%\lib\log4j-1.2.8.jar";"%AXIS_HOME%\lib\stax-1.1.2-dev.jar";"%AXIS_HOME%\lib\stax-api-1.0.jar";"%AXIS_HOME%\lib\geronimo-spec-activation-1.0.2-rc3.jar";"%AXIS_HOME%\lib\geronimo-spec-javamail-1.3.1-rc3.jar";"%AXIS_HOME%\lib\xbean-2.0.0-beta1.jar";%AXIS_HOME%\lib\commons-codec-1.3.jar

java -cp %AXIS2_CLASS_PATH% org.apache.axis2.transport.tcp.TCPServer %1 %2
