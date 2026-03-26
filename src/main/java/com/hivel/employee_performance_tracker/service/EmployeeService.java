package com.hivel.employee_performance_tracker.service;

import com.hivel.employee_performance_tracker.dto.request.CreateEmployeeRequest;
import com.hivel.employee_performance_tracker.dto.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    List<EmployeeResponse> filterEmployees(String department, double minRating);
}