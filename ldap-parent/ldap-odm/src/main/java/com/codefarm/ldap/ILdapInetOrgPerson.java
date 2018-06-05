package com.codefarm.ldap;

public interface ILdapInetOrgPerson extends ILdapEntry
{
    public String getDisplayName();
    
    public String getCarLicense();
    
    public String getDepartmentNumber();
    
    public String getEmployeeNumber();
}
