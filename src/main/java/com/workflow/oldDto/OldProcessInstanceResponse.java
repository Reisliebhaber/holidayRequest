package com.workflow.oldDto;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class OldProcessInstanceResponse {
  String processId;
  boolean isEnded;
}
