@echo off
echo Starting Barangay San Marino BDMS...
echo.
call "%~dp0mvnw.cmd" clean javafx:run
pause
