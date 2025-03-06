package ch.bpm.workflow.example.config;


import ch.guru.springframework.apifirst.ApiClient;
import ch.guru.springframework.apifirst.client.CustomerApi;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
@Getter
public class RestApiConfiguration {

    @Value("${application.apifirst-server-jpa.protocol}")
    private String protocol;

    @Value("${application.apifirst-server-jpa.host}")
    private String host;

    @Value("${application.apifirst-server-jpa.port}")
    private String port;

    @Value("${application.apifirst-server-jpa.context}")
    private String context;

    @Bean
    public RestTemplate customerApiRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CustomerApi customerApi(RestTemplate customerApiRestTemplate) {
        ApiClient apiClient = new ApiClient(customerApiRestTemplate);

        apiClient.addDefaultHeader("Accept", "application/json");
        apiClient.setBasePath(protocol + "://" + host + ":" + port + "/" + context);

        log.info("apifirst-server-jpa rest url: " + apiClient.getBasePath());

        return new CustomerApi(apiClient);
    }

}
