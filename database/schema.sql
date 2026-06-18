CREATE DATABASE IF NOT EXISTS ai_test_case
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE ai_test_case;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  creator_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_project_creator_id (creator_id)
);

CREATE TABLE IF NOT EXISTS requirement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_requirement_project_id (project_id)
);

CREATE TABLE IF NOT EXISTS test_case (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  requirement_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  type VARCHAR(50) NOT NULL,
  precondition TEXT,
  steps TEXT,
  test_data TEXT,
  expected_result TEXT,
  priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
  adoption_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_test_case_project_id (project_id),
  INDEX idx_test_case_requirement_id (requirement_id),
  INDEX idx_test_case_adoption_status (adoption_status)
);

CREATE TABLE IF NOT EXISTS ai_generation_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  requirement_id BIGINT NOT NULL,
  model_name VARCHAR(50) NOT NULL,
  prompt TEXT NOT NULL,
  response_content LONGTEXT,
  status VARCHAR(20) NOT NULL,
  error_message TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_ai_record_project_id (project_id),
  INDEX idx_ai_record_requirement_id (requirement_id),
  INDEX idx_ai_record_status (status)
);

INSERT INTO sys_user (username, password, real_name, role)
SELECT 'admin', '123456', '贾舒凯', 'ADMIN'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_user WHERE username = 'admin'
);
