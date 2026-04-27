package com.budget.ms.repository;

import com.budget.ms.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

import com.budget.ms.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByDateAndTrip_TripName(LocalDate date, String tripName);
    List<Expense> findByDateBetweenAndTrip_TripName(LocalDate startDate, LocalDate endDate, String tripName);
    boolean existsByRecurringExpenseAndDate(RecurringExpense recurringExpense, LocalDate date);
}