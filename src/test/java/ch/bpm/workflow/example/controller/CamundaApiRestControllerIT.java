package ch.bpm.workflow.example.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "test")
class CamundaApiRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getFile() throws Exception {
        this.mockMvc.perform(get("/restapi/camunda")).andExpect(status().isOk()).andExpect(content().json(createResponse()));
    }

    private String createResponse() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/openapi.json");
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

}
