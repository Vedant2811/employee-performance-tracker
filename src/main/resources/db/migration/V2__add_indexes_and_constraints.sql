-- Indexes for employees
CREATE INDEX idx_employees_department ON employees (department);

-- Indexes for performance_reviews
CREATE INDEX idx_reviews_employee_id     ON performance_reviews (employee_id);
CREATE INDEX idx_reviews_review_cycle_id ON performance_reviews (review_cycle_id);
CREATE INDEX idx_reviews_cycle_employee  ON performance_reviews (review_cycle_id, employee_id);

-- Indexes for goals
CREATE INDEX idx_goals_review_cycle_id   ON goals (review_cycle_id);
CREATE INDEX idx_goals_employee_cycle    ON goals (employee_id, review_cycle_id);

-- Rating constraint
ALTER TABLE performance_reviews
    ADD CONSTRAINT chk_rating_range CHECK (rating BETWEEN 1 AND 5);

-- Cycle date constraint
ALTER TABLE review_cycles
    ADD CONSTRAINT chk_cycle_dates CHECK (end_date > start_date);

-- Status constraint (since ENUM wasn't created)
ALTER TABLE goals
    ADD CONSTRAINT chk_goal_status
        CHECK (status IN ('pending', 'completed', 'missed'));