package ch.bpm.workflow.example.bpm;

import javax.sql.DataSource;

import ch.bpm.workflow.example.common.bpm.WorkflowException;
import ch.bpm.workflow.example.common.bpm.token.TokenVariable;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.RuntimeService;
import org.cibseven.bpm.engine.runtime.ProcessInstance;
import org.cibseven.bpm.engine.test.Deployment;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static ch.bpm.workflow.example.common.bpm.WorkflowConstants.*;
import static ch.bpm.workflow.example.common.bpm.token.TokenVariable.TOKEN_VARIABLE_NAME;
import static java.util.Map.entry;
import static org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@TestPropertySource(properties = {
        "camunda.bpm.job-execution.enabled=false",
        "camunda.bpm.generate-unique-process-engine-name=true",
        "camunda.bpm.generate-unique-process-application-name=true",
        "spring.datasource.generate-unique-name=true"
})
@Deployment(resources = "process.bpmn")
@Slf4j
@ActiveProfiles(value = "local")
class WorkflowTestBPM {

    @Autowired
    public RuntimeService runtimeService;

    // See https://docs.camunda.org/manual/latest/user-guide/spring-boot-integration/develop-and-test/#using-assertions-with-context-caching
    @Autowired
    ProcessEngine processEngine;

    @Autowired
    private DataSource dataSource;


    @BeforeEach
    void setUp() {
        init(processEngine);
        log.info("### ProcessEngine started: {} with datasource {}", processEngine.getName(), dataSource);
    }

    @AfterEach
    void tearDown() {
        reset();
        log.info("### ProcessEngine ended: {} with datasource {}", processEngine.getName(), dataSource);
    }

    @Test
    void test_shouldExecuteHappyPath() {
        // when
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, BUSINESS_KEY, Map.of(INPUT_VARIABLE_NAME, "hello-variable"));

        // then
        assertThat(processInstance).isStarted().hasBusinessKey(BUSINESS_KEY).hasVariables(INPUT_VARIABLE_NAME).variables().contains(entry(INPUT_VARIABLE_NAME, "hello-variable"));
        TokenVariable tokenVariable = (TokenVariable) runtimeService.getVariable(processInstance.getId(), TOKEN_VARIABLE_NAME);
        assertEquals("hello-variable", tokenVariable.getInput().inputVariable());
        assertEquals("STARTED", tokenVariable.getStatus());

        // token is wating at the end of the validate input activity because of the Asynchronous continuations After flag
        assertThat(processInstance).hasPassed("Activity_validate_input");
        assertThat(processInstance).isWaitingAt("Activity_validate_input");
        execute(job()); // push forwward

        assertThat(processInstance).isWaitingAt("say-hello");
        assertThat(processInstance).task().hasDefinitionKey("say-hello").hasCandidateUser("admin").isNotAssigned();

        complete(task());

        assertThat(processInstance).hasPassed("Activity_say_hello-via_delegate");
        assertThat(processInstance).isWaitingAt("Activity_say_hello-via_delegate");
        tokenVariable = (TokenVariable) runtimeService.getVariable(processInstance.getId(), TOKEN_VARIABLE_NAME);
        assertEquals("hello-variable", tokenVariable.getInput().inputVariable());
        assertEquals("DONE", tokenVariable.getStatus());
        execute(job());

        assertThat(processInstance).isEnded();
    }

    @Test
    void test_invalidInput_shoudFail() {
        // when
        WorkflowException thrown = Assertions.assertThrows(WorkflowException.class, () -> runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, BUSINESS_KEY));

        Assertions.assertEquals("Variable " + INPUT_VARIABLE_NAME + " not found or empty", thrown.getMessage());
    }

    @Test
    void test_emptyInput_shoudFail() {
        // when
        WorkflowException thrown = Assertions.assertThrows(WorkflowException.class, () -> runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, BUSINESS_KEY, Map.of(INPUT_VARIABLE_NAME, "")));

        Assertions.assertEquals("Variable " + INPUT_VARIABLE_NAME + " not found or empty", thrown.getMessage());
    }

}
