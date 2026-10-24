@echo off
set "JAVA_HOME=%USERPROFILE%\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ===================================================
echo Starting APAAR Verify Backend (Java 25 on Port 8085)
echo ===================================================
cd backend
java -jar target\apaar-verify-backend-1.0.0.jar
