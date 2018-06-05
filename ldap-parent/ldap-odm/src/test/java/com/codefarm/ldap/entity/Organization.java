package com.codefarm.ldap.entity;

import javax.naming.ldap.LdapName;

import com.codefarm.ldap.annotations.DN;
import com.codefarm.ldap.annotations.LdapAttribute;
import com.codefarm.ldap.annotations.LdapEntity;

@LdapEntity(requiredObjectClasses = "organization")
public class Organization
{
    @DN
    private LdapName dn;
    
    @LdapAttribute(name = "o")
    private String organization;
    
    public LdapName getDn()
    {
        return dn;
    }
    
    public void setDn(LdapName dn)
    {
        this.dn = dn;
    }
    
    public String getOrganization()
    {
        return organization;
    }
    
    public void setOrganization(String organization)
    {
        this.organization = organization;
    }
}
