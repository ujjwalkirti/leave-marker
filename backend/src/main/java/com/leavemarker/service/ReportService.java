package com.leavemarker.service;

import com.leavemarker.entity.Attendance;
import com.leavemarker.entity.LeaveApplication;
import com.leavemarker.entity.LeaveBalance;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.repository.AttendanceRepository;
import com.leavemarker.repository.LeaveApplicationRepository;
import com.leavemarker.repository.LeaveBalanceRepository;
import com.leavemarker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final AttendanceRepository attendanceRepository;
    private final SubscriptionFeatureService subscriptionFeatureService;

    public byte[] generateLeaveBalanceReport(Integer year, UserPrincipal currentUser, String format) {
        // Check if advanced reports are available on their plan
        subscriptionFeatureService.validateReportsAccess(currentUser.getCompanyId());
        List<LeaveBalance> balances = leaveBalanceRepository
                .findByEmployeeCompanyIdAndYearAndDeletedFalse(currentUser.getCompanyId(), year);

        if ("csv".equalsIgnoreCase(format)) {
            return generateLeaveBalanceCsv(balances);
        } else if ("excel".equalsIgnoreCase(format)) {
            return generateLeaveBalanceExcel(balances);
        } else {
            throw new BadRequestException("Unsupported format. Use 'csv' or 'excel'");
        }
    }

    public byte[] generateAttendanceReport(LocalDate startDate, LocalDate endDate,
                                           UserPrincipal currentUser, String format) {
        // Check if advanced reports are available on their plan
        subscriptionFeatureService.validateReportsAccess(currentUser.getCompanyId());

        List<Attendance> attendances = attendanceRepository
                .findByCompanyIdAndDateRange(currentUser.getCompanyId(), startDate, endDate);

        if ("csv".equalsIgnoreCase(format)) {
            return generateAttendanceCsv(attendances);
        } else if ("excel".equalsIgnoreCase(format)) {
            return generateAttendanceExcel(attendances);
        } else {
            throw new BadRequestException("Unsupported format. Use 'csv' or 'excel'");
        }
    }

    public byte[] generateLeaveUsageReport(LocalDate startDate, LocalDate endDate,
                                           UserPrincipal currentUser, String format) {
        // Check if advanced reports are available on their plan
        subscriptionFeatureService.validateReportsAccess(currentUser.getCompanyId());

        List<LeaveApplication> leaves = leaveApplicationRepository
                .findByCompanyIdAndDateRange(currentUser.getCompanyId(), startDate, endDate);

        if ("csv".equalsIgnoreCase(format)) {
            return generateLeaveUsageCsv(leaves);
        } else if ("excel".equalsIgnoreCase(format)) {
            return generateLeaveUsageExcel(leaves);
        } else {
            throw new BadRequestException("Unsupported format. Use 'csv' or 'excel'");
        }
    }

    private byte[] generateLeaveBalanceCsv(List<LeaveBalance> balances) {
        StringBuilder csv = new StringBuilder();
        csv.append("Employee ID,Employee Name,Leave Type,Year,Total Quota,Used,Pending,Available,Carried Forward\n");

        for (LeaveBalance balance : balances) {
            csv.append(String.format("%s,%s,%s,%d,%.1f,%.1f,%.1f,%.1f,%.1f\n",
                    balance.getEmployee().getEmployeeId(),
                    balance.getEmployee().getFullName(),
                    balance.getLeaveType(),
                    balance.getYear(),
                    balance.getTotalQuota(),
                    balance.getUsed(),
                    balance.getPending(),
                    balance.getAvailable(),
                    balance.getCarriedForward()
            ));
        }

        return csv.toString().getBytes();
    }

    private byte[] generateLeaveBalanceExcel(List<LeaveBalance> balances) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Leave Balance Report");

            String[] headers = {"Employee ID", "Employee Name", "Leave Type", "Year",
                    "Total Quota", "Used", "Pending", "Available", "Carried Forward"};
            Row headerRow = sheet.createRow(0);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (LeaveBalance balance : balances) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(balance.getEmployee().getEmployeeId());
                row.createCell(1).setCellValue(balance.getEmployee().getFullName());
                row.createCell(2).setCellValue(balance.getLeaveType().toString());
                row.createCell(3).setCellValue(balance.getYear());
                row.createCell(4).setCellValue(balance.getTotalQuota());
                row.createCell(5).setCellValue(balance.getUsed());
                row.createCell(6).setCellValue(balance.getPending());
                row.createCell(7).setCellValue(balance.getAvailable());
                row.createCell(8).setCellValue(balance.getCarriedForward());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private byte[] generateAttendanceCsv(List<Attendance> attendances) {
        StringBuilder csv = new StringBuilder();
        csv.append("Employee ID,Employee Name,Date,Punch In,Punch Out,Work Type,Status,Remarks\n");

        for (Attendance attendance : attendances) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                    attendance.getEmployee().getEmployeeId(),
                    attendance.getEmployee().getFullName(),
                    attendance.getDate(),
                    attendance.getPunchInTime() != null ? attendance.getPunchInTime() : "",
                    attendance.getPunchOutTime() != null ? attendance.getPunchOutTime() : "",
                    attendance.getWorkType() != null ? attendance.getWorkType() : "",
                    attendance.getStatus(),
                    attendance.getRemarks() != null ? attendance.getRemarks() : ""
            ));
        }

        return csv.toString().getBytes();
    }

    private byte[] generateAttendanceExcel(List<Attendance> attendances) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Attendance Report");

            String[] headers = {"Employee ID", "Employee Name", "Date", "Punch In",
                    "Punch Out", "Work Type", "Status", "Remarks"};
            Row headerRow = sheet.createRow(0);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Attendance attendance : attendances) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(attendance.getEmployee().getEmployeeId());
                row.createCell(1).setCellValue(attendance.getEmployee().getFullName());
                row.createCell(2).setCellValue(attendance.getDate().toString());
                row.createCell(3).setCellValue(attendance.getPunchInTime() != null ?
                        attendance.getPunchInTime().toString() : "");
                row.createCell(4).setCellValue(attendance.getPunchOutTime() != null ?
                        attendance.getPunchOutTime().toString() : "");
                row.createCell(5).setCellValue(attendance.getWorkType() != null ?
                        attendance.getWorkType().toString() : "");
                row.createCell(6).setCellValue(attendance.getStatus().toString());
                row.createCell(7).setCellValue(attendance.getRemarks() != null ?
                        attendance.getRemarks() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private byte[] generateLeaveUsageCsv(List<LeaveApplication> leaves) {
        StringBuilder csv = new StringBuilder();
        csv.append("Employee ID,Employee Name,Leave Type,Start Date,End Date,Days,Status,Applied Date\n");

        for (LeaveApplication leave : leaves) {
            csv.append(String.format("%s,%s,%s,%s,%s,%.1f,%s,%s\n",
                    leave.getEmployee().getEmployeeId(),
                    leave.getEmployee().getFullName(),
                    leave.getLeaveType(),
                    leave.getStartDate(),
                    leave.getEndDate(),
                    leave.getNumberOfDays(),
                    leave.getStatus(),
                    leave.getCreatedAt().toLocalDate()
            ));
        }

        return csv.toString().getBytes();
    }

    private byte[] generateLeaveUsageExcel(List<LeaveApplication> leaves) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Leave Usage Report");

            String[] headers = {"Employee ID", "Employee Name", "Leave Type", "Start Date",
                    "End Date", "Days", "Status", "Applied Date"};
            Row headerRow = sheet.createRow(0);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (LeaveApplication leave : leaves) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(leave.getEmployee().getEmployeeId());
                row.createCell(1).setCellValue(leave.getEmployee().getFullName());
                row.createCell(2).setCellValue(leave.getLeaveType().toString());
                row.createCell(3).setCellValue(leave.getStartDate().toString());
                row.createCell(4).setCellValue(leave.getEndDate().toString());
                row.createCell(5).setCellValue(leave.getNumberOfDays());
                row.createCell(6).setCellValue(leave.getStatus().toString());
                row.createCell(7).setCellValue(leave.getCreatedAt().toLocalDate().toString());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
}
