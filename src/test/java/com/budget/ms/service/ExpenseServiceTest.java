package com.budget.ms.service;

import com.budget.ms.dto.ExpenseRequest;
import com.budget.ms.dto.ExpenseResponse;
import com.budget.ms.entity.Expense;
import com.budget.ms.entity.Trip;
import com.budget.ms.exception.ResourceNotFoundException;
import com.budget.ms.model.ExpenseCategory;
import com.budget.ms.repository.ExpenseRepository;
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
class ExpenseServiceTest {

    private ExpenseRepository expenseRepository;
    private TripRepository tripRepository;
    private CurrencyService currencyService;
    private SettingsService settingsService;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseRepository = mock(ExpenseRepository.class);
        tripRepository = mock(TripRepository.class);
        currencyService = mock(CurrencyService.class);
        settingsService = mock(SettingsService.class);

        expenseService = new ExpenseService(
                expenseRepository,
                tripRepository,
                currencyService,
                settingsService
        );
    }

    @Test
    void shouldGetExpenseById() {
        Trip trip = createTrip();
        Expense expense = createExpense(trip);

        when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));

        ExpenseResponse response = expenseService.getExpenseById(1L);

        assertEquals("Lunch", response.getName());
        assertEquals("Tokyo Trip", response.getTripName());
    }

    @Test
    void shouldThrowWhenExpenseNotFound() {
        when(expenseRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> expenseService.getExpenseById(1L)
        );
    }

    @Test
    void shouldGetExpensesByDateAndTrip() {
        Trip trip = createTrip();
        Expense expense = createExpense(trip);

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        when(expenseRepository.findByDateAndTrip_TripName(
                LocalDate.of(2025, 5, 1),
                "Tokyo Trip"
        )).thenReturn(List.of(expense));

        List<ExpenseResponse> responses =
                expenseService.getExpensesByDateAndTrip(
                        LocalDate.of(2025, 5, 1),
                        "Tokyo Trip"
                );

        assertEquals(1, responses.size());
        assertEquals("Lunch", responses.get(0).getName());
    }

    @Test
    void shouldGetTotalByDate() {
        Trip trip = createTrip();

        Expense expense1 = createExpense(trip);
        expense1.setAmount(BigDecimal.valueOf(10));

        Expense expense2 = createExpense(trip);
        expense2.setAmount(BigDecimal.valueOf(20));

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        when(expenseRepository.findByDateAndTrip_TripName(
                LocalDate.of(2025, 5, 1),
                "Tokyo Trip"
        )).thenReturn(List.of(expense1, expense2));

        when(settingsService.getCurrency())
                .thenReturn("USD");

        when(currencyService.convertValue(
                any(BigDecimal.class),
                anyString(),
                anyString()
        )).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal total = expenseService.getTotalByDate(
                LocalDate.of(2025, 5, 1),
                "Tokyo Trip"
        );

        assertEquals(
                0,
                BigDecimal.valueOf(30).compareTo(total)
        );
    }

    @Test
    void shouldCreateExpense() {
        Trip trip = createTrip();
        ExpenseRequest request = createRequest();

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        when(expenseRepository.save(any(Expense.class)))
                .thenAnswer(invocation -> {
                    Expense saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        ExpenseResponse response =
                expenseService.createExpense(request);

        assertEquals("Lunch", response.getName());
        assertEquals("USD", response.getCurrency());

        verify(currencyService)
                .validateCurrency("USD");

        verify(expenseRepository)
                .save(any(Expense.class));
    }

    @Test
    void shouldThrowWhenExpenseDateBeforeTripStart() {
        Trip trip = createTrip();

        ExpenseRequest request = createRequest();
        request.setDate(LocalDate.of(2025, 4, 1));

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.createExpense(request)
        );
    }

    @Test
    void shouldThrowWhenExpenseDateAfterTripEnd() {
        Trip trip = createTrip();

        ExpenseRequest request = createRequest();
        request.setDate(LocalDate.of(2025, 10, 1));

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.createExpense(request)
        );
    }

    @Test
    void shouldUpdateExpense() {
        Trip trip = createTrip();

        Expense expense = createExpense(trip);

        ExpenseRequest request = createRequest();
        request.setName("Dinner");

        when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        when(expenseRepository.save(any(Expense.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseResponse response =
                expenseService.updateExpense(1L, request);

        assertEquals("Dinner", response.getName());

        verify(expenseRepository)
                .save(any(Expense.class));
    }

    @Test
    void shouldDeleteExpense() {
        Trip trip = createTrip();

        Expense expense = createExpense(trip);

        when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));

        expenseService.deleteExpense(1L);

        verify(expenseRepository)
                .delete(expense);
    }

    @Test
    void shouldThrowWhenDeletingMissingExpense() {
        when(expenseRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> expenseService.deleteExpense(1L)
        );
    }

    private Trip createTrip() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setTripName("Tokyo Trip");
        trip.setStartDate(LocalDate.of(2025, 5, 1));
        trip.setEndDate(LocalDate.of(2025, 5, 10));

        return trip;
    }

    private Expense createExpense(Trip trip) {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setName("Lunch");
        expense.setAmount(BigDecimal.valueOf(25));
        expense.setCurrency("USD");
        expense.setCategory(ExpenseCategory.FOOD);
        expense.setDate(LocalDate.of(2025, 5, 1));
        expense.setTrip(trip);

        return expense;
    }

    private ExpenseRequest createRequest() {
        ExpenseRequest request = new ExpenseRequest();
        request.setName("Lunch");
        request.setAmount(BigDecimal.valueOf(25));
        request.setCurrency("USD");
        request.setCategory(ExpenseCategory.FOOD);
        request.setDate(LocalDate.of(2025, 5, 1));
        request.setTripName("Tokyo Trip");

        return request;
    }
}