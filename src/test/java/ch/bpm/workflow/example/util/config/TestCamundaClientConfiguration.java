package ch.bpm.workflow.example.util.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.client.spring.impl.client.ClientConfiguration;
import org.cibseven.bpm.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@TestConfiguration
public class TestCamundaClientConfiguration {

    @Value("${server.port}")
    int localServerPort;

    @Autowired
    protected ClientConfiguration clientConfiguration;

    @Autowired
    private ProcessEngine processEngine;

    @PostConstruct
    // this is a workaround to initialize the Camunda client with the correct base URL. The test
    // is using a random port and random engine name. Therefore, we need to change the client
    // base url which include the engine name. We obtain the engine name from the ProcessEngine.
    public void init() throws URISyntaxException {
        URI uri = new URI(clientConfiguration.getBaseUrl());
        String baseUrl = new URI(uri.getScheme(), null, uri.getHost(), localServerPort, uri.getPath(), null, null).toString();

        baseUrl = baseUrl + "/engine/" +  processEngine.getName();
        log.info("Initializing Camunda client with rest baseUrl: {}", baseUrl);

        clientConfiguration.setBaseUrl(baseUrl);
    }
}
