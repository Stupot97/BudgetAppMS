package com.budget.ms.controller;

import com.budget.ms.dto.CurrencyResponse;
import com.budget.ms.entity.AppSettings;
import com.budget.ms.service.SettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(SettingsController.class)
public class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SettingsService settingsService;

    @Test
    void shouldGetCurrency() throws Exception {

        AppSettings settingsResponse = new AppSettings();
        settingsResponse.setId(1L);
        settingsResponse.setPreferredCurrency("USD");

        when(settingsService.getSettings())
                .thenReturn(settingsResponse);

        MvcResult result = mockMvc.perform(get("/settings/currency"))
                .andReturn();
    }

    @Test
    void shouldUpdateCurrency() throws Exception {

        MvcResult result = mockMvc.perform(put("/settings/currency")
                        .param("currency", "EUR"))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertAll(
                () -> assertEquals(200,result.getResponse().getStatus()),
                () -> assertTrue(json.contains("EUR"))
        );
    }

}
