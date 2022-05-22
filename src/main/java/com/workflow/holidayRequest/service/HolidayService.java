package com.workflow.holidayRequest.service;

import com.workflow.holidayRequest.dto.HolidayRequest;
import com.workflow.holidayRequest.dto.ProcessInstanceResponse;
import com.workflow.holidayRequest.dto.Details;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class HolidayService {
    public static final String PROCESS_DEFINITION_KEY = "holidayRequestWorkflow";
    public static final String TASK_CANDIDATE_GROUP_SUPERIOR = "superior";
    public static final String TASK_CANDIDATE_GROUP_SUBSTITUTE = "substitute";
    public static final String TASK_CANDIDATE_GROUP_EMPLOYEE = "employee";
    public static final String TASK_CANDIDATE_GROUP_APPROVED = "approvedHolidayRequests";
    public static final String EMP_NAME = "empName";
    public static final String ACT_TYPE_END_EVENT = "endEvent";// HistoryService
    public static final String ACT_ID_NOTIFY_EMPLOYEE_END = "notifyEmployeeEnd";// HistoryService

    //********************************************************** **********************************************************
    RuntimeService runtimeService;
    TaskService taskService;
    ProcessEngine processEngine;
    RepositoryService repositoryService;
    HistoryService historyService;


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
        return response;
    }

    public List<Details> getEmployeeTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP_EMPLOYEE).list();
        List<Details> taskDetails = getTaskDetails(tasks);

        return taskDetails;
    }

    public void addSubstitute(String taskId, Boolean approved) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("withSubstitute", approved.booleanValue());
        taskService.complete(taskId, variables);
    }

    public List<Details> getSubstituteTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP_SUBSTITUTE).list();
        List<Details> taskDetails = getTaskDetails(tasks);

        return taskDetails;
    }

    public void approveSubstituteTask(String taskId, Boolean approved) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approveSubstitution", approved.booleanValue());
        taskService.complete(taskId, variables);
    }

    public List<Details> getSuperiorTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP_SUPERIOR).list();
        List<Details> taskDetails = getTaskDetails(tasks);

        return taskDetails;
    }
    private List<Details> getTaskDetails(List<Task> tasks) {
        List<Details> taskDetails = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> processVariables = taskService.getVariables(task.getId());
            taskDetails.add(new Details(task.getId(), task.getName(), processVariables));
        }
        return taskDetails;
    }


    public void approveHoliday(String taskId, Boolean approve) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approve", approve.booleanValue());
        taskService.complete(taskId, variables);
    }

    public void acceptHoliday(String taskId) {
        taskService.complete(taskId);
    }


    public List<Details> getUserTasks() {

        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(EMP_NAME).list();
        List<Details> taskDetails = getTaskDetails(tasks);

        return taskDetails;
    }

    /**
     * TODO remove
     *
     * @param processId
     */
    public void checkProcessHistory(String processId) {

        HistoryService historyService = processEngine.getHistoryService();
        List<Details> closedHolidayRequests = fetchClosedHolidayRequests();
        List<Details> employeeHolidayRequests = fetchEmployeeHolidayRequests();

        for (HistoricActivityInstance activity : activities) {
            System.out.println(activity.getActivityId() + " took " + activity.getDurationInMillis() + " milliseconds");
        }

        System.out.println("\n \n \n \n");
    }

    public List<Details> fetchClosedHolidayRequests() {
        List<String> processInstanceIds = historyService
                .createHistoricActivityInstanceQuery()
                .finished()
                .activityType(ACT_TYPE_END_EVENT)
                .list()
                .stream()
                .map(HistoricActivityInstance::getProcessInstanceId)
                .distinct()
                .collect(Collectors.toList());
        return fetchHistoricActivityInstanceDetails(processInstanceIds);
    }

    public List<Details> fetchEmployeeHolidayRequests() {
        List<String> processInstanceIds = historyService.createHistoricActivityInstanceQuery()
                .finished()
                .activityId(ACT_ID_NOTIFY_EMPLOYEE_END)
                .activityType(ACT_TYPE_END_EVENT)
                .list().stream()
                .map(HistoricActivityInstance::getProcessInstanceId)
                .distinct()
                .collect(Collectors.toList());
        return fetchHistoricActivityInstanceDetails(processInstanceIds);
    }

    private List<Details> fetchHistoricActivityInstanceDetails(List<String> processInstanceIds) {
        List<Details> activityDetails = new ArrayList<>();
        String detailName;
        boolean holidayApproved = false;
        for (String processInstanceId : processInstanceIds) {
            Map<String, Object> processVariables = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, HistoricVariableInstance::getValue));
            if (processVariables.containsKey("approve")) {
                if (processVariables.get("approve") instanceof Boolean) {
                    holidayApproved = (boolean) processVariables.get("approve");
                }
            }
            detailName = (holidayApproved) ? "Holiday Request approved" : "Holiday Request rejected";
            activityDetails.add(new Details(processInstanceId, detailName, processVariables));
        }
        return activityDetails;
    }
}

