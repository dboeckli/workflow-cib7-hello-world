package ch.bpm.workflow.example.config;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static ch.bpm.workflow.example.common.LogMessage.READING_CONFIG_CLASS;
import static ch.bpm.workflow.example.config.CamundaLdapConfiguration.printLdapIdentityProviderPlugin;

@Component
@Slf4j
@Profile({"local", "ci"})
public class InitializingBeanImpl implements InitializingBean {

    private final LdapIdentityProviderPlugin ldapIdentityProviderPlugin;

    public InitializingBeanImpl(LdapIdentityProviderPlugin ldapIdentityProviderPlugin) {
        this.ldapIdentityProviderPlugin = ldapIdentityProviderPlugin;
    }

    @Override
    public void afterPropertiesSet() {
        log.info(READING_CONFIG_CLASS.getMessage(), this.getClass().getName(), printLdapIdentityProviderPlugin(ldapIdentityProviderPlugin));
    }
}
