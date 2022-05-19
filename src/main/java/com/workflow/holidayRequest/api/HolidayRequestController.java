package com.workflow.holidayRequest.api;

import com.workflow.holidayRequest.OldHolidayService;
import com.workflow.holidayRequest.service.HolidayService;
import com.workflow.oldDto.OldHolidayRequest;
import com.workflow.oldDto.OldProcessInstanceResponse;
import com.workflow.oldDto.OldTaskDetails;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class HolidayRequestController {
    HolidayService holidayService;
    OldHolidayService oldHolidayService;//todo delete

    //********************************************************** deployment & process endpoints **************************************
    @PostMapping("/holiday/apply")
    public OldProcessInstanceResponse applyHoliday(@RequestBody OldHolidayRequest oldHolidayRequest) {
        holidayService.deployProcessDefinition();
        return oldHolidayService.applyHoliday(oldHolidayRequest);
    }

    @GetMapping("/manager/tasks")
    public List<OldTaskDetails> getTasks() {
        return oldHolidayService.getManagerTasks();
    }


    @PostMapping("/manager/approve/tasks/{taskId}/{approved}")
    public void approveTask(@PathVariable("taskId") String taskId, @PathVariable("approved") Boolean approved){
        oldHolidayService.approveHoliday(taskId,approved);
    }

    @PostMapping("/user/accept/{taskId}")
    public void acceptHoliday(@PathVariable("taskId") String taskId){
        oldHolidayService.acceptHoliday(taskId);
    }

    @GetMapping("/user/tasks")
    public List<OldTaskDetails> getUserTasks() {
        return oldHolidayService.getUserTasks();
    }

}
