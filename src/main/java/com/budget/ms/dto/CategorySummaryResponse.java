package com.budget.ms.dto;

import com.budget.ms.model.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategorySummaryResponse {
    private ExpenseCategory category;
    private BigDecimal categoryTotal;
    private String tripName;
}
