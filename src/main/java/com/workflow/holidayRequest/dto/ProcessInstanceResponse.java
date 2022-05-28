package com.workflow.holidayRequest.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ProcessInstanceResponse {
    private String processId;
    private boolean isEnded;
}
