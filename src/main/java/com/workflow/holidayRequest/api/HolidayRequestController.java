package com.workflow.holidayRequest.api;

import com.workflow.holidayRequest.OldHolidayService;
import com.workflow.holidayRequest.dto.HolidayRequest;
import com.workflow.holidayRequest.dto.ProcessInstanceResponse;
import com.workflow.holidayRequest.dto.TaskDetails;
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
    public ProcessInstanceResponse applyHoliday(@RequestBody HolidayRequest holidayRequest) {
        holidayService.deployProcessDefinition();
        return holidayService.applyHoliday(holidayRequest);
    }

    @GetMapping("/employee/tasks")
    public List<TaskDetails> getEmployeeTasks() {
        return holidayService.getEmployeeTasks();
    }

    @PostMapping("/employee/addSubstitute/tasks/{taskId}/{employeeId}")
    public void addSubstitute(@PathVariable("taskId") String taskId, @PathVariable("employeeId") Integer employeeId) {
        holidayService.addSubstituteToHoliday(taskId, employeeId);
    }
    @PostMapping("/employee/noSubstitute/tasks/{taskId}")
    public void addNoSubstitute(@PathVariable("taskId") String taskId) {
        holidayService.addSubstituteToHoliday(taskId, null);
        //holidayService.addNoSubstituteToHoliday(taskId);
    }

    @GetMapping("/superior/tasks")
    public List<TaskDetails> getSuperiorTasks() {
        return holidayService.getManagerTasks();
    }


    @PostMapping("/manager/approve/tasks/{taskId}/{approved}")
    public void approveTask(@PathVariable("taskId") String taskId, @PathVariable("withSubstitute") Boolean withSubstitute) {
        holidayService.approveHoliday(taskId, withSubstitute);
    }

    @PostMapping("/user/accept/{taskId}")
    public void acceptHoliday(@PathVariable("taskId") String taskId) {
        oldHolidayService.acceptHoliday(taskId);
    }

    @GetMapping("/user/tasks")
    public List<TaskDetails> getUserTasks() {
        return holidayService.getUserTasks();
    }

}
