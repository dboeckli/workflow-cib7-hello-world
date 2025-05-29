package ch.bpm.workflow.example.config.security.camunda;

import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cibseven.bpm.engine.impl.plugin.AdministratorAuthorizationPlugin;
import org.cibseven.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.cibseven.bpm.engine.rest.security.auth.impl.CompositeAuthenticationProvider;
import org.cibseven.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider;
import org.cibseven.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.lang.reflect.Field;

import static org.cibseven.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter.AUTHENTICATION_PROVIDER_PARAM;

@Configuration
@Slf4j
@Profile({"local"})
public class CamundaLdapConfiguration {
    @Value("${camunda.bpm.run.auth.authentication}")
    private String authenticationProvider;

    @Value("${camunda-ldap-plugin.url}")
    private String ldapUrl;

    @Value("${camunda-ldap-plugin.sslEnabled}")
    private Boolean useSsl;

    @Value("${camunda-ldap-plugin.user}")
    private String ldapUserName;

    @Value("${camunda-ldap-plugin.password}")
    private String ldapUserPassword;

    @Value("${camunda-ldap-plugin.base-dn}")
    private String baseDn;

    @Value("${camunda-ldap-plugin.users.user-search-base}")
    private String userSearchBase;

    @Value("${camunda-ldap-plugin.users.user-search-filter}")
    private String userSearchFilter;

    @Value("${camunda-ldap-plugin.users.user-id-attribute}")
    private String useridAttribute;

    @Value("${camunda-ldap-plugin.users.user-firstname-attribute}")
    private String userFirstnameAttribute;

    @Value("${camunda-ldap-plugin.users.user-lastname-attribute}")
    private String userLastnameAttribute;

    @Value("${camunda-ldap-plugin.users.user-email-attribute}")
    private String userEmailAttribute;

    @Value("${camunda-ldap-plugin.users.user-password-attribute}")
    private String userPasswordAttribute;

    @Value("${camunda-ldap-plugin.groups.group-search-base}")
    private String groupSearchBase;

    @Value("${camunda-ldap-plugin.groups.group-search-filter}")
    private String groupSearchFilter;

    @Value("${camunda-ldap-plugin.groups.group-id-attribute}")
    private String groupIdAttribute;

    @Value("${camunda-ldap-plugin.groups.group-name-attribute}")
    private String groupNameAttribute;

    @Value("${camunda-ldap-plugin.groups.group-member-attribute}")
    private String groupMemberAttribute;

    @Value("${camunda-authorization-plugin.admin-group}")
    private String adminGroup;

    @Value("${camunda-authorization-plugin.admin-user}")
    private String adminUser;

    @Bean
    public LdapIdentityProviderPlugin ldapIdentityProviderPlugin() {
        LdapIdentityProviderPlugin ldapIdentityProviderPlugin = new LdapIdentityProviderPlugin();

        ldapIdentityProviderPlugin.setServerUrl(ldapUrl);
        ldapIdentityProviderPlugin.setManagerDn(ldapUserName);
        ldapIdentityProviderPlugin.setManagerPassword(ldapUserPassword);
        ldapIdentityProviderPlugin.setUseSsl(useSsl);
        ldapIdentityProviderPlugin.setSecurityAuthentication("simple");
        ldapIdentityProviderPlugin.setBaseDn(baseDn);

        ldapIdentityProviderPlugin.setAcceptUntrustedCertificates(true);
        ldapIdentityProviderPlugin.setAllowAnonymousLogin(false);
        ldapIdentityProviderPlugin.setAuthorizationCheckEnabled(true);

        ldapIdentityProviderPlugin.setUserSearchBase(userSearchBase);
        ldapIdentityProviderPlugin.setUserSearchFilter(userSearchFilter);
        ldapIdentityProviderPlugin.setUserIdAttribute(useridAttribute);
        ldapIdentityProviderPlugin.setUserFirstnameAttribute(userFirstnameAttribute);
        ldapIdentityProviderPlugin.setUserLastnameAttribute(userLastnameAttribute);
        ldapIdentityProviderPlugin.setUserEmailAttribute(userEmailAttribute);
        ldapIdentityProviderPlugin.setUserPasswordAttribute(userPasswordAttribute);

        ldapIdentityProviderPlugin.setGroupSearchBase(groupSearchBase);
        ldapIdentityProviderPlugin.setGroupSearchFilter(groupSearchFilter);
        ldapIdentityProviderPlugin.setGroupIdAttribute(groupIdAttribute);
        ldapIdentityProviderPlugin.setGroupNameAttribute(groupNameAttribute);
        ldapIdentityProviderPlugin.setGroupMemberAttribute(groupMemberAttribute);

        return ldapIdentityProviderPlugin;
    }

    @Bean
    public AdministratorAuthorizationPlugin administratorAuthorizationPlugin() {
        AdministratorAuthorizationPlugin plugin = new AdministratorAuthorizationPlugin();
        plugin.setAdministratorGroupName(adminGroup);
        plugin.setAdministratorUserName(adminUser);
        return plugin;
    }

    @Bean
    public FilterRegistrationBean<Filter> processEngineAuthenticationFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setName("camunda-auth");
        registration.setFilter(getProcessEngineAuthenticationFilter());

        // This is a workaround to set the authentication provider dynamically based on the provided configuration
        if (authenticationProvider.equals("basic")) {
            registration.addInitParameter(AUTHENTICATION_PROVIDER_PARAM, HttpBasicAuthenticationProvider.class.getName());
            log.info("### Using basic authentication");
        } else if (authenticationProvider.equals("composite")) {
            registration.addInitParameter(AUTHENTICATION_PROVIDER_PARAM, CompositeAuthenticationProvider.class.getName());
            log.info("### Using composite authentication");
        } else {
            throw new IllegalArgumentException("Invalid authentication provider: " + authenticationProvider);
        }

        registration.addUrlPatterns("/restapi/*");
        registration.addUrlPatterns("/engine-rest/*");

        registration.setOrder(1);
        return registration;
    }

    @Bean
    public Filter getProcessEngineAuthenticationFilter() {
        return new ProcessEngineAuthenticationFilter();
    }

    public static String printLdapIdentityProviderPlugin(LdapIdentityProviderPlugin ldapIdentityProviderPlugin) {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(
                ldapIdentityProviderPlugin, ToStringStyle.MULTI_LINE_STYLE) {
            @Override
            protected boolean accept(Field field) {
                return !field.getName().equals("managerPassword");
            }
        };
        return builder.toString();
    }
}
