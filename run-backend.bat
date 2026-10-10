@echo off
echo ======================================================================
echo  Starting SmartFind Spring Boot Backend (Port 8083)...
echo ======================================================================
cd /d "%~dp0SmartFindBackend"

if not exist "target\smartfind-backend-1.0.0.jar" (
    echo [INFO] JAR file not found. Building now with Maven...
    call mvn clean package -DskipTests
)

java -jar target\smartfind-backend-1.0.0.jar
pause
