package com.budget.ms.controller;

import com.budget.ms.dto.ExpenseRequest;
import com.budget.ms.dto.ExpenseResponse;
import com.budget.ms.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(
            ExpenseService expenseService
    ) {
        this.expenseService = expenseService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpensesByDateAndTrip(
            @RequestParam("date") LocalDate date,
            @RequestParam("tripName") String tripName
    ) {
        return ResponseEntity.ok(expenseService.getExpensesByDateAndTrip(date, tripName));
    }

    @GetMapping("/dailyTotal")
    public ResponseEntity<BigDecimal> getTotalByDate(
            @RequestParam("date") LocalDate date,
            @RequestParam("tripName") String tripName
    ) {
        return ResponseEntity.ok(expenseService.getTotalByDate(date, tripName));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request
    ) {
        return ResponseEntity.ok(expenseService.createExpense(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable("id") Long id,
            @Valid @RequestBody ExpenseRequest request
    ) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable("id") Long id
    ) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}