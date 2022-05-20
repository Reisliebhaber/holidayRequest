package com.workflow.holidayRequest.service;

import com.workflow.holidayRequest.dto.HolidayRequest;
import com.workflow.holidayRequest.dto.ProcessInstanceResponse;
import com.workflow.holidayRequest.dto.TaskDetails;
import com.workflow.oldDto.OldTaskDetails;
import liquibase.pro.packaged.T;
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
    public static final String PROCESS_DEFINITION_KEY = "holidayRequestWorkflow";
    public static final String TASK_CANDIDATE_GROUP_SUPERIOR = "superior";
    public static final String TASK_CANDIDATE_GROUP_SUBSTITUTE = "substitute";
    public static final String TASK_CANDIDATE_GROUP_EMPLOYEE = "employee";
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
                        .addClasspathResource("processes/holiday-request.bpmn20.xml")
                        .deploy();


    }


    //********************************************************** process service methods **********************************************************

    public ProcessInstanceResponse applyHoliday(HolidayRequest holidayRequest) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", holidayRequest.getEmpName());
        variables.put("noOfHolidays", holidayRequest.getNoOfHolidays());
        variables.put("description", holidayRequest.getRequestDescription());
        variables.put("startingDate", holidayRequest.getStartingDate());

        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
        Task holidayRequestTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(holidayRequestTask.getId(), variables);
        ProcessInstanceResponse response = new ProcessInstanceResponse(processInstance.getId(), processInstance.isEnded());
        //taskService.complete(taskId);
        return response;
    }

    public List<TaskDetails> getEmployeeTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP_EMPLOYEE).list();
        List<TaskDetails> taskDetails = getTaskDetails(tasks);

        return taskDetails;
    }

    public void addSubstitute(String taskId, Boolean approved) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("withSubstitute", approved.booleanValue());
        taskService.complete(taskId, variables);
    }

    public List<TaskDetails> getSubstituteTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP_SUBSTITUTE).list();
        List<TaskDetails> taskDetails = getTaskDetails(tasks);

        return taskDetails;
    }

    public void approveSubstituteTask(String taskId, Boolean approved) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approveSubstitution", approved.booleanValue());
        taskService.complete(taskId, variables);
    }

    public List<TaskDetails> getManagerTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP_SUPERIOR).list();
        List<TaskDetails> taskDetails = getTaskDetails(tasks);

        return taskDetails;
    }

    private List<TaskDetails> getTaskDetails(List<Task> tasks) {
        List<TaskDetails> taskDetails = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> processVariables = taskService.getVariables(task.getId());
            taskDetails.add(new TaskDetails(task.getId(), task.getName(), processVariables));
        }
        return taskDetails;
    }


    public void approveHoliday(String taskId, Boolean approved) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("withSubstitute", approved.booleanValue());//TODO change variable
        taskService.complete(taskId, variables);
        /*Map<String, Object> testingVariables = taskService.getVariables(taskId);
        System.out.println("Short test MAXIMILIAN");

        Interesting Code examples:
        test = taskService.createTaskQuery().taskId(taskId).singleResult();
        Collection<String> currentVariables = taskService.getVariablesLocal((test.getId())).keySet();*/
    }

    public void acceptHoliday(String taskId) {
        taskService.complete(taskId);
    }


    public List<TaskDetails> getUserTasks() {

        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(EMP_NAME).list();
        List<TaskDetails> taskDetails = getTaskDetails(tasks);

        return taskDetails;
    }

}
