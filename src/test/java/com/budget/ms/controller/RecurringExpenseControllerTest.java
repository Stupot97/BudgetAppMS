package com.budget.ms.controller;

import com.budget.ms.dto.RecurringExpenseRequest;
import com.budget.ms.dto.RecurringExpenseResponse;
import com.budget.ms.model.ExpenseCategory;
import com.budget.ms.model.RecurringCostDuration;
import com.budget.ms.service.RecurringExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@WebMvcTest(RecurringExpenseController.class)
public class RecurringExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecurringExpenseService recurringExpenseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnRecurringExpenseById() throws Exception {
        RecurringExpenseResponse expenseResponse = new RecurringExpenseResponse();
        expenseResponse.setId(1L);
        expenseResponse.setName("Insurance");

        when(recurringExpenseService.getRecurringExpenseById(anyLong()))
                .thenReturn(expenseResponse);

        MvcResult result = mockMvc.perform(get("/recurring/1"))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200, result.getResponse().getStatus()),
                () -> assertTrue(json.contains("Insurance"))
        );
    }

    @Test
    void shouldCreateRecurringExpense() throws Exception {
        RecurringExpenseRequest recurringExpenseRequest = new RecurringExpenseRequest();
        recurringExpenseRequest.setName("Insurance");
        recurringExpenseRequest.setCurrency("USD");
        recurringExpenseRequest.setCategory(ExpenseCategory.MISC);
        recurringExpenseRequest.setAmount(new BigDecimal(50));
        recurringExpenseRequest.setStartDate(LocalDate.of(2026, 02, 02));
        recurringExpenseRequest.setEndDate(LocalDate.of(2026, 02, 25));
        recurringExpenseRequest.setTripName("Tokyo 2026");
        recurringExpenseRequest.setCostDuration(RecurringCostDuration.MONTHLY);

        RecurringExpenseResponse recurringExpenseResponse = new RecurringExpenseResponse();
        recurringExpenseResponse.setId(2L);
        recurringExpenseResponse.setName("Insurance");
        recurringExpenseResponse.setCategory(ExpenseCategory.MISC);
        recurringExpenseResponse.setCurrency("USD");
        recurringExpenseResponse.setAmount(new BigDecimal(50));
        recurringExpenseResponse.setStartDate(LocalDate.of(2026, 02, 02));
        recurringExpenseResponse.setEndDate(LocalDate.of(2026, 02, 25));
        recurringExpenseResponse.setTripName("Tokyo 2026");
        recurringExpenseResponse.setCostDuration(RecurringCostDuration.MONTHLY);

        when(recurringExpenseService.createRecurringExpense(any()))
                .thenReturn(recurringExpenseResponse);

        MvcResult result = mockMvc.perform(post("/recurring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recurringExpenseRequest)))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200, result.getResponse().getStatus()),
                () -> assertTrue(json.contains("Insurance")),
                () -> assertTrue(json.contains("50")),
                () -> assertTrue(json.contains("2026-02-02")),
                () -> assertTrue(json.contains("2026-02-25")),
                () -> assertTrue(json.contains("MONTHLY")),
                () -> assertTrue(json.contains("MISC")),
                () -> assertTrue(json.contains("Tokyo 2026"))
        );
    }

    @Test
    void shouldEditRecurringExpense() throws Exception {
        RecurringExpenseRequest recurringExpenseRequest = new RecurringExpenseRequest();
        recurringExpenseRequest.setName("Insurance");
        recurringExpenseRequest.setCurrency("USD");
        recurringExpenseRequest.setCategory(ExpenseCategory.MISC);
        recurringExpenseRequest.setAmount(new BigDecimal(50));
        recurringExpenseRequest.setStartDate(LocalDate.of(2026, 02, 02));
        recurringExpenseRequest.setEndDate(LocalDate.of(2026, 02, 25));
        recurringExpenseRequest.setTripName("Tokyo 2026");
        recurringExpenseRequest.setCostDuration(RecurringCostDuration.MONTHLY);

        RecurringExpenseResponse recurringExpenseResponse = new RecurringExpenseResponse();
        recurringExpenseResponse.setId(2L);
        recurringExpenseResponse.setName("Insurance");
        recurringExpenseResponse.setCategory(ExpenseCategory.MISC);
        recurringExpenseResponse.setCurrency("USD");
        recurringExpenseResponse.setAmount(new BigDecimal(50));
        recurringExpenseResponse.setStartDate(LocalDate.of(2026, 02, 02));
        recurringExpenseResponse.setEndDate(LocalDate.of(2026, 02, 25));
        recurringExpenseResponse.setTripName("Tokyo 2026");
        recurringExpenseResponse.setCostDuration(RecurringCostDuration.MONTHLY);

        when(recurringExpenseService.editRecurringExpense(any(), any()))
                .thenReturn(recurringExpenseResponse);

        MvcResult result = mockMvc.perform(put("/recurring/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recurringExpenseRequest)))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200, result.getResponse().getStatus()),
                () -> assertTrue(json.contains("Insurance")),
                () -> assertTrue(json.contains("50")),
                () -> assertTrue(json.contains("2026-02-02")),
                () -> assertTrue(json.contains("2026-02-25")),
                () -> assertTrue(json.contains("MONTHLY")),
                () -> assertTrue(json.contains("MISC")),
                () -> assertTrue(json.contains("Tokyo 2026"))
        );
    }

    @Test
    void shouldDeleteRecurringExpense() throws Exception {
        MvcResult result = mockMvc.perform(delete("/recurring/{id}", 1L))
                .andReturn();

        int status = result.getResponse().getStatus();

        assertEquals(204, status);

        Mockito.verify(recurringExpenseService).deleteRecurringExpenseById(1L);
    }
}
