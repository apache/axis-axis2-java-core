@echo off
rem ---------------------------------------------------------------------------
rem Start script for running the Google spell checker
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
if EXIST "%AXIS_HOME%\lib\axis2-M2.jar" goto okHome
echo The AXIS_HOME environment variable seems not to point to the correct loaction!
echo This environment variable is needed to run this program
pause
exit

:okHome
cd %CURRENT_DIR%
rem get the classes
set AXIS2_CLASS_PATH="%AXIS_HOME%";"%AXIS_HOME%\lib\axis2-M2.jar";"%AXIS_HOME%\lib\axis-wsdl4j-1.2.jar";"%AXIS_HOME%\lib\commons-logging-1.0.3.jar";"%AXIS_HOME%\lib\log4j-1.2.8.jar";"%AXIS_HOME%\lib\stax-1.1.1-dev.jar";"%AXIS_HOME%\lib\stax-api-1.0.jar"
set AXIS2_CLASS_PATH=%AXIS2_CLASS_PATH%;"%CURRENT_DIR%\googleSpellcheck.jar"
start javaw -cp %AXIS2_CLASS_PATH% sample.google.spellcheck.SuggestionForm

:end