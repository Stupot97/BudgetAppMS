package com.budget.ms.controller;

import com.budget.ms.dto.RecurringExpenseRequest;
import com.budget.ms.dto.RecurringExpenseResponse;
import com.budget.ms.service.RecurringExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recurring")
public class RecurringExpenseController {

    private final RecurringExpenseService recurringExpenseService;

    public RecurringExpenseController(RecurringExpenseService recurringExpenseService){
        this.recurringExpenseService = recurringExpenseService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringExpenseResponse> getRecurringExpense(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(recurringExpenseService.getRecurringExpenseById(id));
    }

    @PostMapping
    public ResponseEntity<RecurringExpenseResponse> createRecurringExpense(
            @Valid @RequestBody RecurringExpenseRequest request
    )   {
        return ResponseEntity.ok(recurringExpenseService.createRecurringExpense(request));
    }

    @PutMapping
    public ResponseEntity<RecurringExpenseResponse> editRecurringExpense(
            @Valid @RequestBody RecurringExpenseRequest request
    )   {
        return ResponseEntity.ok(recurringExpenseService.editRecurringExpense(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringExpense(
            @PathVariable("id") Long id
    ) {
        recurringExpenseService.deleteRecurringExpenseById(id);
        return ResponseEntity.noContent().build();
    }
}
