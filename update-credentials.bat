@echo off
echo ========================================
echo Updating User Credentials in Database
echo ========================================
echo.

REM Compile and run the credential updater
javac -cp "target/classes;%USERPROFILE%/.m2/repository/com/h2database/h2/2.2.220/h2-2.2.220.jar" UpdateCredentials.java
java -cp ".;target/classes;%USERPROFILE%/.m2/repository/com/h2database/h2/2.2.220/h2-2.2.220.jar" UpdateCredentials

echo.
echo ========================================
echo Credentials updated successfully!
echo ========================================
echo.
echo You can now login with the new credentials:
echo   superadmin / admin123
echo   captain / captain123
echo   secretary / secretary123
echo   treasurer / treasurer123
echo   etc.
echo.
pause
