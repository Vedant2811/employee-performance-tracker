package com.hivel.employee_performance_tracker.service.impl;

import com.hivel.employee_performance_tracker.dto.request.CreateEmployeeRequest;
import com.hivel.employee_performance_tracker.dto.response.EmployeeResponse;
import com.hivel.employee_performance_tracker.entity.Employee;
import com.hivel.employee_performance_tracker.repository.EmployeeRepository;
import com.hivel.employee_performance_tracker.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        Employee employee = Employee.builder()
                .name(request.getName())
                .department(request.getDepartment())
                .role(request.getRole())
                .joiningDate(request.getJoiningDate())
                .build();

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> filterEmployees(String department, double minRating) {
        return employeeRepository
                .findByDepartmentAndMinRating(department, minRating)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .department(e.getDepartment())
                .role(e.getRole())
                .joiningDate(e.getJoiningDate())
                .build();
    }
}