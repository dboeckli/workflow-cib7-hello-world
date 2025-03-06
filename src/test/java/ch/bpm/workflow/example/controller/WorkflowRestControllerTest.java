package ch.bpm.workflow.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "test")
@Slf4j
class WorkflowRestControllerTest {

    @InjectMocks
    WorkflowRestController workflowRestController;

    @Mock
    RuntimeServiceImpl runtimeServiceMock = new RuntimeServiceImpl();

    @Test
    void getInfo() {
        log.info("Test started: WorkflowRestControllerTest - getInfo() method");

        ExecutionEntity givenExecution = new ExecutionEntity();
        givenExecution.setId("id");
        givenExecution.setCaseInstanceId("caseInstanceId");
        givenExecution.setProcessInstanceId("processInstanceId");
        givenExecution.setRootProcessInstanceId("rootProcessInstanceId");
        givenExecution.setProcessDefinitionId("processDefinitionId");

        Mockito.when(runtimeServiceMock.startProcessInstanceByKey(any())).thenReturn(givenExecution);

        ResponseEntity<WorkflowRestController.HelloWorldWorklfowResponse> entity = workflowRestController.getInfo();

        assertAll(
                () -> assertEquals(givenExecution.getId(), entity.getBody().id()),
                () -> assertEquals(givenExecution.getCaseInstanceId(), entity.getBody().caseInstanceId()),
                () -> assertEquals(givenExecution.getProcessInstanceId(), entity.getBody().processInstanceId()),
                () -> assertEquals(givenExecution.getRootProcessInstanceId(), entity.getBody().rootProcessInstanceId()),
                () -> assertEquals(givenExecution.getProcessDefinitionId(), entity.getBody().processDefinitionId())
        );

    }
}
