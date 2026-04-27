package com.budget.ms.repository;

import com.budget.ms.entity.RecurringExpense;
import com.budget.ms.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense,Long> {
    List<RecurringExpense> findByTrip(Trip trip);
}
