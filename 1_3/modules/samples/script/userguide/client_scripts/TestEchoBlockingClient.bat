@echo off
setlocal EnableDelayedExpansion

rem loop through the libs and add them to the class path
set AXIS2_HOME=..\..\..\

set AXIS2_CLASS_PATH=%AXIS2_HOME%
FOR %%c in (%AXIS2_HOME%\lib\*.jar) DO set AXIS2_CLASS_PATH=!AXIS2_CLASS_PATH!;%%c

set AXIS2_CLASS_PATH=%AXIS2_CLASS_PATH%;..\sample.jar

java -cp %AXIS2_CLASS_PATH% userguide.clients.EchoBlockingClient
endlocal