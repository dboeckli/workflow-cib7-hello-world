package ch.bpm.workflow.example.bpm;

import ch.bpm.workflow.example.common.bpm.token.TokenVariable;
import ch.bpm.workflow.example.config.RestApiConfiguration;
import ch.guru.springframework.apifirst.client.CustomerApi;
import ch.guru.springframework.apifirst.model.AddressDto;
import ch.guru.springframework.apifirst.model.CustomerDto;
import ch.guru.springframework.apifirst.model.NameDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.RuntimeService;
import org.cibseven.bpm.engine.runtime.ProcessInstance;
import org.cibseven.bpm.engine.test.Deployment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ch.bpm.workflow.example.common.bpm.WorkflowConstants.*;
import static ch.bpm.workflow.example.common.bpm.token.TokenVariable.TOKEN_VARIABLE_NAME;
import static java.util.Map.entry;
import static org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
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
class WorkflowTestWithMockServerBPM {

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

    @Test
    void shouldExecuteHappyPath() {
        createExpectedMockserverResponse();

        // when
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, BUSINESS_KEY, Map.of(INPUT_VARIABLE_NAME, "hello-variable"));

        // then
        assertThat(processInstance).isStarted().hasBusinessKey(BUSINESS_KEY).hasVariables(INPUT_VARIABLE_NAME).variables().contains(entry(INPUT_VARIABLE_NAME, "hello-variable"));
        assertEquals("hello-variable", this.getTokenVariable(processInstance).getInput().inputVariable());
        assertEquals(TokenVariable.STATUS.STARTED, this.getTokenVariable(processInstance).getStatus());

        // token is wating at the end of the validate input activity because of the Asynchronous continuations After flag
        assertThat(processInstance).hasPassed("Activity_validate_input");
        assertThat(processInstance).isWaitingAt("Activity_validate_input");
        execute(job()); // push forwward

        assertThat(processInstance).hasPassed("Service_for_Script");

        assertThat(processInstance).isWaitingAt("say-hello");
        assertEquals(TokenVariable.STATUS.STARTED, this.getTokenVariable(processInstance).getStatus());
        assertThat(processInstance).task().hasDefinitionKey("say-hello").hasCandidateUser("admin").isNotAssigned();
        claim(task(), "admin");
        assertEquals("admin", task().getAssignee());
        complete(task());
        execute(job());

        // is waiting before this activity
        assertThat(processInstance).isWaitingAt("Activity_say_hello-via_delegate");
        assertEquals(TokenVariable.STATUS.COMPLETED, this.getTokenVariable(processInstance).getStatus());
        execute(job());
        assertThat(processInstance).hasPassed("Activity_say_hello-via_delegate");
        // is waiting after this activity
        assertThat(processInstance).isWaitingAt("Activity_say_hello-via_delegate");
        assertEquals(TokenVariable.STATUS.FINISHED, this.getTokenVariable(processInstance).getStatus());
        execute(job());

        assertThat(processInstance).isEnded();
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
