package com.workflow.holidayRequest.service;

import com.workflow.oldDto.OldHolidayRequest;
import com.workflow.oldDto.OldProcessInstanceResponse;
import com.workflow.oldDto.OldTaskDetails;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class HolidayService {
    public static final String TASK_CANDIDATE_GROUP = "managers";
    public static final String PROCESS_DEFINITION_KEY = "holidayRequestWorkflow";
    public static final String EMP_NAME = "empName";

    RuntimeService runtimeService;
    TaskService taskService;
    ProcessEngine processEngine;
    RepositoryService repositoryService;


    //********************************************************** deployment service methods **********************************************************

    public void deployProcessDefinition() {

        Deployment deployment =
                repositoryService
                        .createDeployment()
                        .addClasspathResource("processes/holiday-request.bpmn20.xml")//TODO richtiges verzeichnis?
                        .deploy();


    }


    //********************************************************** process service methods **********************************************************

    public OldProcessInstanceResponse applyHoliday(OldHolidayRequest oldHolidayRequest) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", oldHolidayRequest.getEmpName());
        variables.put("noOfHolidays", oldHolidayRequest.getNoOfHolidays());
        variables.put("description", oldHolidayRequest.getRequestDescription());

        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);

        return new OldProcessInstanceResponse(processInstance.getId(), processInstance.isEnded());
    }


    public List<OldTaskDetails> getManagerTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP).list();
        List<OldTaskDetails> oldTaskDetails = getTaskDetails(tasks);

        return oldTaskDetails;
    }

    private List<OldTaskDetails> getTaskDetails(List<Task> tasks) {
        List<OldTaskDetails> oldTaskDetails = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> processVariables = taskService.getVariables(task.getId());
            oldTaskDetails.add(new OldTaskDetails(task.getId(), task.getName(), processVariables));
        }
        return oldTaskDetails;
    }


    public void approveHoliday(String taskId, Boolean approved) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approved", approved.booleanValue());
        taskService.complete(taskId, variables);
    }

    public void acceptHoliday(String taskId) {
        taskService.complete(taskId);
    }


    public List<OldTaskDetails> getUserTasks() {

        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(EMP_NAME).list();
        List<OldTaskDetails> taskDetails = getTaskDetails(tasks);

        return taskDetails;
    }

}
