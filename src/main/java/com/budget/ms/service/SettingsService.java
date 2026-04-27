package com.budget.ms.service;

import com.budget.ms.entity.AppSettings;
import com.budget.ms.repository.SettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
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
