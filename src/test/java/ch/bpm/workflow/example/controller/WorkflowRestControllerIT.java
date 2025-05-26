package ch.bpm.workflow.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@TestPropertySource(properties = {
    "camunda.bpm.job-execution.enabled=false",
    "camunda.bpm.client.disable-auto-fetching=true",
    "spring.docker.compose.file=compose-it.yaml",
    "spring.docker.compose.stop.command=stop"
})
@AutoConfigureMockMvc
@ActiveProfiles(value = "local")
@Slf4j
class WorkflowRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void startProcessWithEmptyInput() throws Exception {
        WorkflowRestController.InfoRequest infoRequest = new WorkflowRestController.InfoRequest("");
        String jsonRequest = objectMapper.writeValueAsString(infoRequest);

        this.mockMvc
            .perform(post("/restapi/workflow")
                .with(httpBasic("camunda-admin", "camunda-admin-password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andDo(result -> log.info("### Response: {}", result.getResponse().getContentAsString())).andDo(print())
            //.andExpect(MockMvcResultMatchers.status().isBadRequest()) // This test will fail as it expects a 400 status code
            .andExpect(result -> {
                Exception resolvedException = result.getResolvedException();
                assertNotNull(resolvedException);
                assertInstanceOf(ResponseStatusException.class, resolvedException);

                ResponseStatusException responseStatusException = (ResponseStatusException) resolvedException;
                assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatusCode());
                assertEquals("Variable input not found or empty", responseStatusException.getReason());
            });
    }

    @Test
    void startProcessWithBadCredentials() throws Exception {
        WorkflowRestController.InfoRequest infoRequest = new WorkflowRestController.InfoRequest("Test Input");
        String jsonRequest = objectMapper.writeValueAsString(infoRequest);

        MvcResult result = this.mockMvc
            .perform(post("/restapi/workflow")
                .with(httpBasic("camunda-admin", "camunda-admin-password1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
            )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized()).andReturn();
        log.info("Response: {}", result.getResponse().getContentAsString());
    }

    @Test
    void startProcessWithUser02WithoutPermission() throws Exception {
        WorkflowRestController.InfoRequest infoRequest = new WorkflowRestController.InfoRequest("Test Input");
        String jsonRequest = objectMapper.writeValueAsString(infoRequest);

        this.mockMvc
            .perform(post("/restapi/workflow")
                .with(httpBasic("user02", "user02-password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
            )
            .andDo(result -> log.info("### Response: {}", result.getResponse().getContentAsString())).andDo(print())
            .andExpect(result -> {
                Exception resolvedException = result.getResolvedException();
                assertNotNull(resolvedException);
                assertInstanceOf(ResponseStatusException.class, resolvedException);

                ResponseStatusException responseStatusException = (ResponseStatusException) resolvedException;
                assertEquals(HttpStatus.FORBIDDEN, responseStatusException.getStatusCode());
                assertEquals("The user with id 'user02' does not have 'CREATE' permission on resource 'ProcessInstance'.", responseStatusException.getReason());
            });
    }

    @Test
    void startProcessWithUser01() throws Exception {
        WorkflowRestController.InfoRequest infoRequest = new WorkflowRestController.InfoRequest("Test Input");
        String jsonRequest = objectMapper.writeValueAsString(infoRequest);

        MvcResult result = this.mockMvc
            .perform(post("/restapi/workflow")
                .with(httpBasic("camunda-admin", "camunda-admin-password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.caseInstanceId").value(nullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.processDefinitionId").value(matchesPattern("hello-world-process:1:[a-f0-9-]+")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.rootProcessInstanceId").value(matchesPattern("[a-f0-9-]+")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(matchesPattern("[a-f0-9-]+")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.processInstanceId").value(matchesPattern("[a-f0-9-]+")))
            .andReturn();
        log.info("Response: {}", result.getResponse().getContentAsString());
    }

    @Test
    void startProcess() throws Exception {
        WorkflowRestController.InfoRequest infoRequest = new WorkflowRestController.InfoRequest("Test Input");
        String jsonRequest = objectMapper.writeValueAsString(infoRequest);

        MvcResult result = this.mockMvc
            .perform(post("/restapi/workflow")
                .with(httpBasic("camunda-admin", "camunda-admin-password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.caseInstanceId").value(nullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.processDefinitionId").value(matchesPattern("hello-world-process:1:[a-f0-9-]+")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.rootProcessInstanceId").value(matchesPattern("[a-f0-9-]+")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(matchesPattern("[a-f0-9-]+")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.processInstanceId").value(matchesPattern("[a-f0-9-]+")))
            .andReturn();
        log.info("Response: {}", result.getResponse().getContentAsString());
    }
}
