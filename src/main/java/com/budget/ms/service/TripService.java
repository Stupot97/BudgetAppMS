package com.budget.ms.service;

import com.budget.ms.dto.BudgetSummaryResponse;
import com.budget.ms.dto.TripRequest;
import com.budget.ms.dto.TripResponse;
import com.budget.ms.entity.Expense;
import com.budget.ms.entity.Trip;
import com.budget.ms.exception.DuplicateResourceException;
import com.budget.ms.exception.ResourceNotFoundException;
import com.budget.ms.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final SettingsService settingsService;
    private final CurrencyService currencyService;

    public TripService(TripRepository tripRepository, SettingsService settingsService, CurrencyService currencyService){
        this.tripRepository = tripRepository;
        this.settingsService = settingsService;
        this.currencyService = currencyService;
    }

    public TripResponse getTrip(Long id){
        Trip trip = tripRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        return mapToResponse(trip);
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
        return mapToResponse(tripRepository.save(trip));
    }

    public TripResponse editTrip(Long id, TripRequest tripRequest){
        Trip trip = tripRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        validateTrip(tripRequest);

        //make sure if changing trip dates that expenses dates are correct

        List<Expense> expenses = trip.getExpenses();
        if(expenses.stream().anyMatch(expense -> {
            if(tripRequest.getStartDate().isAfter(expense.getDate()))
                return true;
            else if(tripRequest.getEndDate()!=null && expense.getDate().isAfter(tripRequest.getEndDate())){
                return true;
            }
            else
                return false;
        }))
        {
            throw new IllegalArgumentException("All expense dates must be within trip dates. Please change expense dates first.");
        }

        trip.setTripName(tripRequest.getTripName());
        trip.setStartDate(tripRequest.getStartDate());
        trip.setEndDate(tripRequest.getEndDate());
        trip.setBudget(tripRequest.getTripBudget());
        return mapToResponse(tripRepository.save(trip));
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
        return tripResponse;
    }
}
