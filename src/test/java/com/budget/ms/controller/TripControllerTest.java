package com.budget.ms.controller;

import com.budget.ms.dto.AnalyticsResponse;
import com.budget.ms.dto.BudgetSummaryResponse;
import com.budget.ms.dto.TripRequest;
import com.budget.ms.dto.TripResponse;
import com.budget.ms.service.TripService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@WebMvcTest(TripController.class)
public class TripControllerTest {

    @MockitoBean
    private TripService tripService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetTripById() throws Exception {
        TripResponse tripResponse = new TripResponse();
        tripResponse.setTripName("Peru 2026");
        tripResponse.setCurrency("USD");
        tripResponse.setId(1L);
        tripResponse.setStartDate(LocalDate.of(2026,5,3));
        tripResponse.setDailySpending(BigDecimal.valueOf(50L));
        tripResponse.setTripBudget(BigDecimal.valueOf(2000L));

        when(tripService.getTrip(any()))
                .thenReturn(tripResponse);

        MvcResult result = mockMvc.perform(get("/trip/1"))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200, result.getResponse().getStatus()),
                () -> assertTrue(json.contains("Peru 2026")),
                () -> assertTrue(json.contains("USD")),
                () -> assertTrue(json.contains("2026-05-03")),
                () -> assertTrue(json.contains("50")),
                () -> assertTrue(json.contains("2000"))
        );
    }


    @Test
    void shouldGetTripAnalytics() throws Exception {
        AnalyticsResponse response = new AnalyticsResponse();
        response.setActual(new BigDecimal("3544"));
        response.setPredicted(new BigDecimal("3500"));
        response.setDate(LocalDate.of(2026,5,3));


        when(tripService.getTripAnalytics(any(),any()))
                .thenReturn(response);

        MvcResult result = mockMvc.perform(get("/trip/1/analytics")
                        .param("date", "2026-05-03")
                        .param("tripName", "Peru 2026"))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200, result.getResponse().getStatus()),
                () -> assertTrue(json.contains("2026-05-03")),
                () -> assertTrue(json.contains("3500")),
                () -> assertTrue(json.contains("3544"))
        );
    }

    @Test
    void shouldCreateTrip() throws Exception {
        TripRequest request = new TripRequest();
        request.setTripBudget(new BigDecimal("55000"));
        request.setTripName("World Trip 2024-2026");
        request.setCurrency("USD");
        request.setStartDate(LocalDate.of(2024,11,9));
        request.setEndDate(LocalDate.of(2026,12,2));

        TripResponse response = new TripResponse();
        response.setTripBudget(new BigDecimal("55000"));
        response.setTripName("World Trip 2024-2026");
        response.setCurrency("USD");
        response.setId(1L);
        response.setStartDate(LocalDate.of(2024,11,9));
        response.setEndDate(LocalDate.of(2026,12,2));

        when(tripService.createTrip(request))
                .thenReturn(response);

        MvcResult result = mockMvc.perform(post("/trip")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200, result.getResponse().getStatus()),
                () -> assertTrue(json.contains("World Trip 2024-2026")),
                () -> assertTrue(json.contains("55000")),
                () -> assertTrue(json.contains("USD")),
                () -> assertTrue(json.contains("2024-11-09")),
                () -> assertTrue(json.contains("2026-12-02"))
                );
    }

    @Test
    void shouldEditTrip() throws Exception {
        TripRequest request = new TripRequest();
        request.setTripBudget(new BigDecimal("60000"));
        request.setTripName("World Trip 2024-2026");
        request.setCurrency("USD");
        request.setStartDate(LocalDate.of(2024,11,9));
        request.setEndDate(LocalDate.of(2026,12,2));

        TripResponse response = new TripResponse();
        response.setTripBudget(new BigDecimal("60000"));
        response.setTripName("World Trip 2024-2026");
        response.setCurrency("USD");
        response.setId(1L);
        response.setStartDate(LocalDate.of(2024,11,9));
        response.setEndDate(LocalDate.of(2026,12,2));

        when(tripService.editTrip(any(), any()))
                .thenReturn(response);

        MvcResult result = mockMvc.perform(put("/trip/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200, result.getResponse().getStatus()),
                () -> assertTrue(json.contains("World Trip 2024-2026")),
                () -> assertTrue(json.contains("60000")),
                () -> assertTrue(json.contains("USD")),
                () -> assertTrue(json.contains("2024-11-09")),
                () -> assertTrue(json.contains("2026-12-02"))
        );
    }

    @Test
    void shouldDeleteTrip() throws Exception {
        MvcResult result = mockMvc.perform(delete("/trip/{id}", 1L))
                .andReturn();

        int status = result.getResponse().getStatus();

        assertEquals(204, status);

        Mockito.verify(tripService).deleteTrip(1L);
    }

    @Test
    void shouldGetTripSummary() throws Exception{
        BudgetSummaryResponse response = new BudgetSummaryResponse();
        response.setName("Peru 2026");
        response.setAmountSpent(new BigDecimal("3500"));
        response.setAmountRemaining(new BigDecimal("1500"));
        response.setId(1L);
        response.setCurrency("USD");
        response.setStartDate(LocalDate.of(2026,4,19));
        response.setEndDate(LocalDate.of(2026,7,19));

        when(tripService.getBudgetSummary(any()))
                .thenReturn(response);

        MvcResult result = mockMvc.perform(get("/trip/{id}/budgetSummary", 1L))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200, result.getResponse().getStatus()),
                () -> assertTrue(json.contains("Peru 2026")),
                () -> assertTrue(json.contains("3500")),
                () -> assertTrue(json.contains("1500")),
                () -> assertTrue(json.contains("1")),
                () -> assertTrue(json.contains("USD")),
                () -> assertTrue(json.contains("2026-04-19")),
                () -> assertTrue(json.contains("2026-07-19"))
        );
    }

}
