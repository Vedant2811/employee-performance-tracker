package com.hivel.employee_performance_tracker.repository;

import com.hivel.employee_performance_tracker.entity.Goal;
import com.hivel.employee_performance_tracker.entity.Goal.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    // Returns rows of [GoalStatus, count] — used in summary endpoint
    // Only fetches completed and missed, skipping pending
    @Query("""
        SELECT g.status, COUNT(g)
        FROM Goal g
        WHERE g.reviewCycle.id = :cycleId
          AND g.status IN ('completed', 'missed')
        GROUP BY g.status
    """)
    List<Object[]> countGoalsByCycleIdGroupedByStatus(
            @Param("cycleId") Long cycleId
    );
}