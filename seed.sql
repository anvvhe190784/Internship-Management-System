-- =============================================================================
-- TSUBASA PLUS - Internship Management System (Quản lý Thực tập)
-- Sample Seed Data Script for PostgreSQL
-- =============================================================================

-- 1. Insert Users (BCrypt hashes for password 'Password123!')
INSERT INTO users (user_id, username, password_hash, full_name, email, phone_number, role, is_active) VALUES
(1, 'admin', '$2a$10$7R0wF4Wk/VjQ5bL6V2.3e.aVzN6d9ZpG2N4a7b8c9d0e1f2g3h4i5', 'Quản Trị Viên', 'admin@university.edu.vn', '0901234567', 'ADMIN', TRUE),
(2, 'mentor01', '$2a$10$7R0wF4Wk/VjQ5bL6V2.3e.aVzN6d9ZpG2N4a7b8c9d0e1f2g3h4i5', 'Nguyễn Văn Hướng', 'mentor01@university.edu.vn', '0912345678', 'MENTOR', TRUE),
(3, 'mentor02', '$2a$10$7R0wF4Wk/VjQ5bL6V2.3e.aVzN6d9ZpG2N4a7b8c9d0e1f2g3h4i5', 'Trần Thị Mai', 'mentormai@university.edu.vn', '0923456789', 'MENTOR', TRUE),
(4, 'student01', '$2a$10$7R0wF4Wk/VjQ5bL6V2.3e.aVzN6d9ZpG2N4a7b8c9d0e1f2g3h4i5', 'Lê Văn Nam', 'studentnam@student.edu.vn', '0934567890', 'STUDENT', TRUE),
(5, 'student02', '$2a$10$7R0wF4Wk/VjQ5bL6V2.3e.aVzN6d9ZpG2N4a7b8c9d0e1f2g3h4i5', 'Phạm Hoàng Anh', 'studentanh@student.edu.vn', '0945678901', 'STUDENT', TRUE);

SELECT setval('users_user_id_seq', (SELECT MAX(user_id) FROM users));

-- 2. Insert Students
INSERT INTO students (student_id, student_code, major, class, date_of_birth, address) VALUES
(4, 'K20CNTT001', 'Công nghệ Thông tin', 'K20-CNTT1', '2002-05-15', '123 Đường Nguyễn Trãi, Hà Nội'),
(5, 'K20CNTT002', 'Khoa học Máy tính', 'K20-KHMT1', '2002-08-20', '456 Đường Cầu Giấy, Hà Nội');

-- 3. Insert Mentors
INSERT INTO mentors (mentor_id, department, academic_rank) VALUES
(2, 'Bộ môn Hợp tác Doanh nghiệp & Thực tập', 'Thạc sĩ'),
(3, 'Khoa Công nghệ Thông tin', 'Tiến sĩ');

-- 4. Insert Internship Phases
INSERT INTO internship_phases (phase_id, phase_name, start_date, end_date, description) VALUES
(1, 'Thực tập Cơ sở HK1 2025-2026', '2025-09-01', '2025-11-30', 'Giai đoạn thực tập cơ sở tại các doanh nghiệp đối tác dành cho sinh viên năm 3'),
(2, 'Thực tập Tốt nghiệp HK2 2025-2026', '2026-02-01', '2026-05-31', 'Giai đoạn thực tập tốt nghiệp và làm đồ án tốt nghiệp tại doanh nghiệp');

SELECT setval('internship_phases_phase_id_seq', (SELECT MAX(phase_id) FROM internship_phases));

-- 5. Insert Evaluation Criteria
INSERT INTO evaluation_criteria (criterion_id, criterion_name, description, max_score) VALUES
(1, 'Kỷ luật & Ý thức làm việc', 'Đi làm đúng giờ, tuân thủ nội quy doanh nghiệp và quy định thực tập', 10.00),
(2, 'Kỹ năng Chuyên môn & Kỹ thuật', 'Khả năng ứng dụng kiến thức lập trình, giải quyết vấn đề kỹ thuật', 10.00),
(3, 'Kỹ năng Giao tiếp & Làm việc nhóm', 'Khả năng trình bày, phối hợp làm việc với đồng nghiệp và mentor', 10.00),
(4, 'Báo cáo & Sản phẩm Thực tập', 'Chất lượng tài liệu báo cáo thực tập và sản phẩm bàn giao', 10.00);

SELECT setval('evaluation_criteria_criterion_id_seq', (SELECT MAX(criterion_id) FROM evaluation_criteria));

-- 6. Insert Assessment Rounds
INSERT INTO assessment_rounds (round_id, phase_id, round_name, start_date, end_date, description, is_active) VALUES
(1, 1, 'Đánh giá Giữa kỳ', '2025-10-01', '2025-10-15', 'Đánh giá tiến độ thực tập giai đoạn 1', TRUE),
(2, 1, 'Đánh giá Cuối kỳ', '2025-11-15', '2025-11-30', 'Đánh giá tổng kết kết quả thực tập giai đoạn 1', TRUE);

SELECT setval('assessment_rounds_round_id_seq', (SELECT MAX(round_id) FROM assessment_rounds));

-- 7. Insert Round Criteria
INSERT INTO round_criteria (round_criterion_id, round_id, criterion_id, weight) VALUES
(1, 1, 1, 0.40),
(2, 1, 2, 0.60),
(3, 2, 1, 0.20),
(4, 2, 2, 0.40),
(5, 2, 3, 0.20),
(6, 2, 4, 0.20);

SELECT setval('round_criteria_round_criterion_id_seq', (SELECT MAX(round_criterion_id) FROM round_criteria));

-- 8. Insert Internship Assignments
INSERT INTO internship_assignments (assignment_id, student_id, mentor_id, phase_id, assigned_date, status) VALUES
(1, 4, 2, 1, '2025-09-02 09:00:00+07', 'IN_PROGRESS'),
(2, 5, 3, 1, '2025-09-02 09:30:00+07', 'IN_PROGRESS');

SELECT setval('internship_assignments_assignment_id_seq', (SELECT MAX(assignment_id) FROM internship_assignments));

-- 9. Insert Assessment Results
INSERT INTO assessment_results (result_id, assignment_id, round_id, criterion_id, score, comments, evaluated_by, evaluation_date) VALUES
(1, 1, 1, 1, 9.50, 'Sinh viên chấp hành tốt nội quy, đi làm đầy đủ.', 2, '2025-10-10 14:00:00+07'),
(2, 1, 1, 2, 8.50, 'Nắm vững kiến thức Java Spring Boot, hoàn thành task đúng hạn.', 2, '2025-10-10 14:05:00+07');

SELECT setval('assessment_results_result_id_seq', (SELECT MAX(result_id) FROM assessment_results));
