package ch.bpm.workflow.example.controller;

import ch.bpm.workflow.example.controller.dto.PingResponse;
import com.google.gson.Gson;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
    "camunda.bpm.job-execution.enabled=false",
    "camunda.bpm.client.disable-auto-fetching=true",
    "spring.docker.compose.file=compose-it.yaml",
    "spring.docker.compose.stop.command=stop"
})
@AutoConfigureMockMvc
@ActiveProfiles(value = "local")
class PingRestControllerIT {

    @Autowired
    BuildProperties buildProperties;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetInfo() throws Exception {
        this.mockMvc.perform(get("/restapi/ping")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("camunda-admin", "camunda-admin-password")))
            //.with(httpBasic("camunda-admin", "camunda-admin-password")))
            .andExpect(status().isOk()).andExpect(content().json(new Gson().toJson(createResponse())));
    }

    @Test
    void testGetInfoWithoutCredentials() throws Exception {
        this.mockMvc.perform(get("/restapi/ping")
            .with(SecurityMockMvcRequestPostProcessors.anonymous()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetInfoWithWrongPassword() throws Exception {
        this.mockMvc.perform(get("/restapi/ping")
                .with(httpBasic("camunda-admin", "wrong-password-here")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Disabled("currently disabled. role base authentication is not implemented yet")
    void testGetInfoNotAllowed() throws Exception {
        this.mockMvc.perform(get("/restapi/ping")
                .with(httpBasic("user01", "user01-password")))
            .andExpect(status().isForbidden());
    }

    private PingResponse createResponse() {
        return PingResponse.builder()
            .mavenGroupdId(buildProperties.getGroup())
            .mavenArtifactId(buildProperties.getArtifact())
            .version(buildProperties.getVersion())
            .vendor(buildProperties.get("javaVendor"))
            .mavenTimeStamp(buildProperties.getTime().toString())
            .mavenUser(buildProperties.get("mavenUser"))
            .javaVersion(buildProperties.get("javaVersion"))
            .gitCommitId(buildProperties.get("commit-id"))
                                              .build();
    }

}
