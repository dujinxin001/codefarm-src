package com.codefarm.ldap.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.LoggerFactory;

import com.codefarm.ldap.ILdapGroup;
import com.codefarm.ldap.ILdapOrganization;
import com.codefarm.ldap.annotations.LdapAttribute;
import com.codefarm.ldap.annotations.LdapEntity;
import com.codefarm.ldap.annotations.TypeHandler;

@LdapEntity(requiredObjectClasses = { "organization", "top" })
public class LdapOrganization extends LdapEntry implements ILdapOrganization,
        Comparable, TypeHandler
{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LdapOrganization.class);
    
    @LdapAttribute(name = "businessCategory", aggregateClass = LdapGroup.class, referencedDN = "${LdapOrganization.categoryDN}"
    /*"cn=?,ou=bus-categories,dc=example,dc=com"*/
    )
    private SortedMap<String, ILdapGroup> businessCategories;
    
    @LdapAttribute(name = "telephoneNumber")
    private String telephoneNumber;
    
    @LdapAttribute(name = "facsimileTelephoneNumber")
    private String facsimileTelephoneNumber;
    
    @LdapAttribute(name = "street")
    private String street;
    
    @LdapAttribute(name = "postOfficeBox")
    private String postOfficeBox;
    
    @LdapAttribute(name = "postalAddress")
    private String postalAddress;
    
    @LdapAttribute(name = "postalCode")
    private String postalCode;
    
    @LdapAttribute(name = "l")
    private String locality;
    
    @LdapAttribute(name = "o")
    private String organization;
    
    /**
     * Initializes an empty SortedMap for the businessCategories.
     */
    public LdapOrganization()
    {
        businessCategories = new TreeMap<String, ILdapGroup>();
    }
    
    public SortedMap<String, ? extends ILdapGroup> getBusinessCategories()
    {
        logger.debug("categories: " + businessCategories);
        return businessCategories == null ? new TreeMap<String, LdapGroup>()
                : Collections.unmodifiableSortedMap(businessCategories);
    }
    
    public String getTelephoneNumber()
    {
        return telephoneNumber;
    }
    
    public String getFacsimileTelephoneNumber()
    {
        return facsimileTelephoneNumber;
    }
    
    public String getStreet()
    {
        return street;
    }
    
    public String getPostOfficeBox()
    {
        return postOfficeBox;
    }
    
    public String getPostalCode()
    {
        return postalCode;
    }
    
    public String getPostalAddress()
    {
        return postalAddress;
    }
    
    public String getLocality()
    {
        return locality;
    }
    
    public String getOrganization()
    {
        return organization;
    }
    
    public void setBusinessCategories(final String[] categories,
            final int operation)
    {
        for (final String category : categories)
        {
            modifyBatchAttribute(REPLACE_ATTRIBUTE,
                    "businessCategory",
                    category);
        }
    }
    
    public void setTelephoneNumber(final String telephoneNumber,
            final int operation)
    {
        this.telephoneNumber = telephoneNumber;
    }
    
    public void setFacsimileTelephoneNumber(final String fax,
            final int operation)
    {
        facsimileTelephoneNumber = fax;
    }
    
    public void setStreet(final String street, final int operation)
    {
        this.street = street;
    }
    
    public void setPostOfficeBox(final String postOfficeBox, final int operation)
    {
        this.postOfficeBox = postOfficeBox;
    }
    
    public void setPostalCode(final String postalCode, final int operation)
    {
        this.postalCode = postalCode;
    }
    
    public void setPostalAddress(final String postalAddress, final int operation)
    {
        this.postalAddress = postalAddress;
    }
    
    public void setLocality(final String city, final int operation)
    {
        locality = city;
    }
    
    public void setOrganization(final String organizationName,
            final int operation)
    {
        organization = organizationName;
    }
    
    @Override
    public String toString()
    {
        return super.toString() + ", LdapOrganization{" + "businessCategories="
                + businessCategories + ", telephoneNumber='" + telephoneNumber
                + '\'' + ", facsimileTelephoneNumber='"
                + facsimileTelephoneNumber + '\'' + ", street='" + street
                + '\'' + ", postOfficeBox='" + postOfficeBox + '\''
                + ", postalAddress='" + postalAddress + '\'' + ", postalCode='"
                + postalCode + '\'' + ", locality='" + locality + '\''
                + ", organization='" + organization + '\'' + '}';
    }
    
    /**
     * returns the category dn with a bind variable.
     * <p/>
     * CRITICAL dn return value config option (issue-23)
     *
     * @return the dn with a bind variable.
     */
    @SuppressWarnings({ "PublicMethodNotExposedInInterface" })
    public String getCategoryDN()
    {
        return "cn=?,ou=bus-categories,dc=example,dc=com";
    }
    
    @SuppressWarnings({ "ChainedMethodCall", "NonFinalFieldReferenceInEquals" })
    @Override
    public boolean equals(final Object o)
    {
        final LdapOrganization rhs = (LdapOrganization) o;
        
        return new EqualsBuilder().appendSuper(super.equals(o))
                .append(businessCategories, rhs.businessCategories)
                .append(getBusinessCategories(), rhs.getBusinessCategories())
                .append(telephoneNumber, rhs.telephoneNumber)
                .append(facsimileTelephoneNumber, rhs.facsimileTelephoneNumber)
                .append(street, rhs.street)
                .append(postOfficeBox, rhs.postOfficeBox)
                .append(postalAddress, rhs.postalAddress)
                .append(postalCode, rhs.postalCode)
                .append(locality, rhs.locality)
                .append(organization, rhs.organization)
                .isEquals();
    }
    
    @SuppressWarnings({ "ChainedMethodCall",
            "NonFinalFieldReferencedInHashCode" })
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode())
                .append(businessCategories)
                .append(getBusinessCategories())
                .append(telephoneNumber)
                .append(facsimileTelephoneNumber)
                .append(street)
                .append(postOfficeBox)
                .append(postalAddress)
                .append(postalCode)
                .append(locality)
                .append(organization)
                .toHashCode();
    }
    
    @SuppressWarnings({ "ChainedMethodCall", "CompareToUsesNonFinalVariable" })
    @Override
    public int compareTo(final Object o)
    {
        final LdapOrganization myClass = (LdapOrganization) o;
        return new CompareToBuilder().appendSuper(super.compareTo(o))
                .append(businessCategories, myClass.businessCategories)
                .append(getBusinessCategories(),
                        myClass.getBusinessCategories())
                .append(telephoneNumber, myClass.telephoneNumber)
                .append(facsimileTelephoneNumber,
                        myClass.facsimileTelephoneNumber)
                .append(street, myClass.street)
                .append(postOfficeBox, myClass.postOfficeBox)
                .append(postalAddress, myClass.postalAddress)
                .append(postalCode, myClass.postalCode)
                .append(locality, myClass.locality)
                .append(organization, myClass.organization)
                .toComparison();
    }
    
    @SuppressWarnings({ "unchecked" })
    @Override
    public Object processValues(final List list, final Class refType)
    {
        Object fieldValue = null;
        if (SortedMap.class.equals(refType))
        { // we know what to do
            final Map categoryMap = new TreeMap();
            for (final Object ldapGroup : list)
            {
                categoryMap.put(((ILdapGroup) ldapGroup).getCN(), ldapGroup);
            }
            fieldValue = categoryMap;
        }
        
        return fieldValue;
    }
    
    @SuppressWarnings({ "unchecked" })
    @Override
    public List getValues(final Class classType, final Class refType,
            final Object instance)
    {
        final List<String> values = new ArrayList<String>(10);
        if (instance instanceof SortedMap)
        { // we know what to do
            final SortedMap<String, LdapGroup> groups = (SortedMap<String, LdapGroup>) instance;
            for (final ILdapGroup group : groups.values())
            {
                values.add(group.getCN());
            }
        }
        
        return values;
    }
}
