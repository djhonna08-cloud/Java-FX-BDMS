@echo off
echo ========================================
echo Final SMS Test - UniSMS API
echo ========================================
echo.
echo This will send a professional message that won't be flagged as spam.
echo.
echo Phone: +639563052862
echo Message: "Your barangay clearance document has been approved..."
echo.
pause

java SimpleSMSTest

echo.
echo ========================================
echo Check your phone for the SMS!
echo It should arrive within 1-5 minutes.
echo ========================================
echo.
pause
