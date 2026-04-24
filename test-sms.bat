@echo off
echo ========================================
echo UniSMS API Test Utility
echo ========================================
echo.
echo Make sure your BDMS application is CLOSED before running this!
echo.
pause

java -cp ".;target/classes;%USERPROFILE%\.m2\repository\com\h2database\h2\2.1.214\h2-2.1.214.jar" com.example.TestUniSMS

echo.
echo ========================================
echo Test Complete!
echo ========================================
pause
