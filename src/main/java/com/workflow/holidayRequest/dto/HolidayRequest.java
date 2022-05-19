package com.workflow.holidayRequest.dto;

import lombok.Data;

import java.util.Date;

@Data
public class HolidayRequest {

    String empName;

    Long noOfHolidays;

    String requestDescription;

    Date startingDate;

    boolean substitute;
}
