@echo off
rem ---------------------------------------------------------------------------
rem Start script for running the Google Search tool
rem
rem ---------------------------------------------------------------------------

rem store the current directory
set CURRENT_DIR=%cd%

rem check the AXIS_HOME environment variable
if not "%AXIS_HOME%" == "" goto gotHome

rem guess the home. Jump two directories up nad take that as the home
cd ..
cd ..
set AXIS_HOME=%cd%

echo using Axis Home %AXIS_HOME%

:gotHome
if EXIST "%AXIS_HOME%\lib\axis2-1.0-alpha.jar" goto okHome
echo The AXIS_HOME environment variable seems not to point to the correct loaction!
echo This environment variable is needed to run this program
pause
exit

:okHome
rem set the classes
cd %CURRENT_DIR%
set AXIS2_CLASS_PATH="%AXIS_HOME%";"%AXIS_HOME%\lib\axis2-1.0-alpha.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\axis-wsdl4j-1.2.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\commons-logging-1.0.3.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\log4j-1.2.8.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\stax-1.1.2-dev.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\stax-api-1.0.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\geronimo-spec-activation-1.0.2-rc4.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\geronimo-spec-javamail-1.3.1-rc5.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\xbean-2.0.0-beta1.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\commons-httpclient-3.0-rc3.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\commons-codec-1.3.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\ant-1.6.2.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\commons-fileupload-1.0.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\geronimo-spec-servlet-2.4-rc4.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\groovy-all-1.0-jsr-01.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\jaxen-1.1-beta-7.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\log4j-1.2.8.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\XmlSchema-0.9.jar"
set AXIS2_CLASS_PATH= %AXIS2_CLASS_PATH%;"%AXIS_HOME%\lib\xmlunit-1.0.jar"
set AXIS2_CLASS_PATH=%AXIS2_CLASS_PATH%;"%CURRENT_DIR%\googleSearch.jar"
start javaw -cp %AXIS2_CLASS_PATH% sample.google.search.AsynchronousClient

:end