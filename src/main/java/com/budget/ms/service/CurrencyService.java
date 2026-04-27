package com.budget.ms.service;

import com.budget.ms.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CurrencyService {

    @Value("${currency.api.url}")
    private String currencyApiUrl;

    private final Map<String, Map<String, Double>> cachedRates = new HashMap<>();

    private final RestTemplate restTemplate = new RestTemplate();

    public CurrencyService(){}

    public BigDecimal convertValue(BigDecimal amount, String baseCurrency, String targetCurrency){
        String url = currencyApiUrl + baseCurrency;

        try{
            Map<String, Double> rates = cachedRates.computeIfAbsent(baseCurrency, x -> {
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                Map<String, Object> rawRates = (Map<String, Object>) response.get("rates");
                return rawRates
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> new BigDecimal(e.getValue().toString()).doubleValue()
                        ));
            });

            Double doubleRate = rates.get(targetCurrency);
            if(doubleRate == null)
                throw new IllegalArgumentException("Unsupported currency conversion pair: " + baseCurrency + "->" + targetCurrency);

            return BigDecimal.valueOf(doubleRate).multiply(amount);
        }
        catch(Exception e){
            throw new ExternalServiceException("Currency Conversion API is down.");
        }


    }

    public void validateCurrency(String currencyCode){
        String url = currencyApiUrl + currencyCode;

        try {
            Currency.getInstance(currencyCode); // built-in Java validation
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported currency: " + currencyCode);
        }
    }
}
