@echo off
rem ---------------------------------------------------------------------------
rem Start script for running the jar generator tool
rem
rem ---------------------------------------------------------------------------

rem set the class path for the tool
set TOOL_CLASS_PATH=".\axis2-tools-M1.jar;.\ant-1.6.2.jar"

java -cp %TOOL_CLASS_PATH% org.apache.axis.tool.ui.MainWindow
