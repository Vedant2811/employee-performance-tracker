package com.hivel.employee_performance_tracker.service.impl;

import com.hivel.employee_performance_tracker.dto.request.CreateReviewRequest;
import com.hivel.employee_performance_tracker.dto.response.ReviewResponse;
import com.hivel.employee_performance_tracker.entity.Employee;
import com.hivel.employee_performance_tracker.entity.PerformanceReview;
import com.hivel.employee_performance_tracker.entity.ReviewCycle;
import com.hivel.employee_performance_tracker.exception.ResourceNotFoundException;
import com.hivel.employee_performance_tracker.repository.EmployeeRepository;
import com.hivel.employee_performance_tracker.repository.PerformanceReviewRepository;
import com.hivel.employee_performance_tracker.repository.ReviewCycleRepository;
import com.hivel.employee_performance_tracker.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final PerformanceReviewRepository reviewRepository;
    private final EmployeeRepository employeeRepository;
    private final ReviewCycleRepository cycleRepository;

    @Override
    @Transactional
    public ReviewResponse submitReview(CreateReviewRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + request.getEmployeeId()));

        ReviewCycle cycle = cycleRepository.findById(request.getReviewCycleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review cycle not found with id: " + request.getReviewCycleId()));

        Employee reviewer = employeeRepository.findById(request.getReviewerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reviewer not found with id: " + request.getReviewerId()));

        PerformanceReview review = PerformanceReview.builder()
                .employee(employee)
                .reviewCycle(cycle)
                .reviewer(reviewer)
                .rating(request.getRating().shortValue())
                .notes(request.getNotes())
                .build();

        PerformanceReview saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException(
                    "Employee not found with id: " + employeeId);
        }

        return reviewRepository
                .findByEmployeeIdWithDetails(employeeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ReviewResponse toResponse(PerformanceReview r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .employeeId(r.getEmployee().getId())
                .employeeName(r.getEmployee().getName())
                .reviewCycleId(r.getReviewCycle().getId())
                .reviewCycleName(r.getReviewCycle().getName())
                .reviewerId(r.getReviewer().getId())
                .reviewerName(r.getReviewer().getName())
                .rating((int) r.getRating())
                .notes(r.getNotes())
                .submittedAt(r.getSubmittedAt())
                .build();
    }
}