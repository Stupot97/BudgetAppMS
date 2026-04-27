package com.budget.ms.controller;

import com.budget.ms.dto.CurrencyResponse;
import com.budget.ms.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(
             SettingsService settingsService
    ) {
        this.settingsService = settingsService;
    }

    @GetMapping("/currency")
    public ResponseEntity<CurrencyResponse> getCurrency(

    ){
        return ResponseEntity.ok(new CurrencyResponse(settingsService.getCurrency()));
    }

    @PutMapping("/currency")
    public ResponseEntity<CurrencyResponse> updateCurrency(
            @RequestParam("currency") String currency
    ) {
        settingsService.updateCurrency(currency);
        return ResponseEntity.ok(new CurrencyResponse(currency));
    }
}
