package com.hr_management.hr.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hr_management.hr.entity.LeavePolicy;
import com.hr_management.hr.enums.LeaveType;
import com.hr_management.hr.repository.LeavePolicyRepository;
import com.hr_management.hr.service.LeavePolicyService;

@Service
public class LeavePolicyServiceImpl implements LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;

    public LeavePolicyServiceImpl(LeavePolicyRepository leavePolicyRepository) {
        this.leavePolicyRepository = leavePolicyRepository;
    }

    @Override
    public List<LeavePolicy> getAllLeavePolicies() {
        return leavePolicyRepository.findAll();
    }

    @Override
    public LeavePolicy getLeavePolicyById(Long id) {
        return leavePolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave policy not found with id: " + id));
    }

    @Override
    public LeavePolicy createLeavePolicy(LeavePolicy leavePolicy) {
        return leavePolicyRepository.save(leavePolicy);
    }

    @Override
    public LeavePolicy updateLeavePolicy(Long id, LeavePolicy leavePolicy) {
        LeavePolicy existingPolicy = getLeavePolicyById(id);
        existingPolicy.setName(leavePolicy.getName());
        existingPolicy.setDescription(leavePolicy.getDescription());
        existingPolicy.setDaysPerMonth(leavePolicy.getDaysPerMonth());
        existingPolicy.setCarryForwardDays(leavePolicy.getCarryForwardDays());
        existingPolicy.setMaxConsecutiveDays(leavePolicy.getMaxConsecutiveDays());
        existingPolicy.setMinNoticeDays(leavePolicy.getMinNoticeDays());
        existingPolicy.setRequiresApproval(leavePolicy.isRequiresApproval());
        existingPolicy.setActive(leavePolicy.isActive());
        return leavePolicyRepository.save(existingPolicy);
    }

    @Override
    public void deleteLeavePolicy(Long id) {
        LeavePolicy leavePolicy = getLeavePolicyById(id);
        leavePolicyRepository.delete(leavePolicy);
    }

    @Override
    public int getMaxConsecutiveDays(LeaveType leaveType) {
        // Find the active leave policy matching the given leave type
        return leavePolicyRepository.findAll().stream()
                .filter(LeavePolicy::isActive)
                .filter(policy -> policy.getName().equalsIgnoreCase(leaveType.name())) // Filter by leave type name (case-insensitive)
                .findFirst()
                .map(LeavePolicy::getMaxConsecutiveDays)
                .orElse(0); // Return 0 if no matching active policy found
    }
} 