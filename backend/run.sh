#!/bin/bash

echo "===================================="
echo "Leave Management System - Backend"
echo "===================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven from https://maven.apache.org/download.cgi"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install JDK 17+ from https://adoptium.net/"
    exit 1
fi

echo "Maven and Java found!"
echo ""

# Set environment variables if not already set
export DB_USERNAME=${DB_USERNAME:-postgres}
export DB_PASSWORD=${DB_PASSWORD:-postgres}
export JWT_SECRET=${JWT_SECRET:-your-256-bit-secret-key-change-this-in-production}

echo "Using Database: leavemarker"
echo "Database User: $DB_USERNAME"
echo ""
echo "WARNING: Make sure PostgreSQL is running and database 'leavemarker' exists"
echo ""
echo "To create database, run in psql:"
echo "  CREATE DATABASE leavemarker;"
echo ""
read -p "Press Enter to continue..."

echo ""
echo "Building application..."
mvn clean install
if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Build failed!"
    exit 1
fi

echo ""
echo "===================================="
echo "Build successful!"
echo "Starting application..."
echo "===================================="
echo ""
echo "Application will start on: http://localhost:8080/api"
echo "Health check: http://localhost:8080/api/health"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

mvn spring-boot:run
