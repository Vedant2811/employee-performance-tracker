package com.hivel.employee_performance_tracker.repository;

import com.hivel.employee_performance_tracker.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("""
        SELECT e
        FROM Employee e
        JOIN PerformanceReview r ON r.employee = e
        WHERE e.department = :department
        GROUP BY e
        HAVING AVG(r.rating) >= :minRating
    """)
    List<Employee> findByDepartmentAndMinRating(
            @Param("department") String department,
            @Param("minRating") double minRating
    );
}