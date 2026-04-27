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
public class RecurringExpenseResponse {
    private Long id;
    private String name;
    private BigDecimal amount;
    private String currency;
    private ExpenseCategory category;
    private LocalDate startDate;
    private LocalDate endDate;
    private RecurringCostDuration costDuration;
    private String tripName;
}
