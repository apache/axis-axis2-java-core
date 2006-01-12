@echo off
rem ---------------------------------------------------------------------------
rem Start script for running the Simple Axis Server
rem
rem ---------------------------------------------------------------------------

rem get the classes for the simple axis server
rem set the AXIS2_HOME as the default classpath
setlocal EnableDelayedExpansion
rem loop through the libs and add them to the class path
set AXIS2_CLASS_PATH=%AXIS2_HOME%
FOR %%c in (%AXIS2_HOME%\lib\*.jar) DO set AXIS2_CLASS_PATH=!AXIS2_CLASS_PATH!;%%c

java -cp %AXIS2_CLASS_PATH% org.apache.axis2.transport.http.SimpleHTTPServer %1 %2
endlocal