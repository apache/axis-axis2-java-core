rem A simple script to create the md5 and signatures
rem first pick the files in the current directory

setlocal EnableDelayedExpansion
FOR %%c in ("%cd%\*.%1") DO md5 %%c
endlocal

