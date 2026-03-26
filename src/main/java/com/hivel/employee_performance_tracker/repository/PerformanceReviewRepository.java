package com.hivel.employee_performance_tracker.repository;

import com.hivel.employee_performance_tracker.entity.Employee;
import com.hivel.employee_performance_tracker.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    // Fetch all reviews for an employee — JOIN FETCH prevents N+1
    // on reviewCycle when we access cycle details in the response
    @Query("""
        SELECT r FROM PerformanceReview r
        JOIN FETCH r.reviewCycle
        JOIN FETCH r.reviewer
        WHERE r.employee.id = :employeeId
        ORDER BY r.submittedAt DESC
    """)
    List<PerformanceReview> findByEmployeeIdWithDetails(
            @Param("employeeId") Long employeeId
    );

    // Average rating for a cycle — used in summary endpoint
    @Query("""
        SELECT AVG(r.rating)
        FROM PerformanceReview r
        WHERE r.reviewCycle.id = :cycleId
    """)
    Optional<Double> findAverageRatingByCycleId(
            @Param("cycleId") Long cycleId
    );

    // Top performer in a cycle — employee with highest avg rating
    @Query("""
        SELECT r.employee
        FROM PerformanceReview r
        WHERE r.reviewCycle.id = :cycleId
        GROUP BY r.employee
        ORDER BY AVG(r.rating) DESC
        LIMIT 1
    """)
    Optional<Employee> findTopPerformerByCycleId(
            @Param("cycleId") Long cycleId
    );

    // Total review count for a cycle
    @Query("""
        SELECT COUNT(r)
        FROM PerformanceReview r
        WHERE r.reviewCycle.id = :cycleId
    """)
    Long countByCycleId(@Param("cycleId") Long cycleId);
}