package com.budget.ms.repository;

import com.budget.ms.entity.Expense;
import com.budget.ms.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip,Long> {
    Optional<Trip> findByTripName(String tripName);
}
