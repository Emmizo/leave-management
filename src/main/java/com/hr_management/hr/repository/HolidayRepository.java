package com.hr_management.hr.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hr_management.hr.entity.Holiday;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    
    List<Holiday> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Holiday> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);
    
    List<Holiday> findByRecurringTrue();
} 