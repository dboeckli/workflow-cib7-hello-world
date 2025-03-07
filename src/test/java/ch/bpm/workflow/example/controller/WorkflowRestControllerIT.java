package ch.bpm.workflow.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@DirtiesContext
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "local")
@Slf4j
class WorkflowRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void startProcess() throws Exception {
        MvcResult result = this.mockMvc
            .perform(post("/restapi/workflow")
            .with(httpBasic("camunda-admin", "camunda-admin-password")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.caseInstanceId").value(nullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.processDefinitionId").value(matchesPattern("hello-world-process:1:[a-f0-9-]+")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.rootProcessInstanceId").value(matchesPattern("[a-f0-9-]+")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(matchesPattern("[a-f0-9-]+")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.processInstanceId").value(matchesPattern("[a-f0-9-]+")))
            .andReturn();
        log.info("Response: {}", result.getResponse().getContentAsString());
    }
}
