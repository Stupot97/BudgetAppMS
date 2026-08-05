package com.budget.ms.repository;

import com.budget.ms.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.budget.ms.entity.Expense;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByDateAndTrip_TripName(LocalDate date, String tripName);
    List<Expense> findByTripId(Long tripId);
    long countByRecurringExpenseId(Long recurringExpenseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Expense e WHERE e.recurringExpense.id = :recurringId")
    void deleteByRecurringExpenseId(@Param("recurringId") Long recurringId);
}