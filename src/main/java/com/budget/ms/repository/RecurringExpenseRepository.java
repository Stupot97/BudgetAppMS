package com.budget.ms.repository;

import com.budget.ms.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense,Long> {
}
