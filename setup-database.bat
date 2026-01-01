@echo off
REM Customer Management System Database Setup
REM This batch file sets up the database for the application

echo ========================================
echo Customer Management System Database Setup
echo ========================================
echo.

REM Set variables
set MYSQL_PATH=C:\Program Files\MariaDB 12.1\bin\mysql.exe
set DB_USER=root
set DB_NAME=customer_management_db
set PROJECT_DIR=e:\MY PROJECTS\Customer_Management_System

echo.
echo Please enter MariaDB root password (or press Enter if no password):
set /p DB_PASS=Password: 

echo Checking if MySQL exists...
if not exist "%MYSQL_PATH%" (
    echo Error: MySQL not found at %MYSQL_PATH%
    echo Please install XAMPP or update the path
    pause
    exit /b 1
)
echo OK - MySQL found

echo.
echo Step 1: Creating database and user...
(
    echo CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    echo DROP USER IF EXISTS 'cms_user'^@'localhost';
    echo CREATE USER 'cms_user'^@'localhost' IDENTIFIED BY 'cms_pass';
    echo GRANT ALL PRIVILEGES ON %DB_NAME%.* TO 'cms_user'^@'localhost';
    echo FLUSH PRIVILEGES;
) | "%MYSQL_PATH%" -u %DB_USER% %if "%DB_PASS%"=="" echo "else echo -p%DB_PASS%"%

if errorlevel 1 (
    echo Failed to create database
    pause
    exit /b 1
)
echo OK - Database created

echo.
echo Step 2: Creating tables...
"%MYSQL_PATH%" -u cms_user -pcms_pass %DB_NAME% < "%PROJECT_DIR%\backend\src\main\resources\schema.sql"

if errorlevel 1 (
    echo Failed to create tables
    pause
    exit /b 1
)
echo OK - Tables created

echo.
echo Step 3: Loading sample data...
"%MYSQL_PATH%" -u cms_user -pcms_pass %DB_NAME% < "%PROJECT_DIR%\backend\src\main\resources\data.sql"

if errorlevel 1 (
    echo Warning: Could not load sample data
) else (
    echo OK - Sample data loaded
)

echo.
echo ========================================
echo Database setup completed successfully!
echo ========================================
echo.
echo Next steps:
echo 1. Start Backend: cd backend ^& mvn spring-boot:run
echo 2. Start Frontend: cd frontend ^& npm start
echo 3. Open: http://localhost:3000
echo.
pause
