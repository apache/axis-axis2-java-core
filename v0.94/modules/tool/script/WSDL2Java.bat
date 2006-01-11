@echo off
rem ---------------------------------------------------------------------------
rem Start script for the WSDLJava
rem
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

:gotHome
if EXIST "%AXIS_HOME%\lib\axis2*.jar" goto okHome
echo The AXIS_HOME environment variable seems not to point to the correct location!
echo This environment variable is needed to run this program
pause
exit

:okHome
rem set the classes
setlocal EnableDelayedExpansion
rem loop through the libs and add them to the class path
set AXIS2_CLASS_PATH=%AXIS_HOME%
FOR %%c in (%AXIS_HOME%\lib\*.jar) DO set AXIS2_CLASS_PATH=!AXIS2_CLASS_PATH!;%%c

rem run the command
java -cp %AXIS2_CLASS_PATH% org.apache.axis2.wsdl.WSDL2Java %1 %2 %3 %4 %5 %6 %7 %8 %9
endlocal
:end
