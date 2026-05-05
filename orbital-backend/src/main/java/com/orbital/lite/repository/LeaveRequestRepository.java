package com.orbital.lite.repository;

import com.orbital.lite.entity.AppUser;
import com.orbital.lite.entity.LeaveRequest;
import com.orbital.lite.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUserOrderByCreatedAtDesc(AppUser user);

    List<LeaveRequest> findAllByOrderByCreatedAtDesc();

    long countByStatus(LeaveStatus status);
}
