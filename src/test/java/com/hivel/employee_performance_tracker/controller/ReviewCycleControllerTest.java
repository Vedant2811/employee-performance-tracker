package com.hivel.employee_performance_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.hivel.employee_performance_tracker.dto.response.CycleSummaryResponse;
import com.hivel.employee_performance_tracker.exception.GlobalExceptionHandler;
import com.hivel.employee_performance_tracker.exception.ResourceNotFoundException;
import com.hivel.employee_performance_tracker.service.ReviewCycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewCycleControllerTest {

    @Mock
    private ReviewCycleService reviewCycleService;

    @InjectMocks
    private ReviewCycleController reviewCycleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(reviewCycleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getCycleSummary_returnsSummary() throws Exception {
        CycleSummaryResponse response = CycleSummaryResponse.builder()
                .cycleId(7L)
                .cycleName("Q1 2026")
                .averageRating(4.6)
                .totalReviews(12L)
                .topPerformer(CycleSummaryResponse.TopPerformer.builder()
                        .id(3L)
                        .name("Alice Johnson")
                        .department("Engineering")
                        .build())
                .completedGoals(25L)
                .missedGoals(4L)
                .build();

        given(reviewCycleService.getCycleSummary(7L)).willReturn(response);

        mockMvc.perform(get("/cycles/{id}/summary", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleId").value(7))
                .andExpect(jsonPath("$.cycleName").value("Q1 2026"))
                .andExpect(jsonPath("$.averageRating").value(4.6))
                .andExpect(jsonPath("$.totalReviews").value(12))
                .andExpect(jsonPath("$.topPerformer.id").value(3))
                .andExpect(jsonPath("$.topPerformer.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.completedGoals").value(25))
                .andExpect(jsonPath("$.missedGoals").value(4));

        verify(reviewCycleService).getCycleSummary(7L);
    }

    @Test
    void getCycleSummary_returnsNotFoundWhenCycleMissing() throws Exception {
        given(reviewCycleService.getCycleSummary(99L))
                .willThrow(new ResourceNotFoundException("Review cycle not found with id 99"));

        mockMvc.perform(get("/cycles/{id}/summary", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Review cycle not found with id 99"));

        verify(reviewCycleService).getCycleSummary(99L);
    }
}
