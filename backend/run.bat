@echo off
echo ====================================
echo Leave Management System - Backend
echo ====================================
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install JDK 17+ from https://adoptium.net/
    pause
    exit /b 1
)

echo Maven and Java found!
echo.

REM Set environment variables if not already set
if "%DB_USERNAME%"=="" set DB_USERNAME=postgres
if "%DB_PASSWORD%"=="" set DB_PASSWORD=postgres
if "%JWT_SECRET%"=="" set JWT_SECRET=your-256-bit-secret-key-change-this-in-production

echo Using Database: leavemarker
echo Database User: %DB_USERNAME%
echo.
echo WARNING: Make sure PostgreSQL is running and database 'leavemarker' exists
echo.
echo To create database, run in psql:
echo   CREATE DATABASE leavemarker;
echo.
pause

echo.
echo Building application...
call mvn clean install
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo ====================================
echo Build successful!
echo Starting application...
echo ====================================
echo.
echo Application will start on: http://localhost:8080/api
echo Health check: http://localhost:8080/api/health
echo.
echo Press Ctrl+C to stop the application
echo.

call mvn spring-boot:run
