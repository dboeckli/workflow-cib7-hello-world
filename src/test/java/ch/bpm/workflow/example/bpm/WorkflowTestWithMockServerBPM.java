package ch.bpm.workflow.example.bpm;

import ch.bpm.workflow.example.config.RestApiConfiguration;
import ch.guru.springframework.apifirst.client.CustomerApi;
import ch.guru.springframework.apifirst.model.AddressDto;
import ch.guru.springframework.apifirst.model.CustomerDto;
import ch.guru.springframework.apifirst.model.NameDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
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

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@TestPropertySource(properties = {
        //"spring.datasource.hikari.jdbc-url=jdbc:h2:mem:WorkflowTestWithMockServerBPM;DB_CLOSE_ON_EXIT=FALSE",
        //"spring.datasource.hikari.pool-name=WorkflowTestWithMockServerBPM",
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
        log.info("### ProcessEngine started: {}", processEngine.getName());
        mockServerClient = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
        customerApi.getApiClient()
                  .setBasePath(restApiConfiguration.getProtocol() + "://" +
                          mockServer.getHost() + ":" +
                          mockServer.getServerPort() + "/" +
                          restApiConfiguration.getContext());
    }

    @AfterEach
    void tearDown() {
        log.info("DataSource after test: {}", dataSource);
    }

    @Test
    void shouldExecuteHappyPath() {
        createExpectedMockserverResponse();

        // given
        String processDefinitionKey = "hello-world-process";

        // when
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey);

        // then
        assertThat(processInstance).isStarted().task().hasDefinitionKey("say-hello").hasCandidateUser("admin").isNotAssigned();
        assertThat(processInstance).isWaitingAt("say-hello");

        complete(task());

        assertThat(processInstance).hasPassed("Activity_say_hello-via_delegate");
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

}
