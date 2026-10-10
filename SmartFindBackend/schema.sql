-- =====================================================================
-- SmartFind - AI-Based Smart Lost & Found System
-- Database Creation & Schema Script (MySQL)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS smartfind_db;
USE smartfind_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Item Reports Table
CREATE TABLE IF NOT EXISTS item_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(150) NOT NULL,
    item_date DATE NOT NULL,
    report_type VARCHAR(10) NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_reports_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Optional: Initial Seed Check
-- Note: Spring Boot's DatabaseInitializer will also automatically seed default accounts 
-- and demo matching items if tables are empty upon boot.
