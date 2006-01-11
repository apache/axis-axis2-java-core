@echo off
rem ---------------------------------------------------------------------------
rem Start script for the Simple Axis Server
rem
rem This starts the server with default parameters
rem ---------------------------------------------------------------------------

rem Guess AXIS_HOME if not defined
if not "%AXIS_HOME%" == "" goto gotHome
cd ..
set AXIS_HOME=%cd%

:gotHome

if exist "%AXIS_HOME%\bin\http-server.bat" goto okHome
echo The AXIS_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end

:okHome
rem check for the default service repository
if exist "%AXIS_HOME%\repository" goto okRepo
mkdir "%AXIS_HOME%\repository"

:okRepo
set REPO_FOLDER=%AXIS_HOME%\repository
if exist "%REPO_FOLDER%\services" goto okService
mkdir "%REPO_FOLDER%\services"

:okService
set EXECUTABLE="%AXIS_HOME%\bin\http-server.bat"

call %EXECUTABLE% %REPO_FOLDER% 8080  

:end
