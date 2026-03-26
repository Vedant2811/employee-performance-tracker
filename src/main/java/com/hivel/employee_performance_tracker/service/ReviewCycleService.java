package com.hivel.employee_performance_tracker.service;

import com.hivel.employee_performance_tracker.dto.response.CycleSummaryResponse;

public interface ReviewCycleService {

    CycleSummaryResponse getCycleSummary(Long cycleId);
}