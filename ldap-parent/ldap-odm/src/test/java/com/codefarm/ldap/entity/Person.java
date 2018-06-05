package com.codefarm.ldap.entity;

import javax.naming.ldap.LdapName;

import com.codefarm.ldap.annotations.DN;
import com.codefarm.ldap.annotations.LdapEntity;

@LdapEntity(requiredObjectClasses = { "organizationalPerson,person,top" })
public class Person
{
    @DN
    private LdapName dn;
    
}
