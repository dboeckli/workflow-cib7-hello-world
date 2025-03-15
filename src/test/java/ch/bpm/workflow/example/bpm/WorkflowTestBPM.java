package ch.bpm.workflow.example.bpm;

import ch.bpm.workflow.example.common.bpm.WorkflowException;
import ch.bpm.workflow.example.common.bpm.variable.token.TokenVariable;
import ch.bpm.workflow.example.util.config.TestCamundaClientConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.RuntimeService;
import org.cibseven.bpm.engine.runtime.ProcessInstance;
import org.cibseven.bpm.engine.test.Deployment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.TestSocketUtils;

import javax.sql.DataSource;
import java.util.Map;

import static ch.bpm.workflow.example.common.bpm.WorkflowConstants.*;
import static ch.bpm.workflow.example.common.bpm.variable.token.TokenStatus.*;
import static ch.bpm.workflow.example.common.bpm.variable.token.TokenVariable.TOKEN_VARIABLE_NAME;
import static java.util.Map.entry;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@TestPropertySource(properties = {
        "camunda.bpm.job-execution.enabled=false",
        "camunda.bpm.generate-unique-process-engine-name=true",
        "camunda.bpm.generate-unique-process-application-name=true",
        "spring.datasource.generate-unique-name=true"
})
@Deployment(resources = "process.bpmn")
@Slf4j
@ActiveProfiles(value = "local")
@Import(TestCamundaClientConfiguration.class)
@SuppressWarnings("java:S3577") // Suppress "Test class names should end with 'Test' or 'Tests'"
class WorkflowTestBPM {

    @LocalServerPort
    private int localServerPort;

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

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        int randomPort = TestSocketUtils.findAvailableTcpPort();
        log.info("### Reserving randomPort: " + randomPort);
        registry.add("server.port", () -> randomPort);
    }

    @Test
    void test_shouldExecuteHappyPath()  {
        // when
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, BUSINESS_KEY, Map.of(INPUT_VARIABLE_NAME, "hello-variable"));

        // then
        assertThat(processInstance).isStarted().hasBusinessKey(BUSINESS_KEY).hasVariables(INPUT_VARIABLE_NAME).variables().contains(entry(INPUT_VARIABLE_NAME, "hello-variable"));
        assertEquals("hello-variable", this.getTokenVariable(processInstance).getInput().getInputVariable());
        assertEquals(STARTED, this.getTokenVariable(processInstance).getStatus());

        // token is wating at the end of the validate input activity because of the Asynchronous continuations After flag
        assertThat(processInstance).hasPassed("Activity_validate_input");
        assertThat(processInstance).isWaitingAt("Activity_validate_input");
        execute(job()); // push forward

        assertThat(processInstance).isWaitingAt("Service_for_Script");
        execute(job());
        assertThat(processInstance).hasPassed("Service_for_Script");
        execute(job());

        assertThat(processInstance).isWaitingAt("External_Task");
        execute(job());
        assertThat(processInstance).isWaitingAt("External_Task").externalTask().hasTopicName("sayHelloTopic");
        await().atMost(5, SECONDS)
            .pollInterval(500, MILLISECONDS)
            .until(() -> {
                TokenVariable currentTokenVariable = this.getTokenVariable(processInstance);
                log.info("Current status: {}", currentTokenVariable.getStatus());
                return currentTokenVariable.getStatus() == RUNNING;
            });
        assertThat(processInstance).hasPassed("External_Task");
        execute(job());

        assertThat(processInstance).isWaitingAt("say-hello");
        assertEquals(RUNNING, this.getTokenVariable(processInstance).getStatus());
        assertThat(processInstance).task().hasDefinitionKey("say-hello").hasCandidateUser("admin").isNotAssigned();
        claim(task(), "admin");
        assertEquals("admin", task().getAssignee());
        complete(task());
        execute(job());

        // is waiting before this activity
        assertThat(processInstance).isWaitingAt("Activity_say_hello-via_delegate");
        assertEquals(COMPLETED, this.getTokenVariable(processInstance).getStatus());
        execute(job());
        assertThat(processInstance).hasPassed("Activity_say_hello-via_delegate");
        // is waiting after this activity
        assertThat(processInstance).isWaitingAt("Activity_say_hello-via_delegate");
        assertEquals(FINISHED, this.getTokenVariable(processInstance).getStatus());
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

    private TokenVariable getTokenVariable(ProcessInstance processInstance) {
        return (TokenVariable) runtimeService.getVariable(processInstance.getId(), TOKEN_VARIABLE_NAME);
    }

}
