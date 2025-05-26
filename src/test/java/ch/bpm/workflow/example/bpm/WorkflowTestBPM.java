package ch.bpm.workflow.example.bpm;

import ch.bpm.workflow.example.common.bpm.WorkflowException;
import ch.bpm.workflow.example.common.bpm.variable.token.TokenVariable;
import ch.bpm.workflow.example.util.config.TestCamundaClientConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.RuntimeService;
import org.cibseven.bpm.engine.runtime.ProcessInstance;
import org.cibseven.bpm.engine.test.Deployment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.TestSocketUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.stream.Stream;

import static ch.bpm.workflow.example.common.bpm.WorkflowConstants.*;
import static ch.bpm.workflow.example.common.bpm.variable.token.TokenStatus.*;
import static ch.bpm.workflow.example.common.bpm.variable.token.TokenVariable.TOKEN_VARIABLE_NAME;
import static ch.bpm.workflow.example.external.task.HelloTaskHandler.TOPIC_NAME;
import static java.util.Map.entry;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.cibseven.bpm.admin.impl.plugin.resources.MetricsRestService.objectMapper;
import static org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "camunda.bpm.job-execution.enabled=false",
        "camunda.bpm.generate-unique-process-engine-name=true",
        "camunda.bpm.generate-unique-process-application-name=true",
        "spring.datasource.hikari.jdbc-url=jdbc:h2:mem:WorkflowTestBPM",
        "spring.datasource.generate-unique-name=true",
        "spring.docker.compose.file=compose.yaml"
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

    @ParameterizedTest
    @MethodSource("happyPathTestParameters")
    void test_shouldExecuteHappyPath(String inputVariable, String expectedActivity, String expectedName) throws JsonProcessingException {
        // when
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, BUSINESS_KEY, Map.of(INPUT_VARIABLE_NAME, inputVariable));

        // then
        assertAll("Process instance initial state assertions",
            () -> assertThat(processInstance).isStarted()
                .hasBusinessKey(BUSINESS_KEY)
                .hasVariables(INPUT_VARIABLE_NAME)
                .variables()
                .contains(entry(INPUT_VARIABLE_NAME, inputVariable)),
            () -> assertEquals(inputVariable, this.getTokenVariable(processInstance).getInput().getInputVariable()),
            () -> assertEquals(STARTED, this.getTokenVariable(processInstance).getStatus())
        );
        // token is waiting at the end of the validate input activity because of the Asynchronous continuations After flag
        assertAll("Validate input activity assertions",
            () -> assertThat(processInstance).hasPassed("Activity_validate_input"),
            () -> assertThat(processInstance).isWaitingAt("Activity_validate_input")
        );
        execute(job()); // push forward

        assertThat(processInstance).isWaitingAt("Service_for_Script");
        execute(job());
        assertThat(processInstance).hasPassed("Service_for_Script");
        String user1 = (String)runtimeService.getVariable(processInstance.getId(), "User01");
        String user2 = (String)runtimeService.getVariable(processInstance.getId(), "User02");
        JsonNode user1Json = objectMapper.readTree(user1);
        JsonNode user2Json = objectMapper.readTree(user2);
        assertAll("User 1/2 JSON assertions",
            () -> assertEquals(1, user1Json.get("id").asInt()),
            () -> assertEquals("John Doe", user1Json.get("name").asText()),
            () -> assertEquals(2, user2Json.get("id").asInt()),
            () -> assertEquals("Jane Smith", user2Json.get("name").asText())
        );
        execute(job());

        assertThat(processInstance).isWaitingAt(expectedActivity);
        execute(job());
        assertThat(processInstance).hasPassed(expectedActivity);
        String user3 = (String)runtimeService.getVariable(processInstance.getId(), "User03");
        JsonNode user3Json = objectMapper.readTree(user3);
        assertAll("User 3 JSON assertions",
            () -> assertEquals(3, user3Json.get("id").asInt()),
            () -> assertEquals(expectedName, user3Json.get("name").asText())
        );
        execute(job());

        assertThat(processInstance).isWaitingAt("External_Task");
        execute(job());
        assertThat(processInstance).isWaitingAt("External_Task").externalTask().hasTopicName(TOPIC_NAME);
        await().atMost(5, SECONDS)
            .pollInterval(500, MILLISECONDS)
            .until(() -> {
                TokenVariable currentTokenVariable = this.getTokenVariable(processInstance);
                log.info("Current status: {}", currentTokenVariable.getStatus());
                return currentTokenVariable.getStatus() == RUNNING;
            });
        assertThat(processInstance).hasPassed("External_Task");
        execute(job());

        assertAll("Say hello task assertions",
            () -> assertThat(processInstance).isWaitingAt("say-hello"),
            () -> assertEquals(RUNNING, this.getTokenVariable(processInstance).getStatus(), "Token status should be RUNNING"),
            () -> assertThat(processInstance).task()
                .hasDefinitionKey("say-hello")
                .hasCandidateUser("admin")
                .isNotAssigned()
        );
        claim(task(), "admin");
        assertEquals("admin", task().getAssignee());
        complete(task());
        execute(job());

        // is waiting before this activity
        assertAll("Activity_say_hello-via_delegate assertions",
            () -> assertThat(processInstance).isWaitingAt("Activity_say_hello-via_delegate"),
            () -> assertEquals(COMPLETED, this.getTokenVariable(processInstance).getStatus(), "Token status should be COMPLETED")
        );
        execute(job());
        assertAll("Final Activity_say_hello-via_delegate assertions",
            () -> assertThat(processInstance).hasPassed("Activity_say_hello-via_delegate"),
            () -> assertThat(processInstance).isWaitingAt("Activity_say_hello-via_delegate"),
            () -> assertEquals(FINISHED, this.getTokenVariable(processInstance).getStatus(), "Token status should be FINISHED")
        );
        execute(job());

        assertThat(processInstance).isEnded();
    }

    static Stream<Arguments> happyPathTestParameters() {
        String generatedName = RandomStringUtils.secure().nextAlphabetic(10);
        return Stream.of(
            Arguments.of(generatedName, "Set_User03_To_Default", "Default"),
            Arguments.of("eder", "Set_User03_To_Eder", "Eder"),
            Arguments.of("pumukel", "Set_User03_To_Pumukel", "Pumukel")
        );
    }

    @Test
    void test_shouldExecuteHappyPathWithFail() throws JsonProcessingException {
        // when
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, BUSINESS_KEY, Map.of(INPUT_VARIABLE_NAME, "fail"));

        // then
        assertThat(processInstance).isStarted().hasBusinessKey(BUSINESS_KEY).hasVariables(INPUT_VARIABLE_NAME).variables().contains(entry(INPUT_VARIABLE_NAME, "fail"));
        assertEquals("fail", this.getTokenVariable(processInstance).getInput().getInputVariable());
        assertEquals(STARTED, this.getTokenVariable(processInstance).getStatus());

        // token is wating at the end of the validate input activity because of the Asynchronous continuations After flag
        assertThat(processInstance).hasPassed("Activity_validate_input");
        assertThat(processInstance).isWaitingAt("Activity_validate_input");
        execute(job()); // push forward

        assertThat(processInstance).isWaitingAt("Service_for_Script");
        execute(job());
        assertThat(processInstance).hasPassed("Service_for_Script");
        execute(job());

        assertThat(processInstance).isWaitingAt("Set_User03_To_Default");
        execute(job());
        assertThat(processInstance).hasPassed("Set_User03_To_Default");
        String user3 = (String)runtimeService.getVariable(processInstance.getId(), "User03");
        JsonNode user3Json = objectMapper.readTree(user3);
        assertAll("User 3 JSON assertions",
            () -> assertEquals(3, user3Json.get("id").asInt()),
            () -> assertEquals("Default", user3Json.get("name").asText())
        );
        execute(job());

        assertThat(processInstance).isWaitingAt("External_Task");
        execute(job());
        assertThat(processInstance).isWaitingAt("External_Task").externalTask().hasTopicName(TOPIC_NAME);
        await().atMost(10, SECONDS)
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
        // Now it fails in the delegate

        assertThat(processInstance).hasPassed("Error_in_delegate");

        // is waiting again in say-hello
        assertThat(processInstance).isWaitingAt("say-hello");
        assertEquals(BUSINESS_EXCEPTION, this.getTokenVariable(processInstance).getStatus());
        claim(task(), "admin");
        complete(task());
        execute(job());

        // is waiting again in delegate
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
