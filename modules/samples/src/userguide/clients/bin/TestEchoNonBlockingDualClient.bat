@echo off
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\axis2-0.91.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\axis-wsdl4j-1.2.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\commons-logging-1.0.3.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\log4j-1.2.8.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\stax-1.1.2-dev.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\stax-api-1.0.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\geronimo-spec-activation-1.0.2-rc4.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\geronimo-spec-javamail-1.3.1-rc5.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\xbean-2.0.0-beta1.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\commons-httpclient-3.0-rc3.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\commons-codec-1.3.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\ant-1.6.2.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\commons-fileupload-1.0.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\geronimo-spec-servlet-2.4-rc4.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\groovy-all-1.0-jsr-01.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\jaxen-1.1-beta-7.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\log4j-1.2.8.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\XmlSchema-0.9.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\..\..\lib\xmlunit-1.0.jar
set CLASSPATH=%CLASSPATH%;..\..\..\..\sample.jar

java userguide.clients.EchoNonBlockingDualClient