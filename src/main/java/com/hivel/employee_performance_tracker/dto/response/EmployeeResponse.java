package com.hivel.employee_performance_tracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private String name;
    private String department;
    private String role;
    private LocalDate joiningDate;
}