package com.hr_management.hr.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hr_management.hr.entity.LeaveTypeConfig;
import com.hr_management.hr.enums.LeaveType;
import com.hr_management.hr.exception.LeaveAPIException;
import com.hr_management.hr.exception.ResourceNotFoundException;
import com.hr_management.hr.model.LeaveTypeConfigDto;
import com.hr_management.hr.repository.LeaveTypeConfigRepository;
import com.hr_management.hr.service.LeaveTypeConfigService;

@Service
public class LeaveTypeConfigServiceImpl implements LeaveTypeConfigService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveTypeConfigServiceImpl.class);
    
    private final LeaveTypeConfigRepository leaveTypeConfigRepository;

    public LeaveTypeConfigServiceImpl(LeaveTypeConfigRepository leaveTypeConfigRepository) {
        this.leaveTypeConfigRepository = leaveTypeConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeConfigDto> getAllLeaveTypeConfigs() {
        logger.info("Fetching all leave type configurations");
        return leaveTypeConfigRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveTypeConfigDto getLeaveTypeConfig(LeaveType leaveType) {
        logger.info("Fetching leave type configuration for: {}", leaveType);
        return leaveTypeConfigRepository.findByLeaveType(leaveType)
                .map(this::convertToDto)
                .orElseThrow(() -> new LeaveAPIException("Leave type configuration not found for: " + leaveType));
    }

    @Override
    @Transactional
    public LeaveTypeConfigDto createLeaveTypeConfig(LeaveTypeConfigDto configDto) {
        logger.info("Creating new leave type configuration: {}", configDto);
        if (leaveTypeConfigRepository.findByLeaveType(configDto.getLeaveType()).isPresent()) {
            throw new LeaveAPIException("Leave type configuration already exists for: " + configDto.getLeaveType());
        }

        LeaveTypeConfig config = new LeaveTypeConfig();
        updateConfigFromDto(config, configDto);
        return convertToDto(leaveTypeConfigRepository.save(config));
    }

    @Override
    @Transactional
    public LeaveTypeConfigDto updateLeaveTypeConfig(Long id, LeaveTypeConfigDto configDto) {
        LeaveTypeConfig config = leaveTypeConfigRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LeaveTypeConfig", "id", id));
        updateConfigFromDto(config, configDto);
        return convertToDto(leaveTypeConfigRepository.save(config));
    }

    @Override
    @Transactional
    public void deleteLeaveTypeConfig(Long id) {
        if (!leaveTypeConfigRepository.existsById(id)) {
            throw new ResourceNotFoundException("LeaveTypeConfig", "id", id);
        }
        leaveTypeConfigRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleLeaveTypeStatus(LeaveType leaveType, boolean isActive) {
        logger.info("Toggling leave type status for: {} to: {}", leaveType, isActive);
        LeaveTypeConfig config = leaveTypeConfigRepository.findByLeaveType(leaveType)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveTypeConfig", "leaveType", 0L));
        config.setIsActive(isActive);
        leaveTypeConfigRepository.save(config);
    }

    @Override
    public LeaveTypeConfigDto getLeaveTypeConfigById(Long id) {
        return leaveTypeConfigRepository.findById(id)
            .map(this::convertToDto)
            .orElseThrow(() -> new ResourceNotFoundException("LeaveTypeConfig", "id", id));
    }

    @Override
    public LeaveTypeConfigDto getLeaveTypeConfigByType(LeaveType leaveType) {
        return leaveTypeConfigRepository.findByLeaveType(leaveType)
            .map(this::convertToDto)
            .orElseThrow(() -> new ResourceNotFoundException("LeaveTypeConfig", "leaveType", 0L));
    }

    @Override
    public boolean isLeaveTypeActive(LeaveType leaveType) {
        return leaveTypeConfigRepository.findByLeaveType(leaveType)
            .map(LeaveTypeConfig::getIsActive)
            .orElse(false);
    }

    @Override
    public int getAnnualLimit(LeaveType leaveType) {
        return leaveTypeConfigRepository.findByLeaveType(leaveType)
            .map(LeaveTypeConfig::getAnnualLimit)
            .orElse(0);
    }

    @Override
    public boolean requiresDocument(LeaveType leaveType) {
        return leaveTypeConfigRepository.findByLeaveType(leaveType)
            .map(LeaveTypeConfig::getRequiresDocument)
            .orElse(false);
    }

    private LeaveTypeConfigDto convertToDto(LeaveTypeConfig config) {
        return LeaveTypeConfigDto.builder()
            .id(config.getId())
            .leaveType(config.getLeaveType())
            .annualLimit(config.getAnnualLimit())
            .requiresDocument(config.getRequiresDocument())
            .description(config.getDescription())
            .active(config.getIsActive())
            .build();
    }

    private void updateConfigFromDto(LeaveTypeConfig config, LeaveTypeConfigDto dto) {
        config.setLeaveType(dto.getLeaveType());
        config.setAnnualLimit(dto.getAnnualLimit());
        config.setRequiresDocument(dto.isRequiresDocument());
        config.setDescription(dto.getDescription());
        config.setIsActive(dto.isActive());
    }
} 