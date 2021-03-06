package com.workflow.holidayRequest.handler;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 *
 */
@Data
@Slf4j
public class HolidayRejectionHandler implements JavaDelegate {
    /**
     *
     * @param execution
     */
    @Override
    public void execute(DelegateExecution execution) {

        log.info("Holiday Rejected");

    }
}
