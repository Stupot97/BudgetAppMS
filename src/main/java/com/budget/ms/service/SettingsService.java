package com.budget.ms.service;

import com.budget.ms.dto.CurrencyResponse;
import com.budget.ms.entity.AppSettings;
import com.budget.ms.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SettingsService {

    @Value("${currency.api.url}")
    private String currencyApiUrl;

    private Set<String> cachedCurrencies = new HashSet<>();

    private final RestTemplate restTemplate;

    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository, RestTemplate restTemplate) {
        this.settingsRepository = settingsRepository;
        this.restTemplate = restTemplate;
    }

    public AppSettings getSettings() {
        return settingsRepository.findById(1L)
                .orElseGet(() -> {
                    AppSettings settings = new AppSettings();
                    settings.setPreferredCurrency("USD");
                    return settingsRepository.save(settings);
                });
    }

    public String getCurrency(){
        return getSettings().getPreferredCurrency();
    }

    @Cacheable("currencies")
    public List<CurrencyResponse> fetchCurrencyList(){
        if(cachedCurrencies == null || cachedCurrencies.isEmpty()){
            Map<String, Object> response =
                    restTemplate.getForObject(currencyApiUrl, Map.class);

            if (response == null || response.get("rates") == null) {
                return Collections.emptyList();
            }

            Map<String, Object> rawRates =
                    (Map<String, Object>) response.get("rates");

            cachedCurrencies = new HashSet<>(rawRates.keySet());
        }

        return cachedCurrencies
                .stream()
                .sorted()
                .map(CurrencyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateCurrency(String currency) {
        try {
            Currency.getInstance(currency); // validates currency
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }

        AppSettings settings = getSettings();
        settings.setPreferredCurrency(currency.toUpperCase());
    }
}
