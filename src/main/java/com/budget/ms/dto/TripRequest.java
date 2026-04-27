package com.budget.ms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TripRequest {

    @NotBlank
    @Schema(example = "Peru 2026")
    private String tripName;

    @NotNull
    @Schema(example = "2026-03-25")
    private LocalDate startDate;

    @Schema(example = "2026-04-25")
    private LocalDate endDate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than zero")
    @Schema(example = "2500.00")
    private BigDecimal tripBudget;
}
