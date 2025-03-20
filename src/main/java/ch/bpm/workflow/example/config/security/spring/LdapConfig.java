package ch.bpm.workflow.example.config.security.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@EnableLdapRepositories(basePackages = "ch.bpm.workflow.example.ldap.repository")
@Profile({"local"})
public class LdapConfig {

    @Value("${spring.ldap.url}")
    private String ldapUrl;

    @Value("${spring.ldap.base-dn}")
    private String baseDn;

    @Value("${spring.ldap.manager-dn}")
    private String managerDn;

    @Value("${spring.ldap.manager-password}")
    private String managerPassword;

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(baseDn);
        contextSource.setUserDn(managerDn);
        contextSource.setPassword(managerPassword);
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }
}
