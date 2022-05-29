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

/**
 *
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class HolidayService {

    //********************************************************** Constants **********************************************************
    public static final String PROCESS_DEFINITION_KEY = "holidayRequestWorkflow";
    public static final String TASK_CANDIDATE_GROUP_SUPERIOR = "superior";
    public static final String TASK_CANDIDATE_GROUP_SUBSTITUTE = "substitute";
    public static final String TASK_CANDIDATE_GROUP_EMPLOYEE = "employee";
    public static final String ACT_TYPE_END_EVENT = "endEvent";// HistoryService
    public static final String ACT_ID_NOTIFY_EMPLOYEE_END = "notifyEmployeeEnd";// HistoryService

    //********************************************************** Flowable Services **********************************************************
    RuntimeService runtimeService;
    TaskService taskService;
    RepositoryService repositoryService;
    HistoryService historyService;

    //********************************************************** deployment service methods **********************************************************

    /**
     *
     */
    public void deployProcessDefinition() {
        Deployment deployment =
                repositoryService
                        .createDeployment()
                        .addClasspathResource("processes/holidayRequest.bpmn20.xml")
                        .deploy();
    }


    //********************************************************** process service methods **********************************************************

    /**
     *
     * @param holidayRequest
     * @return
     */
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
        return new ProcessInstanceResponse(processInstance.getId(), processInstance.isEnded());
    }

    /**
     *
     * @return
     */
    public List<Details> getEmployeeTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP_EMPLOYEE).list();
        return getTaskDetails(tasks);
    }

    /**
     *
     * @param taskId
     * @param approved
     */
    public void addSubstitute(String taskId, Boolean approved) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("withSubstitute", approved);
        taskService.complete(taskId, variables);
    }

    /**
     *
     * @return
     */
    public List<Details> getSubstituteTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP_SUBSTITUTE).list();
        return getTaskDetails(tasks);
    }

    /**
     *
     * @param taskId
     * @param approved
     */
    public void approveSubstituteTask(String taskId, Boolean approved) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approveSubstitution", approved);
        taskService.complete(taskId, variables);
    }

    /**
     *
     * @return
     */
    public List<Details> getSuperiorTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP_SUPERIOR).list();

        return getTaskDetails(tasks);
    }

    /**
     *
     * @param tasks
     * @return
     */
    private List<Details> getTaskDetails(List<Task> tasks) {
        List<Details> taskDetails = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> variables = taskService.getVariables(task.getId());
            taskDetails.add(new Details(task.getId(), task.getName(), variables));
        }
        return taskDetails;
    }
    /**
     *
     * @param taskId
     * @param approve
     */
    public void approveHoliday(String taskId, Boolean approve) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approve", approve);
        taskService.complete(taskId, variables);
    }

    /**
     *
     * @param taskId
     */
    public void acceptHoliday(String taskId) {
        taskService.complete(taskId);
    }

    /**
     *
     * @return
     */
    public List<Details> fetchClosedHolidayRequests() {
        List<String> processInstanceIds = historyService
                .createHistoricActivityInstanceQuery()
                .finished()
                .list()
                .stream()
                .map(HistoricActivityInstance::getProcessInstanceId)
                .distinct()
                .collect(Collectors.toList());
        return fetchHistoricActivityInstanceDetails(processInstanceIds);
    }

    /**
     *
     * @return
     */
    public List<Details> fetchEmployeeHolidayRequests() {
        List<String> processInstanceIds = historyService.createHistoricActivityInstanceQuery()
                .finished()
                .activityId(ACT_ID_NOTIFY_EMPLOYEE_END)
                .list().stream()
                .map(HistoricActivityInstance::getProcessInstanceId)
                .distinct()
                .collect(Collectors.toList());
        return fetchHistoricActivityInstanceDetails(processInstanceIds);
    }

    /**
     *
     * @param processInstanceIds
     * @return
     */
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
