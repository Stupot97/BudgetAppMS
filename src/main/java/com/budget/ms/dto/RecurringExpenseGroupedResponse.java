package com.budget.ms.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecurringExpenseGroupedResponse {
    private RecurringExpenseResponse recurringExpense;
    private List<ExpenseResponse> expenses;
}
