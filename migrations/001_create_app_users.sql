-- ========================================================================
-- KEC ACADEMIX Database Migration: 001_create_app_users.sql
-- Description: Create persistent app_users table for role-based authentication
-- ========================================================================

CREATE TABLE IF NOT EXISTS app_users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMINISTRATOR', 'STAFF', 'STUDENT') NOT NULL,
    student_id INT NULL UNIQUE,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_users_student 
        FOREIGN KEY (student_id) REFERENCES students(student_id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    INDEX idx_app_users_username (username),
    INDEX idx_app_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

