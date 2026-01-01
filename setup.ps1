# Customer Management System - Automated Setup Script
# Run this script to set up the complete system

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Customer Management System Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Find MariaDB/MySQL installation
Write-Host "Step 1: Finding MariaDB/MySQL..." -ForegroundColor Yellow

$mysqlPaths = @(
    "C:\Program Files\MariaDB 10.11\bin\mysql.exe",
    "C:\Program Files\MariaDB 10.10\bin\mysql.exe",
    "C:\Program Files\MariaDB 11.0\bin\mysql.exe",
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
)

$mysql = $null
foreach ($path in $mysqlPaths) {
    if (Test-Path $path) {
        $mysql = $path
        Write-Host "  ✓ Found: $mysql" -ForegroundColor Green
        break
    }
}

if ($null -eq $mysql) {
    Write-Host "  ✗ MariaDB/MySQL not found!" -ForegroundColor Red
    Write-Host "  Please install MariaDB or XAMPP first." -ForegroundColor Red
    Write-Host "  MariaDB: https://mariadb.org/download/" -ForegroundColor Yellow
    Write-Host "  XAMPP: https://www.apachefriends.org/" -ForegroundColor Yellow
    exit 1
}

# Check if MariaDB is running
Write-Host ""
Write-Host "Step 2: Checking if MariaDB is running..." -ForegroundColor Yellow
$process = Get-Process -Name mysqld,mariadbd -ErrorAction SilentlyContinue
if ($null -eq $process) {
    Write-Host "  ✗ MariaDB is not running!" -ForegroundColor Red
    Write-Host "  Please start MariaDB service first." -ForegroundColor Red
    exit 1
} else {
    Write-Host "  ✓ MariaDB is running" -ForegroundColor Green
}

# Get project directory
$projectDir = Split-Path -Parent $PSScriptRoot
if (-not $projectDir) {
    $projectDir = "e:\MY PROJECTS\Customer_Management_System"
}

Write-Host ""
Write-Host "Step 3: Setting up database..." -ForegroundColor Yellow
Write-Host "  Enter MariaDB root password (press Enter if no password):" -ForegroundColor Cyan

# Setup database
& $mysql -u root -p < "$projectDir\setup-database.sql"
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Database created successfully" -ForegroundColor Green
} else {
    Write-Host "  ✗ Database creation failed" -ForegroundColor Red
    Write-Host "  Please run manually: mysql -u root -p < setup-database.sql" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Step 4: Creating tables..." -ForegroundColor Yellow
& $mysql -u cms_user -pcms_pass customer_management_db < "$projectDir\backend\src\main\resources\schema.sql"
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Tables created successfully" -ForegroundColor Green
} else {
    Write-Host "  ✗ Table creation failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 5: Loading sample data..." -ForegroundColor Yellow
& $mysql -u cms_user -pcms_pass customer_management_db < "$projectDir\backend\src\main\resources\data.sql"
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Sample data loaded" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Sample data loading failed (optional)" -ForegroundColor Yellow
}

# Verify setup
Write-Host ""
Write-Host "Step 6: Verifying setup..." -ForegroundColor Yellow
$result = & $mysql -u cms_user -pcms_pass customer_management_db -e "SHOW TABLES;" 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Database verified successfully" -ForegroundColor Green
    Write-Host ""
    Write-Host "Tables created:" -ForegroundColor Cyan
    Write-Host $result
} else {
    Write-Host "  ✗ Verification failed" -ForegroundColor Red
    exit 1
}

# Instructions for next steps
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✓ Database Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Start Backend (in a new terminal):" -ForegroundColor Cyan
Write-Host "   cd `"$projectDir\backend`"" -ForegroundColor White
Write-Host "   mvn clean install -DskipTests" -ForegroundColor White
Write-Host "   mvn spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "2. Start Frontend (in another new terminal):" -ForegroundColor Cyan
Write-Host "   cd `"$projectDir\frontend`"" -ForegroundColor White
Write-Host "   npm start" -ForegroundColor White
Write-Host ""
Write-Host "3. Open browser: http://localhost:3000" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "For detailed guide, see COMPLETE_SETUP.md" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
