package com.budget.ms.service;

import com.budget.ms.dto.AnalyticsResponse;
import com.budget.ms.dto.BudgetSummaryResponse;
import com.budget.ms.dto.TripRequest;
import com.budget.ms.dto.TripResponse;
import com.budget.ms.entity.Expense;
import com.budget.ms.entity.RecurringExpense;
import com.budget.ms.entity.Trip;
import com.budget.ms.exception.DuplicateResourceException;
import com.budget.ms.exception.ResourceNotFoundException;
import com.budget.ms.repository.ExpenseRepository;
import com.budget.ms.repository.RecurringExpenseRepository;
import com.budget.ms.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final SettingsService settingsService;
    private final CurrencyService currencyService;
    private final RecurringExpenseService recurringExpenseService;

    public TripService(TripRepository tripRepository, ExpenseRepository expenseRepository, RecurringExpenseRepository recurringExpenseRepository, SettingsService settingsService, CurrencyService currencyService, RecurringExpenseService recurringExpenseService){
        this.tripRepository = tripRepository;
        this.expenseRepository = expenseRepository;
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.settingsService = settingsService;
        this.currencyService = currencyService;
        this.recurringExpenseService = recurringExpenseService;
    }

    public TripResponse getTrip(Long id){
        Trip trip = tripRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        return mapToResponse(trip);
    }

    public List<TripResponse> getAllTrips(){
        return tripRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AnalyticsResponse getTripAnalytics(Long id, LocalDate date){
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if(trip.getStartDate().isAfter(date))
            throw new IllegalArgumentException("Requested date cannot be before trip start date.");

        if(trip.getEndDate()!=null && trip.getEndDate().isBefore(date))
            throw new IllegalArgumentException("Requested date cannot be after trip end date.");

        if(trip.getEndDate()==null && (trip.getDailySpending() == null || trip.getCurrency() == null)){
            throw new IllegalArgumentException("Cannot provide average if there is no end date or no daily spending configured.");
        }

        AnalyticsResponse analyticsResponse = new AnalyticsResponse();

        if(trip.getEndDate()!=null){
            BigDecimal tripDuration = BigDecimal.valueOf(trip.getStartDate().until(trip.getEndDate(), ChronoUnit.DAYS));
            BigDecimal daysBetween = BigDecimal.valueOf(trip.getStartDate().until(date, ChronoUnit.DAYS));
            analyticsResponse.setPredicted(trip.getBudget().divide(tripDuration, 2, RoundingMode.HALF_UP).multiply(daysBetween));
            analyticsResponse.setDate(date);
        }
        else{
            BigDecimal tripDuration = BigDecimal.valueOf(trip.getStartDate().until(date, ChronoUnit.DAYS));
            BigDecimal predictedAmountInCurrency = trip.getDailySpending().multiply(tripDuration);
            analyticsResponse.setPredicted(currencyService.convertValue(predictedAmountInCurrency,trip.getCurrency(),settingsService.getCurrency()));
            analyticsResponse.setDate(date);
        }

            analyticsResponse.setDate(date);
            analyticsResponse.setActual(
                    trip.getExpenses().
                            stream().
                            filter(expense -> expense.getDate().isBefore(date) || expense.getDate().isEqual(date)).
                            map(expense -> currencyService.convertValue(expense.getAmount(), expense.getCurrency(), settingsService.getCurrency())).
                            reduce(BigDecimal.ZERO, BigDecimal::add)
        );
            return analyticsResponse;
    }


    public TripResponse createTrip(TripRequest tripRequest){
        if(tripRepository.findByTripName(tripRequest.getTripName()).isPresent())
            throw new DuplicateResourceException("Trip name already exists.");

        validateTrip(tripRequest);

        Trip trip = new Trip();
        trip.setTripName(tripRequest.getTripName());
        trip.setStartDate(tripRequest.getStartDate());
        trip.setEndDate(tripRequest.getEndDate());
        trip.setBudget(tripRequest.getTripBudget());
        trip.setDailySpending(tripRequest.getDailySpending());
        trip.setCurrency(tripRequest.getCurrency());
        return mapToResponse(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse editTrip(Long id, TripRequest tripRequest){
        Trip trip = tripRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        validateTrip(tripRequest);

        //make sure if changing trip dates that expenses dates are correct

        List<Expense> expenses = trip.getExpenses();
        if(expenses.stream().anyMatch(expense -> {
            if(tripRequest.getStartDate().isAfter(expense.getDate()) && expense.getRecurringExpense()==null)
                return true;
            else if(tripRequest.getEndDate()!=null && expense.getDate().isAfter(tripRequest.getEndDate()) && expense.getRecurringExpense()==null){
                return true;
            }
            else
                return false;
        }))
        {
            throw new IllegalArgumentException("All expense dates must be within trip dates. Please change expense dates first.");
        }

        boolean datesChanged = !Objects.equals(tripRequest.getEndDate(), trip.getEndDate()) || !tripRequest.getStartDate().isEqual(trip.getStartDate());

        trip.setTripName(tripRequest.getTripName());
        trip.setStartDate(tripRequest.getStartDate());
        trip.setEndDate(tripRequest.getEndDate());
        trip.setBudget(tripRequest.getTripBudget());
        trip.setDailySpending(tripRequest.getDailySpending());
        trip.setCurrency(tripRequest.getCurrency());

        if(datesChanged){
            recurringExpenseService.deleteRecurringExpenses(trip);

            List<RecurringExpense> recurringExpenses = recurringExpenseRepository.findByTrip(trip);
            recurringExpenses.forEach(recurring ->
                    {
                        recurringExpenseService.addRecurringExpenses(recurring, trip);
                    }
            );
        }

        return mapToResponse(trip);
    }

    public void validateTrip(TripRequest tripRequest){
        LocalDate startDate = tripRequest.getStartDate();
        LocalDate endDate = tripRequest.getEndDate();
        BigDecimal tripBudget = tripRequest.getTripBudget();

        if(endDate!=null && endDate.isBefore(startDate)){
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        if (tripBudget.compareTo(BigDecimal.ZERO) < 0 || tripBudget.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("Budget cannot be 0 or negative.");
        }

        boolean endDatePresent = endDate!=null;
        boolean currencyPresent = tripRequest.getCurrency()!=null && !tripRequest.getCurrency().isBlank();
        boolean dailySpendingPresent = tripRequest.getDailySpending()!=null;

        //either provide currency/dailybudget or enddate
        if(endDatePresent && (currencyPresent || dailySpendingPresent) ){
            throw new IllegalArgumentException("End date is provided. Please exclude currency and daily spending.");
        }
        else if(!endDatePresent && (!currencyPresent || !dailySpendingPresent)){
            throw new IllegalArgumentException("If no end date is provided, both currency and daily spending must be provided");
        }
    }

    public void deleteTrip(Long id){
        Trip trip = tripRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        tripRepository.delete(trip);
    }

    public BudgetSummaryResponse getBudgetSummary(Long id){
        Trip trip = tripRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        BudgetSummaryResponse budgetSummary = new BudgetSummaryResponse();

        List<Expense> expenses = trip.getExpenses();

        budgetSummary.setId(trip.getId());
        budgetSummary.setName(trip.getTripName());
        budgetSummary.setCurrency(settingsService.getCurrency());
        budgetSummary.setStartDate(trip.getStartDate());
        budgetSummary.setEndDate(trip.getEndDate());

        BigDecimal totalSpent = expenses.stream().
                map(expense -> currencyService.convertValue(expense.getAmount(), expense.getCurrency(), settingsService.getCurrency())).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal amountRemaining = trip.getBudget().subtract(totalSpent);

        budgetSummary.setAmountSpent(totalSpent);
        budgetSummary.setAmountRemaining(amountRemaining);

        return budgetSummary;
    }


    private TripResponse mapToResponse(Trip trip){
        TripResponse tripResponse = new TripResponse();
        tripResponse.setId(trip.getId());
        tripResponse.setTripName(trip.getTripName());
        tripResponse.setStartDate(trip.getStartDate());
        tripResponse.setEndDate(trip.getEndDate());
        tripResponse.setTripBudget(trip.getBudget());
        tripResponse.setDailySpending(trip.getDailySpending());
        tripResponse.setCurrency(trip.getCurrency());
        return tripResponse;
    }
}
