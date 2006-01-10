@echo off
rem ---------------------------------------------------------------------------
rem Start script for running the Amazon Queuing service
rem
rem ---------------------------------------------------------------------------

rem store the current directory
set CURRENT_DIR=%cd%

rem check the AXIS_HOME environment variable
if not "%AXIS_HOME%" == "" goto gotHome

rem guess the home. Jump two directories up and take that as the home
cd ..
cd ..
set AXIS_HOME=%cd%

:gotHome
if EXIST "%AXIS_HOME%\lib\axis2*.jar" goto okHome
echo The AXIS_HOME environment variable seems not to point to the correct location!
echo This environment variable is needed to run this program
pause
exit

:okHome
rem set the classes
cd %CURRENT_DIR%

setlocal EnableDelayedExpansion

rem loop through the libs and add them to the class path
set AXIS2_CLASS_PATH=%AXIS_HOME%
FOR %%c in (%AXIS_HOME%\lib\*.jar) DO set AXIS2_CLASS_PATH=!AXIS2_CLASS_PATH!;%%c
set AXIS2_CLASS_PATH=%AXIS2_CLASS_PATH%;%CURRENT_DIR%\amazonQS.jar

start javaw -cp %AXIS2_CLASS_PATH% -Daxis2.home=%AXIS_HOME% sample.amazon.amazonSimpleQueueService.RunGUICQ
start javaw -cp %AXIS2_CLASS_PATH% -Daxis2.home=%AXIS_HOME% sample.amazon.amazonSimpleQueueService.RunGUIRQ

endlocal
:end