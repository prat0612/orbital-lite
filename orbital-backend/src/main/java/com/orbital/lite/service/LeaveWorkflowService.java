package com.orbital.lite.service;

import com.orbital.lite.entity.LeaveStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class LeaveWorkflowService {

    public LeaveStatus initialStatus(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return days <= 2 ? LeaveStatus.APPROVED : LeaveStatus.PENDING;
    }
}
