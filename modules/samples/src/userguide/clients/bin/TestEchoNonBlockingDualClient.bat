@echo off
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\axis20.9.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\stax-api-1.0.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\stax-1.1.1-dev.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\log4j-1.2.8.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\commons-logging-1.0.3.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\commons-fileupload-1.0.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\axis-wsdl4j-1.2.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\geronimo-spec-activation-1.0.2-rc3.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\axis-wsdl4j-1.2.jar        
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\geronimo-spec-javamail-1.3.1-rc3.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\xbean-2.0.0-beta1.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\stax-api-1.0.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\sample.jar

java userguide.clients.EchoNonBlockingDualClient