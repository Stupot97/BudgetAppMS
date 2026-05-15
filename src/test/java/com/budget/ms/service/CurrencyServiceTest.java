package com.budget.ms.service;

import com.budget.ms.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class CurrencyServiceTest {

    private CurrencyService currencyService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);

        currencyService = new CurrencyService(restTemplate);

        ReflectionTestUtils.setField(
                currencyService,
                "currencyApiUrl",
                "https://api.exchangerate-api.com/v4/latest/"
        );
    }

    @Test
    void shouldConvertCurrency() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> rates = new HashMap<>();
        rates.put("EUR", 0.85);
        response.put("rates", rates);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(response);

        BigDecimal result = currencyService.convertValue(
                BigDecimal.valueOf(100),
                "USD",
                "EUR"
        );

        assertEquals(0, BigDecimal.valueOf(85.0).compareTo(result));
    }

    @Test
    void shouldThrowExternalServiceExceptionForUnsupportedCurrencyPair() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> rates = new HashMap<>();
        rates.put("GBP", 0.75);

        response.put("rates", rates);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(response);

        assertThrows(
                ExternalServiceException.class,
                () -> currencyService.convertValue(
                        BigDecimal.valueOf(100),
                        "USD",
                        "EUR"
                )
        );
    }

    @Test
    void shouldThrowExternalServiceExceptionWhenApiFails() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("API down"));

        assertThrows(
                ExternalServiceException.class,
                () -> currencyService.convertValue(
                        BigDecimal.valueOf(100),
                        "USD",
                        "EUR"
                )
        );
    }

    @Test
    void shouldValidateSupportedCurrency() {
        assertDoesNotThrow(() ->
                currencyService.validateCurrency("USD")
        );
    }

    @Test
    void shouldThrowExceptionForUnsupportedCurrency() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> currencyService.validateCurrency("INVALID")
        );

        assertEquals(
                "Unsupported currency: INVALID",
                exception.getMessage()
        );
    }

    @Test
    void shouldUseCachedRatesAfterFirstCall() {
        // given
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> rates = new HashMap<>();
        rates.put("EUR", 0.85);

        response.put("rates", rates);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(response);

        currencyService.convertValue(
                BigDecimal.valueOf(100),
                "USD",
                "EUR"
        );

        currencyService.convertValue(
                BigDecimal.valueOf(200),
                "USD",
                "EUR"
        );

        verify(restTemplate, times(1))
                .getForObject(anyString(), eq(Map.class));
    }
}