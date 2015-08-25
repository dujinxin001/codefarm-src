package com.sxj.ldap;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.junit.Before;
import org.junit.Test;

import com.sxj.ldap.entity.Organization;

public class LdapManagerTest
{
    LdapManager manager;
    
    @Before
    public void setup()
    {
        manager = new LdapManager();
    }
    
    @Test
    public void testBind() throws InvalidNameException
    {
        Organization organization = new Organization();
        organization.setDn(new LdapName("o=ddd,dc=sxj,dc=com"));
        organization.setOrganization("ddd");
        manager.bind(organization);
    }
    
    public void testFind() throws InvalidNameException
    {
        Organization find = (Organization) manager.find(Organization.class,
                new LdapName("o=ddd,dc=sxj,dc=com"));
        System.out.println(find.getOrganization());
    }
    
    public void testUnbind() throws InvalidNameException
    {
        manager.unbind(new LdapName("o=ddd,dc=sxj,dc=com"));
    }
    
}
