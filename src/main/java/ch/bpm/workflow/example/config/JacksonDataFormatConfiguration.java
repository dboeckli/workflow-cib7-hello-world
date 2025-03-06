package ch.bpm.workflow.example.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static ch.bpm.workflow.example.common.LogMessage.JACKSON_CONFIG_MODULES;
import static ch.bpm.workflow.example.common.LogMessage.JACKSON_CONFIG_START;

@Slf4j
@Configuration
public class JacksonDataFormatConfiguration {

    final ObjectMapper objectMapper;

    @Autowired
    public JacksonDataFormatConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void configure() {
        log.info(JACKSON_CONFIG_START.getMessage());

        // TODO: check this one: objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules().disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);

        List<Module> modules = ObjectMapper.findModules();
        modules.forEach(module -> log.info(JACKSON_CONFIG_MODULES.getMessage(), module, module.version()));
    }
}
