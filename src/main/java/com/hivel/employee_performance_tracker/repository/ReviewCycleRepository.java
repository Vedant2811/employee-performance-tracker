package com.hivel.employee_performance_tracker.repository;

import com.hivel.employee_performance_tracker.entity.ReviewCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCycleRepository extends JpaRepository<ReviewCycle, Long> {
}