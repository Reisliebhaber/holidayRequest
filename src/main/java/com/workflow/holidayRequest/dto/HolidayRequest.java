package com.workflow.holidayRequest.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayRequest {

    String empName;

    Long noOfHolidays;

    String requestDescription;

    LocalDate startingDate;
}
