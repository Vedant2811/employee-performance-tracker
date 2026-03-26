package com.hivel.employee_performance_tracker.controller;

import com.hivel.employee_performance_tracker.dto.request.CreateReviewRequest;
import com.hivel.employee_performance_tracker.dto.response.ReviewResponse;
import com.hivel.employee_performance_tracker.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> submitReview(
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.submitReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/employees/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviewsByEmployee(
            @PathVariable Long id) {
        List<ReviewResponse> response = reviewService.getReviewsByEmployee(id);
        return ResponseEntity.ok(response);
    }
}