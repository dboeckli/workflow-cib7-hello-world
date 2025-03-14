package ch.bpm.workflow.example.util.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.client.spring.impl.client.ClientConfiguration;
import org.cibseven.bpm.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import java.net.URI;
import java.net.URISyntaxException;

@TestConfiguration
@Slf4j
public class CamundaClientConfiguration {

    @Autowired
    private ClientConfiguration clientConfiguration;

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
    private ProcessEngine processEngine;

    // this is a workaround to initialize the Camunda client with the correct base URL. The test
    // is using a random port and random engine name. Therefore, we need to change the client
    // base url which include the engine name. We obtain the engine name from the ProcessEngine.
    @PostConstruct
    public void init() throws URISyntaxException {
        int port = webServerAppCtxt.getWebServer().getPort();

        URI uri = new URI(clientConfiguration.getBaseUrl());
        String baseUrl = new URI(uri.getScheme(), null, uri.getHost(), port, uri.getPath(), null, null).toString();

        baseUrl = baseUrl + "/engine/" +  processEngine.getName();
        log.info("Initializing Camunda client with rest baseUrl: {}", baseUrl);

        clientConfiguration.setBaseUrl(baseUrl);
    }

}
