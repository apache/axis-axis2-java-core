@echo off
rem ---------------------------------------------------------------------------
rem Start script for running the Google Search tool
rem
rem ---------------------------------------------------------------------------

rem check the AXIS_HOME environment variable
if not "%AXIS_HOME%" == "" goto gotHome
echo The AXIS_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
pause
exit

:gotHome
if EXIST "%AXIS_HOME%\lib\axis2-M2.jar" goto okHome
echo The AXIS_HOME environment variable seems not to point to the correct loaction!
echo This environment variable is needed to run this program
pause
exit

:okHome
rem set the classes
set AXIS2_CLASS_PATH="%AXIS_HOME%";"%AXIS_HOME%\lib\axis2-M2.jar";"%AXIS_HOME%\lib\axis-wsdl4j-1.2.jar";"%AXIS_HOME%\lib\commons-logging-1.0.3.jar";"%AXIS_HOME%\lib\log4j-1.2.8.jar";"%AXIS_HOME%\lib\stax-1.1.1-dev.jar";"%AXIS_HOME%\lib\stax-api-1.0.jar"
set AXIS2_CLASS_PATH=%AXIS2_CLASS_PATH%;.\googleSearch.jar
javaw -cp %AXIS2_CLASS_PATH% sample.google.search.AsynchronousClient

:end