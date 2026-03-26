package com.hivel.employee_performance_tracker.service;

import com.hivel.employee_performance_tracker.dto.request.CreateReviewRequest;
import com.hivel.employee_performance_tracker.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse submitReview(CreateReviewRequest request);

    List<ReviewResponse> getReviewsByEmployee(Long employeeId);
}