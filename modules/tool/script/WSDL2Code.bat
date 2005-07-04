@echo off
rem ---------------------------------------------------------------------------
rem Start script for the WSDLCode
rem
rem 
rem ---------------------------------------------------------------------------

rem Guess AXIS_HOME if not defined
if not "%AXIS_HOME%" == "" goto gotHome
cd ..
set AXIS_HOME=%cd%

:gotHome
if EXIST "%AXIS_HOME%\lib\axis2-0.9.jar" goto okHome
echo The AXIS_HOME environment variable seems not to point to the correct location!
echo This environment variable is needed to run this program
pause
exit

:okHome
rem set the classes
set AXIS2_CLASS_PATH="%AXIS_HOME%";"%AXIS_HOME%\lib\axis2-0.9.jar";"%AXIS_HOME%\lib\axis-wsdl4j-1.2.jar";"%AXIS_HOME%\lib\commons-logging-1.0.3.jar";"%AXIS_HOME%\lib\log4j-1.2.8.jar";"%AXIS_HOME%\lib\stax-1.1.1-dev.jar";"%AXIS_HOME%\lib\xbean-2.0.0-beta1.jar";"%AXIS_HOME%\lib\stax-api-1.0.jar""%AXIS_HOME%\lib\geronimo-spec-activation-1.0.2-rc3.jar";"%AXIS_HOME%\lib\geronimo-spec-javamail-1.3.1-rc3.jar";"%AXIS_HOME%\lib\xbean-2.0.0-beta1.jar"

java -cp %AXIS2_CLASS_PATH% org.apache.axis2.wsdl.WSDL2Code %1 %2 %3 %4 %5 %6 %7 %8 %9 

:end