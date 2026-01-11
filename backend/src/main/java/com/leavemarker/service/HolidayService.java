package com.leavemarker.service;

import com.leavemarker.dto.holiday.HolidayRequest;
import com.leavemarker.dto.holiday.HolidayResponse;
import com.leavemarker.entity.Company;
import com.leavemarker.entity.Holiday;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.CompanyRepository;
import com.leavemarker.repository.HolidayRepository;
import com.leavemarker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public HolidayResponse createHoliday(HolidayRequest request, UserPrincipal currentUser) {
        Company company = companyRepository.findById(currentUser.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (request.getDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot create holiday for past dates");
        }

        Holiday holiday = Holiday.builder()
                .company(company)
                .name(request.getName())
                .date(request.getDate())
                .type(request.getType())
                .state(request.getState())
                .active(request.getActive())
                .build();

        holiday = holidayRepository.save(holiday);
        return mapToResponse(holiday);
    }

    public HolidayResponse getHoliday(Long id, UserPrincipal currentUser) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));

        if (!holiday.getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        return mapToResponse(holiday);
    }

    public List<HolidayResponse> getAllHolidays(UserPrincipal currentUser) {
        List<Holiday> holidays = holidayRepository.findByCompanyIdAndDeletedFalse(
                currentUser.getCompanyId());
        return holidays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<HolidayResponse> getActiveHolidays(UserPrincipal currentUser) {
        List<Holiday> holidays = holidayRepository.findByCompanyIdAndActiveAndDeletedFalse(
                currentUser.getCompanyId(), true);
        return holidays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<HolidayResponse> getHolidaysByDateRange(
            LocalDate startDate, LocalDate endDate, UserPrincipal currentUser) {
        List<Holiday> holidays = holidayRepository.findByCompanyIdAndDateRange(
                currentUser.getCompanyId(), startDate, endDate);
        return holidays.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HolidayResponse updateHoliday(Long id, HolidayRequest request, UserPrincipal currentUser) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));

        if (!holiday.getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        holiday.setName(request.getName());
        holiday.setDate(request.getDate());
        holiday.setType(request.getType());
        holiday.setState(request.getState());
        holiday.setActive(request.getActive());

        holiday = holidayRepository.save(holiday);
        return mapToResponse(holiday);
    }

    @Transactional
    public void deleteHoliday(Long id, UserPrincipal currentUser) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));

        if (!holiday.getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        holiday.setDeleted(true);
        holidayRepository.save(holiday);
    }

    private HolidayResponse mapToResponse(Holiday holiday) {
        return HolidayResponse.builder()
                .id(holiday.getId())
                .name(holiday.getName())
                .date(holiday.getDate())
                .type(holiday.getType())
                .state(holiday.getState())
                .active(holiday.getActive())
                .companyId(holiday.getCompany().getId())
                .companyName(holiday.getCompany().getName())
                .build();
    }
}
