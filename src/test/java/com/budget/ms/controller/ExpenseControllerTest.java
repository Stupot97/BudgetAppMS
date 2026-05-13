package com.budget.ms.controller;

import com.budget.ms.dto.ExpenseRequest;
import com.budget.ms.dto.ExpenseResponse;
import com.budget.ms.model.ExpenseCategory;
import com.budget.ms.service.ExpenseService;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@WebMvcTest(ExpenseController.class)
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnExpenseById() throws Exception {
        ExpenseResponse expenseResponse = new ExpenseResponse();
        expenseResponse.setId(1L);
        expenseResponse.setName("Dinner");

        when(expenseService.getExpenseById(anyLong()))
                .thenReturn(expenseResponse);


        MvcResult result = mockMvc.perform(get("/expenses/1"))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200,result.getResponse().getStatus()),
                () -> assertTrue(json.contains("Dinner"))
        );
    }

    @Test
    void shouldReturnExpensesByDateAndTrip() throws Exception {

        ExpenseResponse expenseResponse1 = new ExpenseResponse();
        expenseResponse1.setId(1L);
        expenseResponse1.setName("Dinner");
        expenseResponse1.setDate(LocalDate.of(2026,02,02));
        expenseResponse1.setTripName("Tokyo 2026");


        ExpenseResponse expenseResponse2 = new ExpenseResponse();
        expenseResponse2.setId(2L);
        expenseResponse2.setName("Hotel");
        expenseResponse2.setDate(LocalDate.of(2026,02,02));
        expenseResponse2.setTripName("Tokyo 2026");

        List<ExpenseResponse> listOfExpenses = new ArrayList<ExpenseResponse>();
        listOfExpenses.add(expenseResponse1);
        listOfExpenses.add(expenseResponse2);

        when(expenseService.getExpensesByDateAndTrip(any(), any()))
                .thenReturn(listOfExpenses);

        MvcResult result = mockMvc.perform(get("/expenses")
                        .param("date", "2026-02-02")
                        .param("tripName", "Tokyo 2026"))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200,result.getResponse().getStatus()),
                () -> assertTrue(json.contains("Dinner")),
                () -> assertTrue(json.contains("Hotel")),
                () -> assertTrue(json.contains("2026-02-02")),
                () -> assertTrue(json.contains("Tokyo 2026"))
        );
    }

    @Test
    void shouldCreateExpense() throws Exception {

        ExpenseRequest request = new ExpenseRequest();
        request.setName("Dinner");
        request.setTripName("Tokyo 2026");
        request.setDate(LocalDate.of(2026, 5, 26));
        request.setAmount(new BigDecimal(20));
        request.setCategory(ExpenseCategory.FOOD);
        request.setCurrency("YEN");

        ExpenseResponse response = new ExpenseResponse();
        response.setId(1L);
        response.setName("Dinner");
        response.setTripName("Tokyo 2026");
        response.setDate(LocalDate.of(2026, 5, 26));
        response.setAmount(new BigDecimal(20));
        response.setCategory(ExpenseCategory.FOOD);
        response.setCurrency("YEN");

        when(expenseService.createExpense(any()))
                .thenReturn(response);

        MvcResult result = mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200,result.getResponse().getStatus()),
                () -> assertTrue(json.contains("Dinner")),
                () -> assertTrue(json.contains("Tokyo 2026")),
                () -> assertTrue(json.contains("2026-05-26")),
                () -> assertTrue(json.contains("20")),
                () -> assertTrue(json.contains("FOOD")),
                () -> assertTrue(json.contains("YEN"))
        );
    }

    @Test
    void shouldReturnTotalByDate () throws Exception {
        ExpenseRequest request = new ExpenseRequest();
        request.setTripName("Tokyo 2026");
        request.setDate(LocalDate.of(2026,02,02));

        BigDecimal response = new BigDecimal("70");
        when(expenseService.getTotalByDate(any(),any()))
                .thenReturn(response);


        MvcResult result = mockMvc.perform(get("/expenses/dailyTotal")
                        .param("date", "2026-02-02")
                        .param("tripName", "Tokyo 2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200,result.getResponse().getStatus()),
                () -> assertTrue(json.contains("70"))
        );
    }

    @Test
    void shouldUpdateExpense() throws Exception {

        ExpenseRequest request = new ExpenseRequest();
        request.setName("Dinner");
        request.setTripName("Tokyo 2026");
        request.setDate(LocalDate.of(2026, 5, 26));
        request.setAmount(new BigDecimal(50));
        request.setCategory(ExpenseCategory.FOOD);
        request.setCurrency("YEN");

        ExpenseResponse response = new ExpenseResponse();
        response.setId(1L);
        response.setName("Dinner");
        response.setTripName("Tokyo 2026");
        response.setDate(LocalDate.of(2026, 5, 26));
        response.setAmount(new BigDecimal(50));
        response.setCategory(ExpenseCategory.FOOD);
        response.setCurrency("YEN");

        when(expenseService.updateExpense(any(), any()))
                .thenReturn(response);

        MvcResult result = mockMvc.perform(put("/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200,result.getResponse().getStatus()),
                () -> assertTrue(json.contains("Dinner")),
                () -> assertTrue(json.contains("Tokyo 2026")),
                () -> assertTrue(json.contains("2026-05-26")),
                () -> assertTrue(json.contains("50")),
                () -> assertTrue(json.contains("FOOD")),
                () -> assertTrue(json.contains("YEN"))
        );
    }

    @Test
    void shouldDeleteExpense() throws Exception {
        MvcResult result = mockMvc.perform(delete("/expenses/{id}", 1L))
                .andReturn();

        int status = result.getResponse().getStatus();

        assertEquals(204, status);

        Mockito.verify(expenseService).deleteExpense(1L);
    }
}
