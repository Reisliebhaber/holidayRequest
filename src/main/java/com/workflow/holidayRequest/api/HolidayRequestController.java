package com.workflow.holidayRequest.api;

import com.workflow.holidayRequest.dto.HolidayRequest;
import com.workflow.holidayRequest.dto.ProcessInstanceResponse;
import com.workflow.holidayRequest.dto.Details;
import com.workflow.holidayRequest.service.HolidayService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Class for Holiday Request API
 */
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class HolidayRequestController {
    HolidayService holidayService;

    //********************************************************** deployment & process endpoints **************************************
    /**
     * Method to apply a Holiday, needs an HolidayRequest Object to apply holiday.
     * @param holidayRequest HolidayRequest Object to apply a holiday for
     * @return ProcessInstanceResponse Object including the processId for the Request and the information whether the process is already finished.
     */
    @PostMapping("/holiday/apply")
    public ProcessInstanceResponse applyHoliday(@RequestBody HolidayRequest holidayRequest) {
        holidayService.deployProcessDefinition();
        return holidayService.applyHoliday(holidayRequest);
    }
    /**
     * Returns all tasks which are currently available to the employee candidate group.
     * @return A list of tasks including, the taskId, taskName and the relevant Holiday Request parameters.
     */
    @GetMapping("/employee/tasks")
    public List<Details> getEmployeeTasks() {
        return holidayService.getEmployeeTasks();
    }
    /**
     * Adds information whether a substitute is included in the holiday request or not.
     * Therefore, the taskId of the task has to be given and the information whether a substitute is included has to be given as a boolean too.
     * Both has to be included as a path-variable.
     * @param taskId ID of affected task
     * @param withSubstitute information whether a substitute is included has to be given as a boolean too.
     */
    @PostMapping("/employee/addSubstitute/tasks/{taskId}/{withSubstitute}")
    public void addSubstitute(@PathVariable("taskId") String taskId, @PathVariable("withSubstitute") Boolean withSubstitute) {
        holidayService.addSubstitute(taskId, withSubstitute);
    }
    /**
     * Returns all tasks which are currently available to the substitute candidate group.
     * @return A list of tasks including, the taskId, taskName and the relevant Holiday Request parameters.
     */
    @GetMapping("/substitute/tasks")
    public List<Details> getSubstituteTasks() {
        return holidayService.getSubstituteTasks();
    }
    /**
     * Adds information whether the substitute has approved the substitution for the holiday request or not.
     * Therefore, the taskId of the task has to be given and the information whether the substitute has approved the substitution or not has to be given as a boolean too.
     * Both has to be included as a path-variable.
     * @param taskId ID of affected task
     * @param approveSubstitution information whether the substitute has approved the substitution or not
     */
    @PostMapping("/substitute/approve/tasks/{taskId}/{approveSubstitution}")
    public void approveSubstituteTask(@PathVariable("taskId") String taskId, @PathVariable("approveSubstitution") Boolean approveSubstitution) {
        holidayService.approveSubstituteTask(taskId, approveSubstitution);
    }

    /**
     * Returns all tasks which are currently available to the superior candidate group.
     * @return A list of tasks including, the taskId, taskName and the relevant Holiday Request parameters.
     */
    @GetMapping("/superior/tasks")
    public List<Details> getSuperiorTasks() {
        return holidayService.getSuperiorTasks();
    }
    /**
     * Adds information whether the superior has approved the holiday request or not.
     * Therefore, the taskId of the task has to be given and the information whether the superior has approved the holiday request or not has to be given as a boolean too.
     * @param taskId ID of affected task
     * @param approve the superior has approved the holiday request or not
     */
    @PostMapping("/superior/approve/tasks/{taskId}/{approve}")
    public void approveTask(@PathVariable("taskId") String taskId, @PathVariable("approve") Boolean approve) {
        holidayService.approveHoliday(taskId, approve);
    }
    /**
     * Returns all holiday requests which are currently available to the HR-department.
     * @return A list of tasks including, the taskId, taskName and the relevant Holiday Request parameters.
     */
    @GetMapping("/employee/holidays/approved")
    public List<Details> getApprovedHolidayRequests() {
        return holidayService.fetchEmployeeHolidayRequests();
    }
    /**
     * Returns all approved holiday requests which are currently available to the employee.
     * @return A list of tasks including, the taskId, taskName and the relevant Holiday Request parameters.
     */
    @GetMapping("/department/hr/holidayrequest/closed")
    public List<Details> getClosedHolidayRequests() {
        return holidayService.fetchClosedHolidayRequests();
    }
}
