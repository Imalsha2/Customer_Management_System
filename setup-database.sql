-- ========================================
-- MariaDB Setup Script
-- Customer Management System
-- ========================================

-- Drop existing database if exists (CAREFUL: This deletes all data!)
-- DROP DATABASE IF EXISTS customer_management_db;

-- Create database with UTF8 support
CREATE DATABASE IF NOT EXISTS customer_management_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Create user for application
DROP USER IF EXISTS 'cms_user'@'localhost';
CREATE USER 'cms_user'@'localhost' IDENTIFIED BY 'cms_pass';

-- Grant all privileges on the database
GRANT ALL PRIVILEGES ON customer_management_db.* TO 'cms_user'@'localhost';
FLUSH PRIVILEGES;

-- Switch to database
USE customer_management_db;

-- Show current database
SELECT DATABASE() as 'Current Database';

-- Verify user permissions
SHOW GRANTS FOR 'cms_user'@'localhost';

-- Show message
SELECT 'Database and user created successfully!' as 'Status';
