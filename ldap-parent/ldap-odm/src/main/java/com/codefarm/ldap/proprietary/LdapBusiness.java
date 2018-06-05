package com.codefarm.ldap.proprietary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.codefarm.ldap.annotations.LdapAttribute;
import com.codefarm.ldap.annotations.LdapEntity;
import com.codefarm.ldap.impl.LdapOrganization;

@LdapEntity(requiredObjectClasses = { "tntbusiness" })
public class LdapBusiness extends LdapOrganization implements ILdapBusiness
{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LdapBusiness.class);
    
    @LdapAttribute(name = "businessContact")
    private String businessContact;
    
    private String[] labeledURI;
    
    @LdapAttribute(name = "mail")
    private List<String> mail;
    
    public LdapBusiness()
    {
        super();
        mail = new ArrayList();
    }
    
    public String getBusinessContact()
    {
        return businessContact;
    }
    
    public String[] getLabeledURI()
    {
        return labeledURI;
    }
    
    public String getMail()
    {
        return mail != null && mail.size() > 0 ? mail.get(0) : null;
    }
    
    @Override
    public String[] getMails()
    {
        return mail.toArray(new String[] {});
    }
    
    public void setBusinessContact(final String businessContact,
            final int operation)
    {
        modifyBatchAttribute(operation, "businessContact", businessContact);
        this.businessContact = businessContact;
    }
    
    protected void setLabeledURI(String labeledURI)
    {
        logger.info("labeledURI: " + labeledURI);
        if (labeledURI != null)
        {
            final String[] webComponents;
            this.labeledURI = new String[2];
            webComponents = labeledURI.split(" ", 2);
            switch (webComponents.length)
            {
                case 0:
                    labeledURI = null;
                    break;
                case 1:
                    logger.debug("labeledURI: " + labeledURI);
                    this.labeledURI[0] = webComponents[0].matches("http:\\/\\/") ? ("http://" + webComponents[0])
                            : webComponents[0];
                    this.labeledURI[1] = getOrganization();
                    break;
                case 2:
                    this.labeledURI[0] = webComponents[0].matches("http:\\/\\/") ? ("http://" + webComponents[0])
                            : webComponents[0];
                    this.labeledURI[1] = webComponents[1];
                    break;
            }
        }
        else
        {
            this.labeledURI = new String[2];
        }
        logger.info("labeledURI[0]: " + this.labeledURI[0]);
        logger.info("labeledURI[1]: " + this.labeledURI[1]);
    }
    
    public void setLabeledURI(final String labeledURI, final int operation)
    {
        setLabeledURI(labeledURI);
        modifyBatchAttribute(operation, "labeledURI", this.labeledURI[0] + " "
                + this.labeledURI[1]);
    }
    
    public void setMail(final String mail, final int operation)
    {
        modifyBatchAttribute(operation, "mail", mail);
        //        this.mail = mail;
    }
    
    @Override
    public String toString()
    {
        return super.toString() + ", LdapBusiness{" + "businessContact='"
                + businessContact + '\'' + ", labeledURI="
                + (labeledURI == null ? null : Arrays.asList(labeledURI))
                + ", mail=" + mail + '}';
    }
}
