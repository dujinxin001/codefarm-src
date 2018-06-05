package com.codefarm.ldap.proprietary;

import com.codefarm.ldap.ILdapOrganization;

public interface ILdapBusiness extends ILdapOrganization
{
    public String getBusinessContact();
    
    public String[] getLabeledURI();
    
    public String getMail();
    
    public void setBusinessContact(String businessContact, int operation);
    
    public void setLabeledURI(String labeledURI, int operation);
    
    public void setMail(String mail, int operation);
    
    /**
     * @return An array of all email addresses
     */
    String[] getMails();
}
