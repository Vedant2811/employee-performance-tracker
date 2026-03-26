package com.hivel.employee_performance_tracker.service.impl;

import com.hivel.employee_performance_tracker.dto.response.CycleSummaryResponse;
import com.hivel.employee_performance_tracker.entity.Employee;
import com.hivel.employee_performance_tracker.entity.Goal.GoalStatus;
import com.hivel.employee_performance_tracker.entity.ReviewCycle;
import com.hivel.employee_performance_tracker.exception.ResourceNotFoundException;
import com.hivel.employee_performance_tracker.repository.GoalRepository;
import com.hivel.employee_performance_tracker.repository.PerformanceReviewRepository;
import com.hivel.employee_performance_tracker.repository.ReviewCycleRepository;
import com.hivel.employee_performance_tracker.service.ReviewCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewCycleServiceImpl implements ReviewCycleService {

    private final ReviewCycleRepository cycleRepository;
    private final PerformanceReviewRepository reviewRepository;
    private final GoalRepository goalRepository;

    @Override
    @Transactional(readOnly = true)
    public CycleSummaryResponse getCycleSummary(Long cycleId) {
        ReviewCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review cycle not found with id: " + cycleId));

        // 3 targeted queries — no N+1, all aggregation in Postgres
        Double avgRating = reviewRepository
                .findAverageRatingByCycleId(cycleId)
                .orElse(0.0);

        Long totalReviews = reviewRepository.countByCycleId(cycleId);

        Employee topPerformer = reviewRepository
                .findTopPerformerByCycleId(cycleId)
                .orElse(null);

        List<Object[]> goalCounts = goalRepository
                .countGoalsByCycleIdGroupedByStatus(cycleId);

        // Map goal counts from Object[] rows to named fields
        long completedGoals = 0L;
        long missedGoals = 0L;

        for (Object[] row : goalCounts) {
            GoalStatus status = (GoalStatus) row[0];
            Long count = (Long) row[1];
            if (status == GoalStatus.completed) completedGoals = count;
            else if (status == GoalStatus.missed) missedGoals = count;
        }

        return CycleSummaryResponse.builder()
                .cycleId(cycle.getId())
                .cycleName(cycle.getName())
                .averageRating(Math.round(avgRating * 100.0) / 100.0)
                .totalReviews(totalReviews)
                .topPerformer(topPerformer == null ? null :
                        CycleSummaryResponse.TopPerformer.builder()
                        .id(topPerformer.getId())
                        .name(topPerformer.getName())
                        .department(topPerformer.getDepartment())
                        .build())
                .completedGoals(completedGoals)
                .missedGoals(missedGoals)
                .build();
    }
}