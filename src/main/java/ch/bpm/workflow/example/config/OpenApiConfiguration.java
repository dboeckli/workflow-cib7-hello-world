package ch.bpm.workflow.example.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static ch.bpm.workflow.example.controller.CamundaOpenApiRestController.CAMUNDA_OPENAPI_CONTEXT;

@OpenAPIDefinition(
    info = @Info(
        license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
    )
)
@Configuration
@RequiredArgsConstructor
@Slf4j
public class OpenApiConfiguration {

    private final BuildProperties buildProperties;

    private final Environment environment;

    @Value("${camunda.bpm.client.basic-auth.username}")
    private String username;

    @Value("${camunda.bpm.client.basic-auth.password}")
    private String password;

    @Bean
    @Qualifier("customGlobalHeaderOpenApiCustomizer")
    public OpenApiCustomizer customGlobalHeaderOpenApiCustomizer() {
        return openApi -> {
            io.swagger.v3.oas.models.info.Info info = openApi.getInfo();
            info.setTitle(buildProperties.getName());
            info.setVersion(buildProperties.getVersion());
            if (openApi.getTags() != null) {
                log.info("Setting descriptions for tags" + openApi.getTags());
                openApi.getTags().forEach(tag -> {
                    log.info("Setting description for tag '{}'", tag.getName());
                    if ("Actuator".equals(tag.getName())) {
                        tag.setDescription("Spring Boot Actuator endpoints for monitoring and managing the application");
                    }
                    // Add other group descriptions here if needed
                });
            }
        };
    }

    @Bean
    // TODO: ADD SECURITY
    public GroupedOpenApi restapiApi(@Qualifier("customGlobalHeaderOpenApiCustomizer") OpenApiCustomizer customGlobalHeaderOpenApiCustomizer) {
        return GroupedOpenApi.builder()
            .group("restapi")
            .addOpenApiCustomizer(customGlobalHeaderOpenApiCustomizer)
            .addOpenApiCustomizer(openApi -> {
                openApi.components(new Components()
                    .addSecuritySchemes("basicAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")));
                openApi.addSecurityItem(new SecurityRequirement().addList("basicAuth"));
            })
            .displayName("REST API")
            .pathsToMatch("/restapi/**")
            .build();
    }

    @Bean
    public GroupedOpenApi actuatorApi(@Qualifier("customGlobalHeaderOpenApiCustomizer") OpenApiCustomizer customGlobalHeaderOpenApiCustomizer) {
        return GroupedOpenApi.builder()
            .group("actuator")
            .addOpenApiCustomizer(customGlobalHeaderOpenApiCustomizer)
            .displayName("Actuator")
            .pathsToMatch("/actuator/**")
            .build();
    }

    @Bean
    public GroupedOpenApi camnundaEngineRestApi(@Qualifier("customGlobalHeaderOpenApiCustomizer") OpenApiCustomizer customGlobalHeaderOpenApiCustomizer) {
        return GroupedOpenApi.builder()
            .group("camunda-engine-rest-api")
            .addOpenApiCustomizer(customGlobalHeaderOpenApiCustomizer)
            .displayName("Camunda Engine Rest API")
            .pathsToMatch("/engine-rest/**")
            .addOpenApiCustomizer(openApi -> {
                try {
                    String port = environment.getProperty("local.server.port");
                    String hostname = "localhost";
                    String scheme = environment.getProperty("server.ssl.key-store") != null ? "https" : "http";

                    String camundaJsonOpenApiUrl = scheme + "://" + hostname + ":" + port + CAMUNDA_OPENAPI_CONTEXT;
                    URL url = new URL(camundaJsonOpenApiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // Set up basic auth
                    String auth = username + ":" + password;
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                    String authHeaderValue = "Basic " + encodedAuth;
                    connection.setRequestProperty("Authorization", authHeaderValue);

                    // Read the response
                    StringBuilder content = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                    }

                    OpenAPI externalOpenApi = new OpenAPIV3Parser().readContents(content.toString()).getOpenAPI();

                    // Set the correct server URL
                    externalOpenApi.getServers().clear();
                    String engineRestUrl = scheme + "://" + hostname + ":" + port + "/engine-rest";
                    externalOpenApi.addServersItem(new Server().url(engineRestUrl));

                    // Merge paths and components from external OpenAPI into the current one
                    openApi.getPaths().putAll(externalOpenApi.getPaths());
                    if (externalOpenApi.getComponents() != null) {
                        if (openApi.getComponents() == null) {
                            openApi.setComponents(new Components());
                        }
                        openApi.getComponents().getSchemas().putAll(externalOpenApi.getComponents().getSchemas());
                    }

                    openApi.setServers(externalOpenApi.getServers());

                } catch (Exception ex) {
                    throw new RuntimeException("Failed to load camunda OpenAPI definition." ,ex);
                }
            })
            .build();
    }
}
