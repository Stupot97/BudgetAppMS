package com.budget.ms.dto;

import com.budget.ms.model.ExpenseCategory;
import com.budget.ms.model.RecurringCostDuration;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecurringExpenseRequest {

    @NotBlank
    @Schema(example = "Travel Insurance")
    private String name;

    @NotNull
    @Schema(example = "20")
    private BigDecimal amount;

    @NotBlank
    @Schema(example = "USD")
    private String currency;

    @NotNull
    @Schema(example = "MISC")
    private ExpenseCategory category;

    @NotNull
    @Schema(example = "2026-03-25")
    private LocalDate startDate;

    @Schema(example = "2026-06-25")
    private LocalDate endDate;

    @NotNull
    @Schema(example = "MONTHLY")
    private RecurringCostDuration costDuration;

    @NotBlank
    @Schema(example = "Peru 2026")
    private String tripName;
}
