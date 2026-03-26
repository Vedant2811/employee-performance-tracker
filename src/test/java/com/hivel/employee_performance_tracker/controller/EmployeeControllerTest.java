package com.hivel.employee_performance_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.hivel.employee_performance_tracker.dto.request.CreateEmployeeRequest;
import com.hivel.employee_performance_tracker.dto.response.EmployeeResponse;
import com.hivel.employee_performance_tracker.exception.GlobalExceptionHandler;
import com.hivel.employee_performance_tracker.service.EmployeeService;
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

import java.time.LocalDate;
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
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createEmployee_returnsCreatedEmployee() throws Exception {
        EmployeeResponse response = EmployeeResponse.builder()
                .id(1L)
                .name("Alice Johnson")
                .department("Engineering")
                .role("Backend Engineer")
                .joiningDate(LocalDate.of(2024, 1, 15))
                .build();

        given(employeeService.createEmployee(any(CreateEmployeeRequest.class))).willReturn(response);

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice Johnson",
                                  "department": "Engineering",
                                  "role": "Backend Engineer",
                                  "joiningDate": "2024-01-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.department").value("Engineering"))
                .andExpect(jsonPath("$.role").value("Backend Engineer"))
                .andExpect(jsonPath("$.joiningDate").value("2024-01-15"));

        ArgumentCaptor<CreateEmployeeRequest> captor =
                ArgumentCaptor.forClass(CreateEmployeeRequest.class);
        verify(employeeService).createEmployee(captor.capture());
        assertEquals("Alice Johnson", captor.getValue().getName());
        assertEquals("Engineering", captor.getValue().getDepartment());
        assertEquals("Backend Engineer", captor.getValue().getRole());
        assertEquals(LocalDate.of(2024, 1, 15), captor.getValue().getJoiningDate());
    }

    @Test
    void createEmployee_returnsBadRequestWhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "department": "Engineering",
                                  "role": "Backend Engineer",
                                  "joiningDate": "2024-01-15"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("name: Name is required"));

        verifyNoInteractions(employeeService);
    }

    @Test
    void filterEmployees_returnsMatchingEmployees() throws Exception {
        List<EmployeeResponse> response = List.of(
                EmployeeResponse.builder()
                        .id(1L)
                        .name("Alice Johnson")
                        .department("Engineering")
                        .role("Backend Engineer")
                        .joiningDate(LocalDate.of(2024, 1, 15))
                        .build(),
                EmployeeResponse.builder()
                        .id(2L)
                        .name("Bob Singh")
                        .department("Engineering")
                        .role("QA Engineer")
                        .joiningDate(LocalDate.of(2023, 7, 1))
                        .build()
        );

        given(employeeService.filterEmployees("Engineering", 4.2)).willReturn(response);

        mockMvc.perform(get("/employees")
                        .param("department", "Engineering")
                        .param("minRating", "4.2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice Johnson"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Bob Singh"));

        verify(employeeService).filterEmployees("Engineering", 4.2);
    }

    @Test
    void filterEmployees_returnsBadRequestWhenMinRatingMissing() throws Exception {
        mockMvc.perform(get("/employees")
                        .param("department", "Engineering"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Missing required parameter: minRating"));

        verifyNoInteractions(employeeService);
    }
}
