package com.hivel.employee_performance_tracker.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CycleSummaryResponse {
    private Long cycleId;
    private String cycleName;
    private Double averageRating;
    private Long totalReviews;
    private TopPerformer topPerformer;
    private Long completedGoals;
    private Long missedGoals;

    @Data
    @Builder
    public static class TopPerformer {
        private Long id;
        private String name;
        private String department;
    }
}