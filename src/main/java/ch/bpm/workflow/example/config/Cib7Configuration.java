package ch.bpm.workflow.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
/*
TODO: FIX FOR THIS ISSUE WHEN UPGRADE TO CIB7 2.0.0. See https://github.com/cibseven/cibseven/issues/85
Parameter 0 of method setPropertyConfigurer in org.cibseven.bpm.client.spring.impl.client.ClientFactory required a single bean, but 2 were found:
	- propertySourcesPlaceholderConfigurer: defined by method 'propertySourcesPlaceholderConfigurer' in class path resource [org/springframework/boot/autoconfigure/context/PropertyPlaceholderAutoConfiguration.class]
	- placeholderConfigurer: defined by method 'placeholderConfigurer' in class path resource [org/cibseven/webapp/SevenWebclientContext.class]
 */
public class Cib7Configuration {

    @Bean
    @Primary
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
