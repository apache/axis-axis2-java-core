@echo off
rem ---------------------------------------------------------------------------
rem Start script for the Simple Axis Server
rem
rem This starts the server with default parameters
rem ---------------------------------------------------------------------------

rem Guess AXIS2_HOME if not defined
if not "%AXIS2_HOME%" == "" goto gotHome
cd ..
set AXIS2_HOME=%cd%

:gotHome

if exist "%AXIS2_HOME%\bin\http-server.bat" goto okHome
echo The AXIS2_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end

:okHome
rem check for the default service repository
if exist "%AXIS2_HOME%\repository" goto okRepo
mkdir "%AXIS2_HOME%\repository"

:okRepo
set REPO_FOLDER=%AXIS2_HOME%\repository
if exist "%REPO_FOLDER%\services" goto okService
mkdir "%REPO_FOLDER%\services"

:okService
set EXECUTABLE="%AXIS2_HOME%\bin\http-server.bat"

call %EXECUTABLE% %REPO_FOLDER% 8080  

:end
