package com.hivel.employee_performance_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.hivel.employee_performance_tracker.dto.request.CreateReviewRequest;
import com.hivel.employee_performance_tracker.dto.response.ReviewResponse;
import com.hivel.employee_performance_tracker.exception.GlobalExceptionHandler;
import com.hivel.employee_performance_tracker.exception.ResourceNotFoundException;
import com.hivel.employee_performance_tracker.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void submitReview_returnsCreatedReview() throws Exception {
        ReviewResponse response = ReviewResponse.builder()
                .id(10L)
                .employeeId(3L)
                .employeeName("Alice Johnson")
                .reviewCycleId(7L)
                .reviewCycleName("Q1 2026")
                .reviewerId(9L)
                .reviewerName("Manager Mike")
                .rating(5)
                .notes("Strong delivery and collaboration")
                .submittedAt(LocalDateTime.of(2026, 3, 20, 10, 15, 30))
                .build();

        given(reviewService.submitReview(any(CreateReviewRequest.class))).willReturn(response);

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": 3,
                                  "reviewCycleId": 7,
                                  "reviewerId": 9,
                                  "rating": 5,
                                  "notes": "Strong delivery and collaboration"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.employeeId").value(3))
                .andExpect(jsonPath("$.reviewCycleId").value(7))
                .andExpect(jsonPath("$.reviewerId").value(9))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.submittedAt").value("2026-03-20T10:15:30"));

        ArgumentCaptor<CreateReviewRequest> captor =
                ArgumentCaptor.forClass(CreateReviewRequest.class);
        verify(reviewService).submitReview(captor.capture());
        assertEquals(3L, captor.getValue().getEmployeeId());
        assertEquals(7L, captor.getValue().getReviewCycleId());
        assertEquals(9L, captor.getValue().getReviewerId());
        assertEquals(5, captor.getValue().getRating());
        assertEquals("Strong delivery and collaboration", captor.getValue().getNotes());
    }

    @Test
    void submitReview_returnsBadRequestWhenRatingIsTooHigh() throws Exception {
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": 3,
                                  "reviewCycleId": 7,
                                  "reviewerId": 9,
                                  "rating": 6,
                                  "notes": "Out of range rating"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("rating: Rating must be at most 5"));

        verifyNoInteractions(reviewService);
    }

    @Test
    void getReviewsByEmployee_returnsReviewList() throws Exception {
        List<ReviewResponse> response = List.of(
                ReviewResponse.builder()
                        .id(10L)
                        .employeeId(3L)
                        .employeeName("Alice Johnson")
                        .reviewCycleId(7L)
                        .reviewCycleName("Q1 2026")
                        .reviewerId(9L)
                        .reviewerName("Manager Mike")
                        .rating(5)
                        .notes("Strong delivery and collaboration")
                        .submittedAt(LocalDateTime.of(2026, 3, 20, 10, 15, 30))
                        .build(),
                ReviewResponse.builder()
                        .id(11L)
                        .employeeId(3L)
                        .employeeName("Alice Johnson")
                        .reviewCycleId(8L)
                        .reviewCycleName("Q2 2026")
                        .reviewerId(12L)
                        .reviewerName("Lead Laura")
                        .rating(4)
                        .notes("Consistent execution")
                        .submittedAt(LocalDateTime.of(2026, 3, 25, 11, 0, 0))
                        .build()
        );

        given(reviewService.getReviewsByEmployee(3L)).willReturn(response);

        mockMvc.perform(get("/employees/{id}/reviews", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].reviewCycleName").value("Q1 2026"))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].reviewCycleName").value("Q2 2026"));

        verify(reviewService).getReviewsByEmployee(3L);
    }

    @Test
    void getReviewsByEmployee_returnsNotFoundWhenEmployeeMissing() throws Exception {
        given(reviewService.getReviewsByEmployee(99L))
                .willThrow(new ResourceNotFoundException("Employee not found with id 99"));

        mockMvc.perform(get("/employees/{id}/reviews", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Employee not found with id 99"));

        verify(reviewService).getReviewsByEmployee(99L);
    }
}
