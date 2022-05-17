package com.workflow.oldDto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OldTaskDetails {

    String taskId;
    String taskName;
    Map<String, Object> taskData;
}
