package com.sxj.ldap.entity;

import javax.naming.ldap.LdapName;

import com.sxj.ldap.annotations.DN;
import com.sxj.ldap.annotations.LdapEntity;

@LdapEntity(requiredObjectClasses = { "organizationalPerson,person,top" })
public class Person
{
    @DN
    private LdapName dn;
    
}
