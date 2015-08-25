package com.sxj.ldap.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.sxj.ldap.ILdapEntry;
import com.sxj.ldap.ILdapGroup;
import com.sxj.ldap.LdapManager;
import com.sxj.ldap.annotations.LdapAttribute;
import com.sxj.ldap.annotations.LdapEntity;

@LdapEntity
public class LdapGroup extends LdapEntry implements ILdapGroup, Serializable,
        Comparable
{
    
    /**
     * REQUIRED_FEATURE replace with SortedMap of ILdapOrganization entries,
     * once we have gotten around infinite recursion.  However, this should
     * probably be done in an example object in the test package, not here, as
     * we do not necessarily want to link back to the original object.  Then
     * again, maybe we should simply be creating two separate sub projects.  One
     * for common Classes, doing it the way we "think", and one that is the
     * actual framework.
     * <p/>
     * SortedSet of members of the group, in DN String format.
     */
    @LdapAttribute(name = "member")
    protected final SortedSet<String> sortedMembers;
    
    @LdapAttribute(name = "businessCategory")
    private final SortedSet<String> businessCategories;
    
    /*    public LdapGroup(final Attributes attributes, final LdapName dn)
            throws NamingException
        {
            super(attributes, dn);

            verifyObjectClass();
            populateMemebrs();
        }*/
    
    public LdapGroup()
    {
        super();
        sortedMembers = new TreeSet<String>();
        businessCategories = new TreeSet<String>();
    }
    
    /*    private void populateMemebrs()
        {
            final List members;
            members = getAttributeValues("member");
            sortedMembers = new TreeSet(members);
            businessCategory = getStringValue("businessCategory");
        }

        private void verifyObjectClass() throws NamingException
        {
            if (!(isObjectClass("groupOfNames") ||
                isObjectClass("groupOfUniqueNames") ||
                isObjectClass("tntGroupOfNames")))
            {
                throw new ObjectClassNotSupportedException(
                    objectClasses.toString() +
                        " must be one of [tntGroupOfNames, groupOfNames, groupOfUniqueNames]");
            }
        }*/
    
    public Map getMembers(final String keyAttribute, final int objectType)
            throws InvalidNameException
    {
        final Iterator memberIt;
        final Map members;
        
        final LdapManager manager = new LdapManager();
        members = new TreeMap();
        memberIt = sortedMembers.iterator();
        while (memberIt.hasNext())
        {
            final String member;
            member = (String) memberIt.next();
            final ILdapEntry ldapEntry = (ILdapEntry) manager.find(LdapEntry.class,
                    new LdapName(member));
            if (ldapEntry != null)
            {
                members.put(ldapEntry.getStringValue(keyAttribute), ldapEntry);
            }
        }
        
        return members;
    }
    
    @Override
    public SortedSet getMembers()
    {
        return Collections.unmodifiableSortedSet(sortedMembers);
    }
    
    public void addMember(final ILdapEntry ldapEntry)
    {
        modifyBatchAttribute(ILdapEntry.ADD_ATTRIBUTE,
                "member",
                ldapEntry.getDn().toString());
    }
    
    public void removeMember(final ILdapEntry ldapEntry)
    {
        modifyBatchAttribute(ILdapEntry.REMOVE_ATTRIBUTE,
                "member",
                ldapEntry.getDn().toString());
    }
    
    @SuppressWarnings({ "ChainedMethodCall" })
    @Override
    public String toString()
    {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }
    
    @SuppressWarnings({ "ChainedMethodCall", "NonFinalFieldReferenceInEquals" })
    @Override
    public boolean equals(final Object o)
    {
        final LdapGroup rhs = (LdapGroup) o;
        
        return new EqualsBuilder().appendSuper(super.equals(o))
                .append(sortedMembers, rhs.sortedMembers)
                .isEquals();
    }
    
    @SuppressWarnings({ "ChainedMethodCall",
            "NonFinalFieldReferencedInHashCode" })
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode())
                .append(sortedMembers)
                .toHashCode();
    }
    
    @SuppressWarnings({ "ChainedMethodCall", "CompareToUsesNonFinalVariable" })
    @Override
    public int compareTo(final Object o)
    {
        final LdapGroup myClass = (LdapGroup) o;
        return new CompareToBuilder().appendSuper(super.compareTo(o))
                .append(sortedMembers, myClass.sortedMembers)
                .toComparison();
    }
    
    /**
     * Retrieves a list of business categories for this group.
     *
     * @return a SortedSet of category names
     */
    @Override
    public SortedSet<String> getBusinessCategories()
    {
        return Collections.unmodifiableSortedSet(businessCategories);
    }
}
