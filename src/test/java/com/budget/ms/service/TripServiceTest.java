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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class TripServiceTest {

    private TripRepository tripRepository;
    private ExpenseRepository expenseRepository;
    private RecurringExpenseRepository recurringExpenseRepository;
    private SettingsService settingsService;
    private CurrencyService currencyService;
    private RecurringExpenseService recurringExpenseService;
    private ExpenseService expenseService;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        expenseRepository = mock(ExpenseRepository.class);
        recurringExpenseRepository = mock(RecurringExpenseRepository.class);
        settingsService = mock(SettingsService.class);
        currencyService = mock(CurrencyService.class);
        recurringExpenseService = mock(RecurringExpenseService.class);

        tripService = new TripService(
                tripRepository,
                expenseRepository,
                recurringExpenseRepository,
                settingsService,
                currencyService,
                recurringExpenseService,
                expenseService
        );
    }

    @Test
    void shouldGetTrip() {
        Trip trip = createTrip();

        when(settingsService.getCurrency())
                .thenReturn("USD");

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        TripResponse response = tripService.getTrip(1L);
        
        assertEquals(1L, response.getId());

        assertEquals(
                "Tokyo Trip",
                response.getTripName()
        );

        assertEquals(
                LocalDate.of(2025, 5, 1),
                response.getStartDate()
        );

        assertEquals(
                LocalDate.of(2025, 5, 10),
                response.getEndDate()
        );

        assertEquals(
                0,
                BigDecimal.valueOf(1000)
                        .compareTo(
                                response.getTripBudget()
                        )
        );

        assertNull(response.getDailySpending());
        assertNull(response.getCurrency());
    }

    @Test
    void shouldThrowWhenTripNotFound() {
        when(tripRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tripService.getTrip(1L)
        );
    }

    @Test
    void shouldCreateTrip() {
        TripRequest request = createTripRequest();

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.empty());

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> {
                    Trip trip = invocation.getArgument(0);
                    trip.setId(1L);
                    return trip;
                });

        when(settingsService.getCurrency())
                .thenReturn("USD");

        
        TripResponse response =
                tripService.createTrip(request);

        
        assertEquals(1L, response.getId());

        assertEquals(
                "Tokyo Trip",
                response.getTripName()
        );

        assertEquals(
                LocalDate.of(2025, 5, 1),
                response.getStartDate()
        );

        assertEquals(
                LocalDate.of(2025, 5, 10),
                response.getEndDate()
        );

        assertEquals(
                0,
                BigDecimal.valueOf(1000)
                        .compareTo(
                                response.getTripBudget()
                        )
        );

        assertNull(response.getDailySpending());
        assertNull(response.getCurrency());
    }

    @Test
    void shouldThrowWhenTripNameAlreadyExists() {
        Trip trip = createTrip();

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        assertThrows(
                DuplicateResourceException.class,
                () -> tripService.createTrip(
                        createTripRequest()
                )
        );
    }

    @Test
    void shouldThrowWhenEndDateIsProvidedWithExcessFields() {
        TripRequest request = createTripRequest();
        request.setDailySpending(new BigDecimal("50"));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(request)
        );
    }

    @Test
    void shouldThrowWhenEndDateIsntProvidedWithMissingFields() {
        TripRequest request = createTripRequest();
        request.setEndDate(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(request)
        );
    }

    @Test
    void shouldDeleteTrip() {
        Trip trip = createTrip();

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));
        
        tripService.deleteTrip(1L);
        
        verify(tripRepository)
                .delete(trip);
    }

    @Test
    void shouldValidateTrip() {
        TripRequest request = createTripRequest();
        
        assertDoesNotThrow(() ->
                tripService.validateTrip(request)
        );
    }

    @Test
    void shouldThrowWhenEndDateBeforeStartDate() {
        TripRequest request = createTripRequest();

        request.setEndDate(
                LocalDate.of(2025, 4, 1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.validateTrip(request)
        );
    }

    @Test
    void shouldThrowWhenBudgetIsZero() {
        TripRequest request = createTripRequest();

        request.setTripBudget(BigDecimal.ZERO);

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.validateTrip(request)
        );
    }

    @Test
    void shouldGetBudgetSummary() {
        Trip trip = createTrip();

        Expense expense1 = createExpense(
                BigDecimal.valueOf(100),
                trip
        );

        Expense expense2 = createExpense(
                BigDecimal.valueOf(50),
                trip
        );

        trip.setExpenses(
                List.of(expense1, expense2)
        );

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        when(settingsService.getCurrency())
                .thenReturn("USD");

        when(currencyService.convertValue(
                any(),
                anyString(),
                anyString()
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        BudgetSummaryResponse response =
                tripService.getBudgetSummary(1L);
        
        assertEquals(1L, response.getId());

        assertEquals(
                "Tokyo Trip",
                response.getName()
        );

        assertEquals(
                "USD",
                response.getCurrency()
        );

        assertEquals(
                LocalDate.of(2025, 5, 1),
                response.getStartDate()
        );

        assertEquals(
                LocalDate.of(2025, 5, 10),
                response.getEndDate()
        );

        assertEquals(
                0,
                BigDecimal.valueOf(150)
                        .compareTo(
                                response.getAmountSpent()
                        )
        );

        assertEquals(
                0,
                BigDecimal.valueOf(850)
                        .compareTo(
                                response.getAmountRemaining()
                        )
        );
    }

    @Test
    void shouldGetTripAnalyticsWithEndDate() {
        Trip trip = createTrip();

        Expense expense = createExpense(
                BigDecimal.valueOf(200),
                trip
        );

        trip.setExpenses(List.of(expense));

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        when(settingsService.getCurrency())
                .thenReturn("USD");

        when(currencyService.convertValue(
                any(),
                anyString(),
                anyString()
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        AnalyticsResponse response =
                tripService.getTripAnalytics(
                        1L,
                        LocalDate.of(2025, 5, 5)
                );

        
        assertEquals(
                LocalDate.of(2025, 5, 5),
                response.getDate()
        );

        assertEquals(
                0,
                BigDecimal.valueOf(200)
                        .compareTo(
                                response.getActual()
                        )
        );

        // 1000 / 9 days * 4 elapsed days
        assertEquals(
                0,
                BigDecimal.valueOf(444.44)
                        .compareTo(
                                response.getPredicted()
                        )
        );
    }

    @Test
    void shouldGetTripAnalyticsWithNoEndDate() {
        Trip trip = createTrip();

        Expense expense = createExpense(
                BigDecimal.valueOf(200),
                trip
        );

        trip.setExpenses(List.of(expense));
        trip.setEndDate(null);
        trip.setDailySpending(new BigDecimal("50"));
        trip.setCurrency("USD");

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        when(settingsService.getCurrency())
                .thenReturn("USD");

        when(currencyService.convertValue(
                any(),
                anyString(),
                anyString()
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        AnalyticsResponse response =
                tripService.getTripAnalytics(
                        1L,
                        LocalDate.of(2025, 5, 5)
                );


        assertEquals(
                LocalDate.of(2025, 5, 5),
                response.getDate()
        );

        assertEquals(
                0,
                BigDecimal.valueOf(200)
                        .compareTo(
                                response.getActual()
                        )
        );

        // 50 daily * 4
        assertEquals(
                0,
                BigDecimal.valueOf(200)
                        .compareTo(
                                response.getPredicted()
                        )
        );
    }

    @Test
    void shouldThrowWhenRequestingDateBefore(){
        Trip trip = createTrip();

        Expense expense = createExpense(
                BigDecimal.valueOf(200),
                trip
        );

        trip.setExpenses(List.of(expense));

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                tripService.getTripAnalytics(
                        1L,
                        LocalDate.of(2022, 5, 5)
                )
        );
    }


    @Test
    void shouldThrowWhenRequestingDateAfter(){
        Trip trip = createTrip();

        Expense expense = createExpense(
                BigDecimal.valueOf(200),
                trip
        );

        trip.setExpenses(List.of(expense));

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        tripService.getTripAnalytics(
                                1L,
                                LocalDate.of(2029, 5, 5)
                        )
        );
    }

    @Test
    void shouldThrowWhenNoDailySpendingAndNoEnd(){
        Trip trip = createTrip();

        Expense expense = createExpense(
                BigDecimal.valueOf(200),
                trip
        );

        trip.setExpenses(List.of(expense));
        trip.setDailySpending(null);
        trip.setEndDate(null);

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        tripService.getTripAnalytics(
                                1L,
                                LocalDate.of(2025, 5, 5)
                        )
        );
    }

    @Test
    void shouldEditTripAndRefreshRecurringExpenses() {
        Trip trip = createTrip();

        RecurringExpense recurring =
                new RecurringExpense();

        recurring.setId(1L);

        trip.setExpenses(List.of());

        TripRequest request = createTripRequest();

        request.setEndDate(
                LocalDate.of(2025, 5, 20)
        );

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        when(recurringExpenseRepository.findByTrip(trip))
                .thenReturn(List.of(recurring));

        when(settingsService.getCurrency())
                .thenReturn("USD");
        
        TripResponse response =
                tripService.editTrip(1L, request);

        assertEquals(1L, response.getId());

        assertEquals(
                "Tokyo Trip",
                response.getTripName()
        );

        assertEquals(
                LocalDate.of(2025, 5, 1),
                response.getStartDate()
        );

        assertEquals(
                LocalDate.of(2025, 5, 20),
                response.getEndDate()
        );

        assertEquals(
                0,
                BigDecimal.valueOf(1000)
                        .compareTo(
                                response.getTripBudget()
                        )
        );

        assertNull(response.getDailySpending());

        assertNull(response.getCurrency());

        verify(recurringExpenseService)
                .deleteRecurringExpenses(trip);

        verify(recurringExpenseService)
                .addRecurringExpenses(recurring, trip);
    }

    @Test
    void shouldThrowWhenEditingTripWithExpenseBefore() {
        Trip trip = createTrip();

        Expense expense = createExpense(
                BigDecimal.valueOf(100),
                trip
        );

        expense.setDate(
                LocalDate.of(2025, 4, 1)
        );

        trip.setExpenses(List.of(expense));

        TripRequest request = createTripRequest();

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

         
        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.editTrip(1L, request)
        );
    }

    @Test
    void shouldThrowWhenEditingTripWithExpenseAfter() {
        Trip trip = createTrip();

        Expense expense = createExpense(
                BigDecimal.valueOf(100),
                trip
        );

        expense.setDate(
                LocalDate.of(2025, 6, 1)
        );

        trip.setExpenses(List.of(expense));

        TripRequest request = createTripRequest();

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));


        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.editTrip(1L, request)
        );
    }

    private Trip createTrip() {
        Trip trip = new Trip();

        trip.setId(1L);
        trip.setTripName("Tokyo Trip");
        trip.setStartDate(
                LocalDate.of(2025, 5, 1)
        );
        trip.setEndDate(
                LocalDate.of(2025, 5, 10)
        );
        trip.setBudget(
                BigDecimal.valueOf(1000)
        );

        return trip;
    }

    private TripRequest createTripRequest() {
        TripRequest request = new TripRequest();

        request.setTripName("Tokyo Trip");
        request.setStartDate(
                LocalDate.of(2025, 5, 1)
        );
        request.setEndDate(
                LocalDate.of(2025, 5, 10)
        );
        request.setTripBudget(
                BigDecimal.valueOf(1000)
        );

        return request;
    }

    private Expense createExpense(
            BigDecimal amount,
            Trip trip
    ) {
        Expense expense = new Expense();

        expense.setId(1L);
        expense.setName("Food");
        expense.setAmount(amount);
        expense.setCurrency("USD");
        expense.setDate(
                LocalDate.of(2025, 5, 2)
        );
        expense.setTrip(trip);

        return expense;
    }
}