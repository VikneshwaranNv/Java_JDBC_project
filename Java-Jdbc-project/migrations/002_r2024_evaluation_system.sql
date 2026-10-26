-- ========================================================================
-- KEC ACADEMIX Database Migration: 002_r2024_evaluation_system.sql
-- Description: Add course_type, enrollment semester, attendance table,
--              and absent support for KEC R2024 Academic Evaluation System
-- ========================================================================

-- 1. Add course_type to courses table if not present
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'kec_academic' AND TABLE_NAME = 'courses' AND COLUMN_NAME = 'course_type'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE courses ADD COLUMN course_type ENUM(\'THEORY\', \'PRACTICAL\', \'EMBEDDED\') NOT NULL DEFAULT \'THEORY\'', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Add semester to enrollments table if not present
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'kec_academic' AND TABLE_NAME = 'enrollments' AND COLUMN_NAME = 'semester'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE enrollments ADD COLUMN semester INT NOT NULL DEFAULT 4', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Create attendance table
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL UNIQUE,
    classes_held INT NOT NULL DEFAULT 0,
    classes_attended INT NOT NULL DEFAULT 0,
    condonation_approved TINYINT(1) NOT NULL DEFAULT 0,
    condonation_reason VARCHAR(255) NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_enrollment FOREIGN KEY (enrollment_id)
        REFERENCES enrollments(enrollment_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_attendance_enrollment (enrollment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Add is_absent flag to assessment_marks if not present
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'kec_academic' AND TABLE_NAME = 'assessment_marks' AND COLUMN_NAME = 'is_absent'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assessment_marks ADD COLUMN is_absent TINYINT(1) NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. Seed default attendance records for existing enrollments so data is never null or orphaned
INSERT INTO attendance (enrollment_id, classes_held, classes_attended, condonation_approved)
SELECT e.enrollment_id, 45, 40, 0
FROM enrollments e
LEFT JOIN attendance a ON a.enrollment_id = e.enrollment_id
WHERE a.attendance_id IS NULL;

