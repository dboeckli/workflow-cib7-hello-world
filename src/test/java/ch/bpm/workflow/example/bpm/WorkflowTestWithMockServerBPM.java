package ch.bpm.workflow.example.bpm;

import ch.bpm.workflow.example.common.bpm.variable.token.TokenVariable;
import ch.bpm.workflow.example.config.RestApiConfiguration;
import ch.bpm.workflow.example.util.config.TestCamundaClientConfiguration;
import ch.guru.springframework.apifirst.client.CustomerApi;
import ch.guru.springframework.apifirst.model.AddressDto;
import ch.guru.springframework.apifirst.model.CustomerDto;
import ch.guru.springframework.apifirst.model.NameDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.RuntimeService;
import org.cibseven.bpm.engine.runtime.ProcessInstance;
import org.cibseven.bpm.engine.test.Deployment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockserver.client.MockServerClient;
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
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static ch.bpm.workflow.example.common.bpm.WorkflowConstants.*;
import static ch.bpm.workflow.example.common.bpm.variable.token.TokenStatus.*;
import static ch.bpm.workflow.example.common.bpm.variable.token.TokenVariable.TOKEN_VARIABLE_NAME;
import static java.util.Map.entry;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@TestPropertySource(properties = {
    "camunda.bpm.job-execution.enabled=false",
    "camunda.bpm.generate-unique-process-engine-name=true",
    "camunda.bpm.generate-unique-process-application-name=true",
    "spring.datasource.generate-unique-name=true"
})
@Testcontainers
@Deployment(resources = "process.bpmn")
@Slf4j
@ActiveProfiles(value = "local")
@Import(TestCamundaClientConfiguration.class)
@SuppressWarnings("java:S3577") // Suppress "Test class names should end with 'Test' or 'Tests'"
class WorkflowTestWithMockServerBPM {

    @LocalServerPort
    private int localServerPort;

    @Autowired
    public RuntimeService runtimeService;

    // See https://docs.camunda.org/manual/latest/user-guide/spring-boot-integration/develop-and-test/#using-assertions-with-context-caching
    @Autowired
    ProcessEngine processEngine;

    @Autowired
    private DataSource dataSource;

    // this version should correspond to the client version defined int the pom.xml
    @Container
    public MockServerContainer mockServer = new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.15.0"));

    @Autowired
    CustomerApi customerApi;

    @Autowired
    RestApiConfiguration restApiConfiguration;

    @Autowired
    ObjectMapper objectMapper;

    private MockServerClient mockServerClient;


    @BeforeEach
    void setup() {
        init(processEngine);
        log.info("### ProcessEngine started: {} with datasource {}", processEngine.getName(), dataSource);

        mockServerClient = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
        customerApi.getApiClient()
                  .setBasePath(restApiConfiguration.getProtocol() + "://" +
                          mockServer.getHost() + ":" +
                          mockServer.getServerPort() + "/" +
                          restApiConfiguration.getContext());
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
    void shouldExecuteHappyPath(String inputVariable, String expectedActivity, String expectedName) throws JsonProcessingException {
        createExpectedMockserverResponse();

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
        assertThat(processInstance).isWaitingAt("External_Task").externalTask().hasTopicName("sayHelloTopic");
        await().atMost(15, SECONDS)
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

    private void createExpectedMockserverResponse() {
        List<CustomerDto> customers = new ArrayList<>();
        CustomerDto customer1 = CustomerDto.builder()
                .id(UUID.randomUUID())
                .name(NameDto.builder().firstName("John").lastName("Doe").build())
                .shipToAddress(AddressDto.builder().addressLine1("123 Main St").city("New York").city("NY").state("NY").zip("10001").build())
                .billToAddress(AddressDto.builder().addressLine1("456 Elm St").city("Los Angeles").city("CA").state("CA").zip("90001").build())
                .build();
        customers.add(customer1);

        String customersJson;
        try {
            customersJson = objectMapper.writeValueAsString(customers);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert customers to JSON", e);
        }
        mockServerClient
                .when(request().withMethod("GET").withPath("/v1/customers"))
                .respond(response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(customersJson));
    }

    private TokenVariable getTokenVariable(ProcessInstance processInstance) {
        return (TokenVariable) runtimeService.getVariable(processInstance.getId(), TOKEN_VARIABLE_NAME);
    }

}
