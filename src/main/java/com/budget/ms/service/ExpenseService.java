package com.budget.ms.service;

import com.budget.ms.dto.*;
import com.budget.ms.entity.Expense;
import com.budget.ms.entity.RecurringExpense;
import com.budget.ms.entity.Trip;
import com.budget.ms.exception.ResourceNotFoundException;
import com.budget.ms.repository.ExpenseRepository;
import com.budget.ms.repository.RecurringExpenseRepository;
import com.budget.ms.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final CurrencyService currencyService;
    private final SettingsService settingsService;
    private final TripRepository tripRepository;

    public ExpenseService(ExpenseRepository expenseRepository, TripRepository tripRepository, CurrencyService currencyService, SettingsService settingsService, RecurringExpenseRepository recurringExpenseRepository) {
        this.expenseRepository = expenseRepository;
        this.tripRepository = tripRepository;
        this.currencyService = currencyService;
        this.settingsService = settingsService;
        this.recurringExpenseRepository = recurringExpenseRepository;
    }

    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        return mapToResponse(expense);
    }

    public List<ExpenseResponse> getExpensesByDateAndTrip(LocalDate date, String tripName) {
        tripRepository.findByTripName(tripName)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        List<Expense> expenses = expenseRepository.findByDateAndTrip_TripName(date, tripName);

        return expenses.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BigDecimal getTotalByDate(LocalDate date, String tripName){
        tripRepository.findByTripName(tripName)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        List<Expense> expenses = expenseRepository.findByDateAndTrip_TripName(date, tripName);

        return expenses.stream()
                .map(expense -> currencyService.convertValue(expense.getAmount(), expense.getCurrency(), settingsService.getCurrency()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        String tripName = request.getTripName();

        Trip trip = tripRepository.findByTripName(tripName)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found for given tripName."));

        verifyExpenseDates(trip,request);

        String currencyCode = request.getCurrency();
        currencyService.validateCurrency(currencyCode);

        Expense expense = new Expense();
        expense.setName(request.getName());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setCurrency(request.getCurrency());
        expense.setTrip(trip);

        return mapToResponse(expenseRepository.save(expense));
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        Trip trip = tripRepository.findByTripName(request.getTripName())
                .orElseThrow(() -> new ResourceNotFoundException("Trip defined in expense not found"));

        verifyExpenseDates(trip,request);

        String currencyCode = request.getCurrency();

        currencyService.validateCurrency(currencyCode);

        expense.setName(request.getName());
        expense.setAmount(request.getAmount());
        expense.setCurrency(currencyCode);
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setTrip(trip);

        return mapToResponse(expenseRepository.save(expense));
    }

    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if(expense.getRecurringExpense()!=null){
             if(expenseRepository.countByRecurringExpenseId(expense.getRecurringExpense().getId()) == 1)
                 recurringExpenseRepository.deleteById(expense.getRecurringExpense().getId());
             else
                 expenseRepository.delete(expense);
        }
        else
            expenseRepository.delete(expense);

    }

    public ExpenseResponse mapToResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setName(expense.getName());

        String appCurrency = settingsService.getCurrency();;
        String expenseCurrency = expense.getCurrency();
        BigDecimal amount = expense.getAmount();

        if(appCurrency!=expenseCurrency) {
            response.setAmount(currencyService.convertValue(amount, expenseCurrency, appCurrency));
        }
        else {
            response.setAmount(amount);
        }

        response.setAmount(expense.getAmount());
        response.setCategory(expense.getCategory());
        response.setDate(expense.getDate());
        response.setCurrency(expense.getCurrency());
        response.setTripName(expense.getTrip().getTripName());
        return response;
    }

    private void verifyExpenseDates(Trip trip, ExpenseRequest request){
        if(trip.getStartDate().isAfter(request.getDate()))
            throw new IllegalArgumentException("Expense date cannot be before trip start date.");

        if(trip.getEndDate()!=null && trip.getEndDate().isBefore(request.getDate()))
            throw new IllegalArgumentException("Expense date cannot be after trip end date.");

    }

}
