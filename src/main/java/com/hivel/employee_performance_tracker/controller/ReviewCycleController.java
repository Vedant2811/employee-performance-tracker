package com.hivel.employee_performance_tracker.controller;

import com.hivel.employee_performance_tracker.dto.response.CycleSummaryResponse;
import com.hivel.employee_performance_tracker.service.ReviewCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cycles")
@RequiredArgsConstructor
public class ReviewCycleController {

    private final ReviewCycleService reviewCycleService;

    @GetMapping("/{id}/summary")
    public ResponseEntity<CycleSummaryResponse> getCycleSummary(
            @PathVariable Long id) {
        CycleSummaryResponse response = reviewCycleService.getCycleSummary(id);
        return ResponseEntity.ok(response);
    }
}