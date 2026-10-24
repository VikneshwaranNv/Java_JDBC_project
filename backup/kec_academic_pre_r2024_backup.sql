-- ====================================================================
-- KEC ACADEMIX Pre-R2024 Database Backup
-- Timestamp: Fri Oct 23 18:11:33 IST 2026
-- Database: kec_academic
-- ====================================================================
SET FOREIGN_KEY_CHECKS = 0;

--
-- Data for table `app_users`
--
INSERT INTO `app_users` (`user_id`, `username`, `password_hash`, `role`, `student_id`, `is_active`, `created_at`, `updated_at`) VALUES (4, 'test_stud_arun', '$2a$12$fyZitkStQFC1kMY0ukwvDuuvt19PAeC7uCtvf2mJvygy7eJFUSimK', 'STUDENT', 1, 1, '2026-10-18 20:26:17.0', '2026-10-18 20:26:17.0');
INSERT INTO `app_users` (`user_id`, `username`, `password_hash`, `role`, `student_id`, `is_active`, `created_at`, `updated_at`) VALUES (5, 'test_admin_sys', '$2a$12$HMeYgnRzvn0BW.0.8hoMIe43ZsLid7ThqvcU6GNAYYuNdiCpr3NEu', 'ADMINISTRATOR', NULL, 1, '2026-10-18 20:26:18.0', '2026-10-18 20:26:18.0');
INSERT INTO `app_users` (`user_id`, `username`, `password_hash`, `role`, `student_id`, `is_active`, `created_at`, `updated_at`) VALUES (6, 'test_staff_kavitha', '$2a$12$jBsRJjFE.RqM3HH8lCHzOeFWSrrafLL3d74cS589tSWmXDQAMbzhO', 'STAFF', NULL, 1, '2026-10-18 20:26:18.0', '2026-10-18 20:26:18.0');
INSERT INTO `app_users` (`user_id`, `username`, `password_hash`, `role`, `student_id`, `is_active`, `created_at`, `updated_at`) VALUES (7, 'Vikneshwaran N', '$2a$12$1UqL4g0fqILPkqIichzFGOu2H5Leog2tQmHoLWG.2YVzkCP8mKmCy', 'STUDENT', 4, 1, '2026-10-18 20:29:29.0', '2026-10-18 20:58:04.0');
INSERT INTO `app_users` (`user_id`, `username`, `password_hash`, `role`, `student_id`, `is_active`, `created_at`, `updated_at`) VALUES (10, 'Thina Rajkumar R', '$2a$12$HFYdft4ogqhso9sF8H7CM.zjATRzNbizecmZOazbBbel8uM2dnxXO', 'STUDENT', 6, 1, '2026-10-18 22:03:51.0', '2026-10-18 22:03:51.0');

--
-- Data for table `students`
--
INSERT INTO `students` (`student_id`, `roll_no`, `student_name`, `department`, `semester`) VALUES (1, '24CSE001', 'Arun Kumar', 'CSE', 4);
INSERT INTO `students` (`student_id`, `roll_no`, `student_name`, `department`, `semester`) VALUES (2, '24CSE002', 'Priya Devi', 'CSE', 4);
INSERT INTO `students` (`student_id`, `roll_no`, `student_name`, `department`, `semester`) VALUES (3, '24IT001', 'Karthik Raj', 'IT', 4);
INSERT INTO `students` (`student_id`, `roll_no`, `student_name`, `department`, `semester`) VALUES (4, '25ITR173', 'VIKNESHWARAN N', 'IT', 4);
INSERT INTO `students` (`student_id`, `roll_no`, `student_name`, `department`, `semester`) VALUES (6, '25ITR161', 'Thina Rajkumar R', 'Information Technology', 4);

--
-- Data for table `courses`
--
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`) VALUES (1, 'CS2401', 'Java Programming', 4);
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`) VALUES (2, 'CS2402', 'Database Management Systems', 3);

--
-- Data for table `enrollments`
--
INSERT INTO `enrollments` (`enrollment_id`, `student_id`, `course_id`, `academic_year`) VALUES (1, 1, 1, '2026-2027');
INSERT INTO `enrollments` (`enrollment_id`, `student_id`, `course_id`, `academic_year`) VALUES (2, 1, 2, '2026-2027');
INSERT INTO `enrollments` (`enrollment_id`, `student_id`, `course_id`, `academic_year`) VALUES (3, 2, 1, '2026-2027');
INSERT INTO `enrollments` (`enrollment_id`, `student_id`, `course_id`, `academic_year`) VALUES (4, 2, 2, '2026-2027');
INSERT INTO `enrollments` (`enrollment_id`, `student_id`, `course_id`, `academic_year`) VALUES (5, 3, 1, '2026-2027');

--
-- Data for table `assessment_marks`
--
INSERT INTO `assessment_marks` (`mark_id`, `enrollment_id`, `assessment_name`, `max_marks`, `marks_obtained`) VALUES (1, 1, 'Internal Assessment 1', 50.00, 42.00);
INSERT INTO `assessment_marks` (`mark_id`, `enrollment_id`, `assessment_name`, `max_marks`, `marks_obtained`) VALUES (2, 1, 'Assignment 1', 10.00, 9.00);
INSERT INTO `assessment_marks` (`mark_id`, `enrollment_id`, `assessment_name`, `max_marks`, `marks_obtained`) VALUES (3, 2, 'Internal Assessment 1', 50.00, 45.00);
INSERT INTO `assessment_marks` (`mark_id`, `enrollment_id`, `assessment_name`, `max_marks`, `marks_obtained`) VALUES (4, 3, 'Internal Assessment 1', 50.00, 38.00);
INSERT INTO `assessment_marks` (`mark_id`, `enrollment_id`, `assessment_name`, `max_marks`, `marks_obtained`) VALUES (5, 4, 'Internal Assessment 1', 50.00, 44.00);
INSERT INTO `assessment_marks` (`mark_id`, `enrollment_id`, `assessment_name`, `max_marks`, `marks_obtained`) VALUES (6, 5, 'Internal Assessment 1', 50.00, 40.00);
INSERT INTO `assessment_marks` (`mark_id`, `enrollment_id`, `assessment_name`, `max_marks`, `marks_obtained`) VALUES (7, 1, 'CIA1', 100.00, 36.00);
INSERT INTO `assessment_marks` (`mark_id`, `enrollment_id`, `assessment_name`, `max_marks`, `marks_obtained`) VALUES (8, 1, 'CIA2', 100.00, 45.00);
INSERT INTO `assessment_marks` (`mark_id`, `enrollment_id`, `assessment_name`, `max_marks`, `marks_obtained`) VALUES (9, 1, 'CIA3', 100.00, 40.00);
INSERT INTO `assessment_marks` (`mark_id`, `enrollment_id`, `assessment_name`, `max_marks`, `marks_obtained`) VALUES (10, 1, 'ESE', 60.00, 47.00);

--
-- Data for table `sap_activities`
--
INSERT INTO `sap_activities` (`activity_id`, `student_id`, `activity_name`, `activity_type`, `activity_date`, `organizer`, `description`, `certificate_original_name`, `certificate_path`, `certificate_mime_type`, `certificate_size_bytes`, `certificate_sha256`, `extracted_text`, `analysis_status`, `eligibility_status`, `exclusion_reason`, `duplicate_flag`, `analysis_confidence`, `analysis_notes`, `claimed_points`, `recommended_points`, `approved_points`, `status`, `student_comments`, `reviewer_comments`, `reviewed_by`, `reviewed_at`, `created_at`, `updated_at`) VALUES (1, 1, '[TEST] Paper Presentation on Cloud Security', 'TECHNICAL', '2026-10-18', 'KEC ISTE Chapter', 'Test submission to verify genuine SAP storage without placeholders.', 'sample_certificate.txt', 'uploads/24CSE001_1792323512124_sample_certificate.txt', 'text/plain', 190, '781394b2b04b93d42fe0ff896d3e223ce1fa4978627887543cc3be4b32a49b16', 'Audited Verification Test Run Text', 'ANALYZED', 'ELIGIBLE', NULL, 0, 85.00, 'Automated audit test verification.', 20, 0, 0, 'PENDING_REVIEW', NULL, NULL, NULL, NULL, '2026-10-18 17:08:32.0', '2026-10-18 19:03:40.0');

--
-- Data for table `sap_activity_reviews`
--

--
-- Data for table `sap_bonus_rules`
--
INSERT INTO `sap_bonus_rules` (`rule_id`, `minimum_points`, `maximum_points`, `bonus_level`, `rule_description`, `active`) VALUES (1, 0, 0, 1, 'Zero approved points', 1);
INSERT INTO `sap_bonus_rules` (`rule_id`, `minimum_points`, `maximum_points`, `bonus_level`, `rule_description`, `active`) VALUES (2, 1, 50, 2, '1 to 50 approved points', 1);
INSERT INTO `sap_bonus_rules` (`rule_id`, `minimum_points`, `maximum_points`, `bonus_level`, `rule_description`, `active`) VALUES (3, 51, 60, 3, '51 to 60 approved points', 1);
INSERT INTO `sap_bonus_rules` (`rule_id`, `minimum_points`, `maximum_points`, `bonus_level`, `rule_description`, `active`) VALUES (4, 61, 100, 4, '61 to 100 approved points', 1);
INSERT INTO `sap_bonus_rules` (`rule_id`, `minimum_points`, `maximum_points`, `bonus_level`, `rule_description`, `active`) VALUES (5, 101, 150, 5, '101 to 150 approved points', 1);

SET FOREIGN_KEY_CHECKS = 1;
-- Backup complete.
