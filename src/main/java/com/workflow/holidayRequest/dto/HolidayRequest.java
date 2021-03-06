package com.workflow.holidayRequest.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 *
 */
@Data
public class HolidayRequest {

    private String empName;
    private Long noOfHolidays;
    private String requestDescription;
    private LocalDate startingDate;
}
