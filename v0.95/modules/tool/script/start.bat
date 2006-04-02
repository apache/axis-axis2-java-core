@echo off
rem ---------------------------------------------------------------------------
rem Startup script for the Simple Axis Server (with default parameters)
rem
rem Environment Variable Prequisites
rem
rem   AXIS2_HOME      Must point at your AXIS2 directory 
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem   JAVA_OPTS       (Optional) Java runtime options 
rem ---------------------------------------------------------------------------
set CURRENT_DIR=%cd%

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not defined
echo This environment variable is needed to run this program
goto end
:gotJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto okJavaHome
:noJavaHome
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
echo NB: JAVA_HOME should point to a JDK/JRE
goto end
:okJavaHome

rem check the AXIS2_HOME environment variable
if not "%AXIS2_HOME%" == "" goto gotHome
set AXIS2_HOME=%CURRENT_DIR%
if exist "%AXIS2_HOME%\bin\start.bat" goto okHome

rem guess the home. Jump one directory up to check if that is the home
cd ..
set AXIS2_HOME=%cd%
cd %CURRENT_DIR%

:gotHome
if EXIST "%AXIS2_HOME%\bin\http-server.bat" goto okHome
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

call %EXECUTABLE% %REPO_FOLDER%   
:end
