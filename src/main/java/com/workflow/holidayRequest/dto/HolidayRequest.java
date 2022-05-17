package com.workflow.holidayRequest.dto;

import lombok.Data;

@Data
public class HolidayRequest {

    String empName;

    Long noOfHolidays;

    String requestDescription;

    boolean substitute;
}
