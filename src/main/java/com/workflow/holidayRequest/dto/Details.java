package com.workflow.holidayRequest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Details {
    String detailId;
    String detailName;
    Map<String, Object> detailData;
}
