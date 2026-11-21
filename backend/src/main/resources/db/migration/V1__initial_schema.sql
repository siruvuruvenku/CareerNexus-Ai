CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(254) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(32) NOT NULL DEFAULT 'USER',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE user_profiles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  full_name VARCHAR(160) NOT NULL,
  target_title VARCHAR(160),
  country_code VARCHAR(2),
  city VARCHAR(160),
  years_experience INT NOT NULL DEFAULT 0,
  summary TEXT,
  resume_path VARCHAR(500),
  CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE countries (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(2) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL
);
CREATE TABLE locations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  country_id BIGINT NOT NULL,
  city VARCHAR(160) NOT NULL,
  CONSTRAINT fk_location_country FOREIGN KEY (country_id) REFERENCES countries(id),
  CONSTRAINT uq_location UNIQUE(country_id, city),
  INDEX ix_location_city(city)
);
CREATE TABLE skills (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  category VARCHAR(80) NOT NULL,
  custom BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE user_skills (
  user_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  PRIMARY KEY(user_id, skill_id),
  CONSTRAINT fk_user_skill_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_skill_skill FOREIGN KEY (skill_id) REFERENCES skills(id)
);
CREATE TABLE companies (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  website VARCHAR(500),
  INDEX ix_company_name(name)
);
CREATE TABLE jobs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id BIGINT,
  title VARCHAR(240) NOT NULL,
  country_code VARCHAR(2),
  location VARCHAR(240),
  description MEDIUMTEXT,
  canonical_url VARCHAR(700) NOT NULL UNIQUE,
  employment_type VARCHAR(40),
  workplace_type VARCHAR(40),
  salary_min DECIMAL(14,2),
  salary_max DECIMAL(14,2),
  salary_currency VARCHAR(3),
  required_experience_min INT,
  required_experience_max INT,
  preferred_experience_min INT,
  preferred_experience_max INT,
  posted_at TIMESTAMP NULL,
  last_verified_at TIMESTAMP NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_job_company FOREIGN KEY (company_id) REFERENCES companies(id),
  INDEX ix_job_location(country_code, location),
  INDEX ix_job_posted(posted_at),
  INDEX ix_job_company(company_id),
  INDEX ix_job_active_verified(active, last_verified_at),
  INDEX ix_job_title(title(100))
);
CREATE TABLE job_sources (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  job_id BIGINT NOT NULL,
  provider VARCHAR(40) NOT NULL,
  provider_job_id VARCHAR(240) NOT NULL,
  original_url VARCHAR(1000) NOT NULL,
  last_verified_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_source_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  CONSTRAINT uq_provider_job UNIQUE(provider, provider_job_id),
  INDEX ix_job_source_job(job_id)
);
CREATE TABLE job_required_skills (
  job_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  PRIMARY KEY(job_id, skill_id),
  CONSTRAINT fk_required_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  CONSTRAINT fk_required_skill FOREIGN KEY (skill_id) REFERENCES skills(id)
);
CREATE TABLE job_additional_skills (
  job_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  PRIMARY KEY(job_id, skill_id),
  CONSTRAINT fk_additional_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  CONSTRAINT fk_additional_skill FOREIGN KEY (skill_id) REFERENCES skills(id)
);
CREATE TABLE saved_jobs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_saved_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_saved_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  CONSTRAINT uq_saved_user_job UNIQUE(user_id, job_id),
  INDEX ix_saved_user_created(user_id, created_at)
);
CREATE TABLE job_applications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  interview_at TIMESTAMP NULL,
  notes TEXT,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  CONSTRAINT uq_application_user_job UNIQUE(user_id, job_id),
  INDEX ix_application_user_status(user_id, status)
);
CREATE TABLE search_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  query_text VARCHAR(500),
  searched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_search_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX ix_search_user_date(user_id, searched_at)
);
CREATE TABLE job_recommendations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  match_score DECIMAL(5,2) NOT NULL,
  explanation_json JSON NOT NULL,
  generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_recommendation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_recommendation_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  CONSTRAINT uq_recommendation_user_job UNIQUE(user_id, job_id),
  INDEX ix_recommendation_user_score(user_id, match_score)
);

