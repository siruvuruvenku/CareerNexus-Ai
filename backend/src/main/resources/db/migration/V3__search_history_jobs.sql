CREATE TABLE search_history_jobs (
  search_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  PRIMARY KEY(search_id, job_id),
  CONSTRAINT fk_search_job_search FOREIGN KEY (search_id) REFERENCES search_history(id) ON DELETE CASCADE,
  CONSTRAINT fk_search_job_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
  INDEX ix_search_job_job(job_id)
);

