package com.budget.ms.controller;

import com.budget.ms.dto.BudgetSummaryResponse;
import com.budget.ms.dto.AnalyticsResponse;
import com.budget.ms.dto.TripRequest;
import com.budget.ms.dto.TripResponse;
import com.budget.ms.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/trip")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService){
        this.tripService = tripService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTrip(
            @PathVariable("id") Long id){

        return ResponseEntity.ok(tripService.getTrip(id));
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<AnalyticsResponse> getTripAnalytics(
            @PathVariable("id") Long id,
            @RequestParam("date") LocalDate date
    ) {
        return ResponseEntity.ok(tripService.getTripAnalytics(id, date));
    }

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(
            @Valid @RequestBody TripRequest tripRequest){

        return ResponseEntity.ok(tripService.createTrip(tripRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> editTrip(
            @PathVariable("id") Long id,
            @Valid @RequestBody TripRequest tripRequest){

         return ResponseEntity.ok(tripService.editTrip(id, tripRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable("id") Long id){

        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/budgetSummary")
    public ResponseEntity<BudgetSummaryResponse> getTripSummary(
            @PathVariable("id") Long id
            ){
        return ResponseEntity.ok(tripService.getBudgetSummary(id));
    }
}
