package com.hivel.employee_performance_tracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long reviewCycleId;
    private String reviewCycleName;
    private Long reviewerId;
    private String reviewerName;
    private Integer rating;
    private String notes;
    private LocalDateTime submittedAt;
}