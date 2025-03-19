package ch.bpm.workflow.example.ldap.model;

import lombok.Data;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;

import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

@Entry(objectClasses = {"inetOrgPerson"}, base = "ou=users")
@Data
public final class Person {

    @Id
    private Name id;

    @Attribute(name = "cn")
    private String commonName;

    @Attribute(name = "uid")
    private String uid;

    @Attribute(name = "givenName")
    private String givenName;

    @Attribute(name = "sn")
    private String lastname;

    @Attribute(name = "userPassword")
    private String userPassword;

    @Attribute(name = "mail")
    private String mail;

}
