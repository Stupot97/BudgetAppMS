package com.budget.ms.dto;

import com.budget.ms.model.ExpenseCategory;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseResponse {
    private Long id;
    private String name;
    private BigDecimal amount;
    private ExpenseCategory category;
    private LocalDate date;
    private String currency;
    private String tripName;
}
