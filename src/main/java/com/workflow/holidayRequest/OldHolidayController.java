package com.workflow.holidayRequest;

import java.util.List;

import com.workflow.oldDto.OldHolidayRequest;
import com.workflow.oldDto.OldProcessInstanceResponse;
import com.workflow.oldDto.OldTaskDetails;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OldHolidayController {

    OldHolidayService oldHolidayService;

    //********************************************************** deployment endpoints **********************************************************
    @PostMapping("/deploy")
    public void deployWorkflow() {
        oldHolidayService.deployProcessDefinition();
    }

    //********************************************************** process endpoints **********************************************************
    @PostMapping("/holiday/apply")
    public OldProcessInstanceResponse applyHoliday(@RequestBody OldHolidayRequest oldHolidayRequest) {
        return oldHolidayService.applyHoliday(oldHolidayRequest);
    }


    @GetMapping("/manager/tasks")
    public List<OldTaskDetails> getTasks() {
        return oldHolidayService.getManagerTasks();
    }


    @PostMapping("/manager/approve/tasks/{taskId}/{approved}")
    public void approveTask(@PathVariable("taskId") String taskId,@PathVariable("approved") Boolean approved){
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

    /**
     * TODO remove
     * @param processId
     */
    @GetMapping("/process/{processId}")
    public void checkState(@PathVariable("processId") String processId){
        oldHolidayService.checkProcessHistory(processId);
    }



}
