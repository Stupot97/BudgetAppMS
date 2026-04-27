package com.budget.ms.dto;

import com.budget.ms.model.ExpenseCategory;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {

    @NotBlank
    @Schema(example = "Hotel")
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than zero")
    @Schema(example = "120.5")
    private BigDecimal amount;

    @NotNull
    @Schema(example = "ACCOMMODATION")
    private ExpenseCategory category;

    @NotNull
    @Schema(example = "2026-04-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotBlank
    @Schema(example = "USD")
    private String currency;

    @NotBlank
    @Schema(example = "Peru 2026")
    private String tripName;
}
