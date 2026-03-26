package com.hivel.employee_performance_tracker.controller;

import com.hivel.employee_performance_tracker.dto.request.CreateEmployeeRequest;
import com.hivel.employee_performance_tracker.dto.response.EmployeeResponse;
import com.hivel.employee_performance_tracker.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> filterEmployees(
            @RequestParam String department,
            @RequestParam double minRating) {
        List<EmployeeResponse> response = employeeService
                .filterEmployees(department, minRating);
        return ResponseEntity.ok(response);
    }
}