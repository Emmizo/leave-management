package com.hr_management.hr.service;

import java.time.LocalDate;
import java.util.List;

import com.hr_management.hr.model.HolidayDto;

public interface HolidayService {
    
    HolidayDto createHoliday(HolidayDto holidayDto, String username);
    
    HolidayDto updateHoliday(Long id, HolidayDto holidayDto);
    
    void deleteHoliday(Long id);
    
    HolidayDto getHolidayById(Long id);
    
    List<HolidayDto> getAllHolidays();
    
    List<HolidayDto> getUpcomingHolidays();
    
    List<HolidayDto> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate);
    
    List<HolidayDto> getRecurringHolidays();
} 