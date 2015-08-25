package com.sxj.ldap;

public interface ILdapPerson extends ILdapEntry
{
    public String getUID();
    
    public String getEMail();
}
