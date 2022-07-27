package com.example.camundabasic01.controller;

import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: huang
 * @Date: 2022/7/27 0:41
 */
@RestController
@RequestMapping("/camunda")
public class CamundaController {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private HistoryService historyService;

    /**
     * 部署流程实例
     */
    @PostMapping("/deploy")
    public void deploy() {
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("BPMN/apply.bpmn")
                .name("camunda-demo申请流程")
                .deploy();
        System.out.println(deploy.getId());
    }

    /**
     * 启动流程实例
     */
    @PostMapping("/start")
    public void runProcinst() {
        Map<String, Object> params = new HashMap<>();
        params.put("money", 2001);
        ProcessInstance apply = runtimeService.startProcessInstanceByKey("apply", params);
        System.out.println(apply.getProcessDefinitionId());
        System.out.println(apply.getId());
        System.out.println(apply.getProcessInstanceId());
    }

    /**
     * 流程任务查询
     */
    @PostMapping("/taskquery")
    public void taskQuery() {
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey("apply")
                .list();
        for (Task task : tasks) {
            System.out.println(task.getAssignee());
            System.out.println(task.getId());
            System.out.println(task.getName());
            System.out.println(task.getTenantId());
        }
    }

    @PostMapping("/mytaskquery")
    public List<HistoricTaskInstance> myTaskQuery() {
        List<HistoricTaskInstance> instances = processEngine.getHistoryService().createHistoricTaskInstanceQuery()
                .taskAssignee("lisi").unfinished()
                .orderByHistoricActivityInstanceStartTime()
                .asc().list();
        return instances;
    }

    @PostMapping("/taskComplete")
    public void taskComplete() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("zhangsan")
                .singleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("approve2", "lisi");
        taskService.complete(task.getId(), params);
    }

    /**
     * 流程定义查询
     */
    @PostMapping("/queryDefine")
    public void queryDefine() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> definitions = query.processDefinitionKey("apply")
                .orderByProcessDefinitionId()
                .desc()
                .list();
        for (ProcessDefinition definition : definitions) {
            System.out.println(definition.getDeploymentId());
            System.out.println(definition.getName());
            System.out.println(definition.getVersion());
            System.out.println(definition.getId());
            System.out.println(definition.getKey());
        }
    }

    /**
     * 删除流程定义
     */
    @DeleteMapping("/deleteDefine")
    public void deleteDefine() {
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> definitions = processDefinitionQuery.processDefinitionKey("apply")
                .orderByProcessDefinitionVersion()
                .asc()
                .list();
        ProcessDefinition processDefinition = definitions.get(0);
        if (processDefinition != null) {
            repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
        }
    }

    /**
     * 查询历史信息
     */
    @PostMapping("/queryHistory")
    public void queryHistory() {
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        for (HistoricActivityInstance instance : list) {
            System.out.println(instance.getActivityId());
            System.out.println(instance.getProcessDefinitionKey());
            System.out.println(instance.getAssignee());
            System.out.println(instance.getStartTime());
            System.out.println(instance.getEndTime());
            System.out.println("=============================");
        }
    }

    /**
     * 启动一个流程实例，并且添加一个业务key
     * 业务key 可以在 act_ru_execution 中看到
     */

    public void startProcInstAddBusinessKey() {
        ProcessInstance apply = runtimeService.startProcessInstanceByKey("apply", "aaaa-scsc-89uc");
        System.out.println(apply.getBusinessKey());
    }
}
