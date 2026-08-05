package com.budget.ms.dto;


import lombok.Data;

import java.util.List;

@Data
public class TripEditResponse {
    List<RecurringExpenseGroupedResponse> recurringExpenses;
    List<ExpenseResponse> expenses;
}
