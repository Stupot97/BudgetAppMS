package com.budget.ms.service;

import com.budget.ms.dto.RecurringExpenseRequest;
import com.budget.ms.dto.RecurringExpenseResponse;
import com.budget.ms.entity.Expense;
import com.budget.ms.entity.RecurringExpense;
import com.budget.ms.entity.Trip;
import com.budget.ms.exception.ResourceNotFoundException;
import com.budget.ms.model.RecurringCostDuration;
import com.budget.ms.repository.ExpenseRepository;
import com.budget.ms.repository.RecurringExpenseRepository;
import com.budget.ms.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RecurringExpenseService {

    public final RecurringExpenseRepository recurringExpenseRepository;
    public final TripRepository tripRepository;
    public final ExpenseRepository expenseRepository;
    public final ExpenseService expenseService;


    public RecurringExpenseService(RecurringExpenseRepository recurringExpenseRepository, TripRepository tripRepository, ExpenseRepository expenseRepository, ExpenseService expenseService){
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.tripRepository = tripRepository;
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
    }

    public RecurringExpenseResponse getRecurringExpenseById(Long id){
        RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring expense not found"));

        return mapToResponse(recurringExpense);
    }

    @Transactional
    public RecurringExpenseResponse createRecurringExpense(RecurringExpenseRequest request){
        Trip trip = tripRepository.findByTripName(request.getTripName())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found."));

        validateRecurringExpense(request, trip);

        RecurringExpense recurringExpense = new RecurringExpense();
        recurringExpense.setAmount(request.getAmount());
        recurringExpense.setCategory(request.getCategory());
        recurringExpense.setName(request.getName());
        recurringExpense.setStartDate(request.getStartDate());
        recurringExpense.setEndDate(request.getEndDate());
        recurringExpense.setCostDuration(request.getCostDuration());
        recurringExpense.setTrip(trip);
        recurringExpense.setCurrency(request.getCurrency());
        recurringExpense = recurringExpenseRepository.save(recurringExpense);

        addRecurringExpenses(recurringExpense, trip);

        return mapToResponse(recurringExpense);
    }

    @Transactional
    public RecurringExpenseResponse editRecurringExpense(RecurringExpenseRequest request){
        Trip trip = tripRepository.findByTripName(request.getTripName())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found."));

        validateRecurringExpense(request, trip);

        RecurringExpense recurringExpense = new RecurringExpense();
        recurringExpense.setAmount(request.getAmount());
        recurringExpense.setCategory(request.getCategory());
        recurringExpense.setName(request.getName());
        recurringExpense.setStartDate(request.getStartDate());
        recurringExpense.setEndDate(request.getEndDate());
        recurringExpense.setCostDuration(request.getCostDuration());
        recurringExpense.setTrip(trip);
        recurringExpense = recurringExpenseRepository.save(recurringExpense);

        //not editing, so false second argument

        deleteRecurringExpenseById(recurringExpense.getId());
        addRecurringExpenses(recurringExpense, trip);

        return mapToResponse(recurringExpense);
    }

    public void deleteRecurringExpenseById(Long id){
          recurringExpenseRepository.deleteById(id);
    }

    private RecurringExpenseResponse mapToResponse(RecurringExpense recurringExpense){
        RecurringExpenseResponse recurringExpenseResponse = new RecurringExpenseResponse();
        recurringExpenseResponse.setId(recurringExpense.getId());
        recurringExpenseResponse.setAmount(recurringExpense.getAmount());
        recurringExpenseResponse.setTripName(recurringExpense.getTrip().getTripName());
        recurringExpenseResponse.setCategory(recurringExpense.getCategory());
        recurringExpenseResponse.setCostDuration(recurringExpense.getCostDuration());
        recurringExpenseResponse.setName(recurringExpense.getName());
        recurringExpenseResponse.setStartDate(recurringExpense.getStartDate());
        recurringExpenseResponse.setEndDate(recurringExpense.getEndDate());
        recurringExpenseResponse.setCurrency(recurringExpense.getCurrency());
        return recurringExpenseResponse;
    }

    private void validateRecurringExpense(RecurringExpenseRequest recurringExpenseRequest, Trip trip){
        if(trip.getStartDate().isAfter(recurringExpenseRequest.getStartDate()))
            throw new IllegalArgumentException("Recurring expense start date cannot be before trip start date.");

        if(trip.getEndDate()!=null && recurringExpenseRequest.getEndDate()!=null && trip.getEndDate().isBefore(recurringExpenseRequest.getEndDate()))
            throw new IllegalArgumentException("Recurring expense end date cannot be after trip end date.");
    }

    private void addRecurringExpenses(RecurringExpense recurringExpense, Trip trip){
        LocalDate current = recurringExpense.getStartDate();
        LocalDate end = recurringExpense.getEndDate().isBefore(trip.getEndDate())
                ? recurringExpense.getEndDate()
                : trip.getEndDate();

        while (!current.isAfter(end)) {
            Expense expense = new Expense();
            expense.setDate(current);
            expense.setRecurringExpense(recurringExpense);
            expense.setName(recurringExpense.getName());
            expense.setTrip(recurringExpense.getTrip());
            expense.setCategory(recurringExpense.getCategory());
            expense.setAmount(recurringExpense.getAmount());
            expense.setCurrency(recurringExpense.getCurrency());
            expenseRepository.save(expense);

            current = incrementDate(current, recurringExpense.getCostDuration());
        }
    }

    private LocalDate incrementDate(LocalDate currentDate, RecurringCostDuration costDuration) {
        return switch (costDuration) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case YEARLY -> currentDate.plusYears(1);
        };
    }

}
