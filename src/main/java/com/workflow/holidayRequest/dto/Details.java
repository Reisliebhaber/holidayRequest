package com.workflow.holidayRequest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Details {
    private String detailId;
    private String detailName;
    private Map<String, Object> detailData;
}
