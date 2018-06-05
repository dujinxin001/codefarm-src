package com.codefarm.ldap;

import java.util.Map;
import java.util.SortedSet;

import javax.naming.InvalidNameException;

public interface ILdapGroup extends ILdapEntry
{
    /**
     *
     * @param keyAttribute key for the map
     * @param objectType
     *
     * @return map of members with organization as the key.  The members are of
     *         type @link{LDAPFactory.LDAP_TNT_BUSINESS} @param keyAttribute
     */
    Map getMembers(String keyAttribute, int objectType)
            throws InvalidNameException;
    
    /**
     * Add a member to this group.
     *
     * @param ldapEntry the ldap object corresponding to the member to add.
     */
    void addMember(ILdapEntry ldapEntry);
    
    /**
     * Remove a member from this group.
     *
     * @param ldapEntry the ldap object corresponding to the member to add.
     */
    void removeMember(ILdapEntry ldapEntry);
    
    /**
     * Returns a non-null SortedSet of group members.  If there are no members,
     * the set's size is ZERO.
     *
     * @return all members in DN format, in alphabetical order.
     */
    SortedSet getMembers();
    
    SortedSet<String> getBusinessCategories();
}
