package com.budget.ms.service;

import com.budget.ms.entity.AppSettings;
import com.budget.ms.repository.SettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
class SettingsServiceTest {

    private SettingsRepository settingsRepository;

    private SettingsService settingsService;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        settingsRepository = mock(SettingsRepository.class);
        restTemplate = mock(RestTemplate.class);

        settingsService = new SettingsService(
                settingsRepository, restTemplate
        );
    }

    @Test
    void shouldReturnExistingSettings() {
        AppSettings settings = new AppSettings();
        settings.setId(1L);
        settings.setPreferredCurrency("EUR");

        when(settingsRepository.findById(1L))
                .thenReturn(Optional.of(settings));

        AppSettings result = settingsService.getSettings();

        assertEquals("EUR", result.getPreferredCurrency());

        verify(settingsRepository, never())
                .save(any());
    }

    @Test
    void shouldCreateDefaultSettingsWhenNoneExist() {
        when(settingsRepository.findById(1L))
                .thenReturn(Optional.empty());

        when(settingsRepository.save(any(AppSettings.class)))
                .thenAnswer(invocation -> {
                    AppSettings settings =
                            invocation.getArgument(0);

                    settings.setId(1L);

                    return settings;
                });

        AppSettings result = settingsService.getSettings();

        assertEquals("USD", result.getPreferredCurrency());
        assertEquals(1L, result.getId());

        verify(settingsRepository)
                .save(any(AppSettings.class));
    }

    @Test
    void shouldReturnPreferredCurrency() {
        AppSettings settings = new AppSettings();
        settings.setPreferredCurrency("JPY");

        when(settingsRepository.findById(1L))
                .thenReturn(Optional.of(settings));

        String currency = settingsService.getCurrency();

        assertEquals("JPY", currency);
    }

    @Test
    void shouldUpdateCurrency() {
        AppSettings settings = new AppSettings();
        settings.setId(1L);
        settings.setPreferredCurrency("USD");

        when(settingsRepository.findById(1L))
                .thenReturn(Optional.of(settings));

        settingsService.updateCurrency("EUR");

        assertEquals(
                "EUR",
                settings.getPreferredCurrency()
        );
    }

    @Test
    void shouldThrowForInvalidCurrency() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> settingsService
                                .updateCurrency("INVALID")
                );

        assertEquals(
                "Unsupported currency: INVALID",
                exception.getMessage()
        );
    }
}