package com.budget.ms.service;

import com.budget.ms.dto.RecurringExpenseRequest;
import com.budget.ms.dto.RecurringExpenseResponse;
import com.budget.ms.entity.RecurringExpense;
import com.budget.ms.entity.Trip;
import com.budget.ms.exception.ResourceNotFoundException;
import com.budget.ms.model.ExpenseCategory;
import com.budget.ms.model.RecurringCostDuration;
import com.budget.ms.repository.ExpenseRepository;
import com.budget.ms.repository.RecurringExpenseRepository;
import com.budget.ms.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecurringExpenseServiceTest {

    private RecurringExpenseRepository recurringExpenseRepository;
    private TripRepository tripRepository;
    private ExpenseRepository expenseRepository;
    private ExpenseService expenseService;

    private RecurringExpenseService recurringExpenseService;

    @BeforeEach
    void setUp() {
        recurringExpenseRepository = mock(RecurringExpenseRepository.class);
        tripRepository = mock(TripRepository.class);
        expenseRepository = mock(ExpenseRepository.class);
        expenseService = mock(ExpenseService.class);

        recurringExpenseService = new RecurringExpenseService(
                recurringExpenseRepository,
                tripRepository,
                expenseRepository,
                expenseService
        );
    }

    @Test
    void shouldGetRecurringExpenseById() {
        Trip trip = createTrip();

        RecurringExpense recurringExpense =
                createRecurringExpense(trip);

        when(recurringExpenseRepository.findById(1L))
                .thenReturn(Optional.of(recurringExpense));

        RecurringExpenseResponse response =
                recurringExpenseService.getRecurringExpenseById(1L);

        assertEquals("Hotel", response.getName());
        assertEquals("Tokyo Trip", response.getTripName());
    }

    @Test
    void shouldThrowWhenRecurringExpenseNotFound() {
        when(recurringExpenseRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> recurringExpenseService
                        .getRecurringExpenseById(1L)
        );
    }

    @Test
    void shouldCreateRecurringExpense() {
        Trip trip = createTrip();

        RecurringExpenseRequest request =
                createRequest();

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        when(recurringExpenseRepository.save(any()))
                .thenAnswer(invocation -> {
                    RecurringExpense recurring =
                            invocation.getArgument(0);

                    recurring.setId(1L);

                    return recurring;
                });

        RecurringExpenseResponse response =
                recurringExpenseService
                        .createRecurringExpense(request);

        assertEquals("Hotel", response.getName());
        assertEquals(
                RecurringCostDuration.DAILY,
                response.getCostDuration()
        );

        verify(recurringExpenseRepository)
                .save(any(RecurringExpense.class));

        verify(expenseRepository, atLeastOnce())
                .save(any());
    }

    @Test
    void shouldThrowWhenTripNotFoundDuringCreate() {
        RecurringExpenseRequest request =
                createRequest();

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> recurringExpenseService
                        .createRecurringExpense(request)
        );
    }

    @Test
    void shouldThrowWhenRecurringExpenseStartsBeforeTrip() {
        Trip trip = createTrip();

        RecurringExpenseRequest request =
                createRequest();

        request.setStartDate(LocalDate.of(2025, 4, 1));

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () -> recurringExpenseService
                        .createRecurringExpense(request)
        );
    }

    @Test
    void shouldThrowWhenRecurringExpenseStartsAfterEndTrip() {
        Trip trip = createTrip();

        RecurringExpenseRequest request =
                createRequest();

        request.setStartDate(LocalDate.of(2027, 4, 1));
        request.setEndDate(LocalDate.of(2027, 12, 1));

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () -> recurringExpenseService
                        .createRecurringExpense(request)
        );
    }

    @Test
    void shouldThrowWhenRecurringExpenseStartsAfterItEnds() {
        Trip trip = createTrip();

        RecurringExpenseRequest request =
                createRequest();

        request.setStartDate(LocalDate.of(2025, 5, 5));
        request.setEndDate(LocalDate.of(2025, 5, 4));

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () -> recurringExpenseService
                        .createRecurringExpense(request)
        );
    }

    @Test
    void shouldThrowWhenNoEndDatesAreProvided() {
        Trip trip = createTrip();
        trip.setEndDate(null);

        RecurringExpenseRequest request =
                createRequest();

        request.setEndDate(null);

        request.setStartDate(LocalDate.of(2026, 4, 1));

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () -> recurringExpenseService
                        .createRecurringExpense(request)
        );
    }


    @Test
    void shouldEditRecurringExpense() {
        Trip trip = createTrip();

        RecurringExpense recurringExpense =
                createRecurringExpense(trip);

        RecurringExpenseRequest request =
                createRequest();

        request.setName("Airbnb");

        when(tripRepository.findByTripName("Tokyo Trip"))
                .thenReturn(Optional.of(trip));

        when(recurringExpenseRepository.findById(1L))
                .thenReturn(Optional.of(recurringExpense));

        when(recurringExpenseRepository.findByTrip(trip))
                .thenReturn(List.of(recurringExpense));

        RecurringExpenseResponse response =
                recurringExpenseService
                        .editRecurringExpense(1L, request);

        assertEquals("Airbnb", response.getName());

        verify(expenseRepository)
                .deleteByRecurringExpenseId(1L);

        verify(expenseRepository, atLeastOnce())
                .save(any());
    }

    @Test
    void shouldDeleteRecurringExpenseById() {
        recurringExpenseService.deleteRecurringExpenseById(1L);

        verify(recurringExpenseRepository)
                .deleteById(1L);
    }

    @Test
    void shouldDeleteRecurringExpenses() {
        Trip trip = createTrip();

        RecurringExpense recurringExpense1 =
                createRecurringExpense(trip);

        recurringExpense1.setId(1L);

        RecurringExpense recurringExpense2 =
                createRecurringExpense(trip);

        recurringExpense2.setId(2L);

        when(recurringExpenseRepository.findByTrip(trip))
                .thenReturn(List.of(
                        recurringExpense1,
                        recurringExpense2
                ));

        recurringExpenseService
                .deleteRecurringExpenses(trip);

        verify(expenseRepository)
                .deleteByRecurringExpenseId(1L);

        verify(expenseRepository)
                .deleteByRecurringExpenseId(2L);
    }

    @Test
    void shouldGenerateDailyRecurringExpenses() {
        Trip trip = createTrip();

        RecurringExpense recurringExpense =
                createRecurringExpense(trip);

        recurringExpense.setStartDate(
                LocalDate.of(2025, 5, 1)
        );

        recurringExpense.setEndDate(
                LocalDate.of(2025, 5, 3)
        );

        recurringExpense.setCostDuration(
                RecurringCostDuration.DAILY
        );

        recurringExpenseService.addRecurringExpenses(
                recurringExpense,
                trip
        );

        // then
        // May 1, 2, 3 = 3 expenses
        verify(expenseRepository, times(3))
                .save(any());
    }

    private Trip createTrip() {
        Trip trip = new Trip();

        trip.setId(1L);
        trip.setTripName("Tokyo Trip");
        trip.setStartDate(LocalDate.of(2025, 5, 1));
        trip.setEndDate(LocalDate.of(2025, 5, 10));

        return trip;
    }

    private RecurringExpense createRecurringExpense(
            Trip trip
    ) {
        RecurringExpense recurringExpense =
                new RecurringExpense();

        recurringExpense.setId(1L);
        recurringExpense.setName("Hotel");
        recurringExpense.setAmount(
                BigDecimal.valueOf(100)
        );
        recurringExpense.setCategory(ExpenseCategory.ACCOMMODATION);
        recurringExpense.setCurrency("USD");
        recurringExpense.setStartDate(
                LocalDate.of(2025, 5, 1)
        );
        recurringExpense.setEndDate(
                LocalDate.of(2025, 5, 3)
        );
        recurringExpense.setCostDuration(
                RecurringCostDuration.DAILY
        );
        recurringExpense.setTrip(trip);

        return recurringExpense;
    }

    private RecurringExpenseRequest createRequest() {
        RecurringExpenseRequest request =
                new RecurringExpenseRequest();

        request.setName("Hotel");
        request.setAmount(
                BigDecimal.valueOf(100)
        );
        request.setCategory(ExpenseCategory.ACCOMMODATION);
        request.setCurrency("USD");
        request.setTripName("Tokyo Trip");
        request.setStartDate(
                LocalDate.of(2025, 5, 1)
        );
        request.setEndDate(
                LocalDate.of(2025, 5, 3)
        );
        request.setCostDuration(
                RecurringCostDuration.DAILY
        );

        return request;
    }
}