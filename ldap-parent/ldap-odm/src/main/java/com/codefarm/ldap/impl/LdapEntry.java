package com.codefarm.ldap.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.LoggerFactory;

import com.codefarm.ldap.ILdapEntry;
import com.codefarm.ldap.LdapManager;
import com.codefarm.ldap.annotations.DN;
import com.codefarm.ldap.annotations.LdapAttribute;
import com.codefarm.ldap.annotations.LdapEntity;
import com.codefarm.ldap.annotations.Manager;
import com.codefarm.ldap.exception.LdapNamingException;

@LdapEntity
public class LdapEntry implements ILdapEntry, Comparable
{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LdapEntry.class);
    
    protected boolean modified;
    
    protected boolean isNew;
    
    protected LinkedHashMap modificationItems;
    
    @Manager
    private LdapManager manager;
    
    /**
     * This contains all of the attributes for the object
     */
    @LdapAttribute(name = "*")
    protected Attributes attributes;
    
    @DN
    private LdapName dn;
    
    @LdapAttribute(name = "cn")
    private String cn;
    
    /**
     * All objectClass attributes, we know they are Strings
     */
    @LdapAttribute(name = "objectClass")
    protected List<String> objectClasses;
    
    /*    @Factory
        private LDAPFactory factory;*/
    
    @SuppressWarnings({ "CollectionWithoutInitialCapacity" })
    public LdapEntry()
    {
        modificationItems = new LinkedHashMap();
        objectClasses = new ArrayList<String>(5);
    }
    
    /**
     * LDAP Distinguished Name
     */
    /**
     * @return the distinquished name of the object. ie, fully qualified path in
     *         LDAP tree.
     */
    public LdapName getDn()
    {
        return dn;
    }
    
    @Override
    public String getDescription()
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }
    
    @Override
    public ILdapEntry convertInstance(final int type) throws NamingException
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }
    
    public String getStringAttribute(final Attributes attributes,
            final String attribute) throws NamingException
    {
        final Attribute temp;
        final String attributeValue;
        temp = attributes.get(attribute);
        if (temp != null)
        {
            attributeValue = (String) temp.get();
            logger.debug(attribute + ": " + getStringValue("cn"));
        }
        else
        {
            attributeValue = null;
        }
        
        return attributeValue;
    }
    
    public List getAttributeValues(final String attribute)
    {
        final Attribute ldapAttribute;
        List values = null;
        
        ldapAttribute = attributes.get(attribute);
        try
        {
            if (ldapAttribute != null && ldapAttribute.size() != 0)
            {
                values = Collections.list(ldapAttribute.getAll());
            }
        }
        catch (NamingException e)
        {
            throw new LdapNamingException(e);
        }
        
        return values;
    }
    
    public String getStringValue(final String attribute)
    {
        final Attribute ldapAttribute;
        try
        {
            ldapAttribute = attributes.get(attribute);
            if (ldapAttribute != null && ldapAttribute.size() != 0)
                return (String) ldapAttribute.get(0);
            else
            {
                return null;
            }
        }
        catch (NamingException e)
        {
            throw new LdapNamingException(e);
        }
        
    }
    
    /**
     * Sets the given attribute right now, and does not delay.  This should only
     * be used in the case where there is only one value for the attribute. If
     * there are multiple values, then the modifyBatchAttribute is the one that
     * really needs to be called.  If you call this with REPLACE_ATTRIBUTE for
     * instance, and there was multiple entries in LDAP, then the existing
     * entries will be replaced with this one value and only this one value. In
     * addition, if you want to modify multiple attributes at a time, then you
     * should not call this, you should use modifyBatchAttribute().
     *
     * @param operation on of ADD_ATTRIBUTE, REPLACE_ATTRIBUTE,
     *                  REMOVE_ATTRIBUTE
     * @param attribute the name of the attribute
     * @param value     the value of the attribute
     *
     * @see #ADD_ATTRIBUTE ADD_ATTRIBUTE
     * @see #REPLACE_ATTRIBUTE REPLACE_ATTRIBUTE
     * @see #REMOVE_ATTRIBUTE REMOVE_ATTRIBUTE
     */
    public void modifyAttribute(final int operation, final String attribute,
            final Object value)
    {
        modifyBatchAttribute(operation, attribute, value);
        modifyBatchAttributes(); // run the attribute operation
    }
    
    /**
     * Please note, the preferred method is to call setXXXX() where XXXX is the
     * attribute name, followed by save().
     * <p/>
     * This sets a batch attribute.  This means that it will be added to a queue
     * for changing LDAP.  You can modify the same attribute multiple times,
     * assuming LDAP supports multivalued attributes for that attribute. You are
     * then required to call modifyBatchAttributes(), which will actually do the
     * operations requested.
     * <p/>
     * You should call this one or more times per attribute, followed by
     * modifyBatchAttributes().
     * <p/>
     * Each time you call this method, for the same attribute, you should
     * specify the same operation, otherwise you will get an
     * IllegalArgumentException, with an appropriate error message.
     *
     * @param operation one of ADD_ATTRIBUTE, REPLACE_ATTRIBUTE,
     *                  REMOVE_ATTRIBUTE
     * @param attribute the name of the attribute
     * @param value     the value of the attribute
     *
     * @see #ADD_ATTRIBUTE ADD_ATTRIBUTE
     * @see #REPLACE_ATTRIBUTE REPLACE_ATTRIBUTE
     * @see #REMOVE_ATTRIBUTE REMOVE_ATTRIBUTE
     */
    public void modifyBatchAttribute(final int operation,
            final String attribute, final Object value)
    {
        final Attribute newAttribute;
        ModificationItem modItem;
        final int mod_op;
        
        switch (operation)
        {
            case ADD_ATTRIBUTE:
                mod_op = DirContext.ADD_ATTRIBUTE;
                break;
            case REPLACE_ATTRIBUTE:
                mod_op = DirContext.REPLACE_ATTRIBUTE;
                break;
            case REMOVE_ATTRIBUTE:
                mod_op = DirContext.REMOVE_ATTRIBUTE;
                break;
            default:
                mod_op = DirContext.ADD_ATTRIBUTE;
        }
        
        modItem = (ModificationItem) modificationItems.get(attribute);
        if (modItem == null)
        { // first time we are doing something with this attribute
            newAttribute = new BasicAttribute(attribute, value);
            modItem = new ModificationItem(mod_op, newAttribute);
        }
        else
        { // we will add it to the attribute values for this attribute
            if (modItem.getModificationOp() != mod_op)
            { // make sure they aren't changing their mind on which op
                throw new IllegalArgumentException(
                        "error, operation does not match previous batch items for this attribute");
            }
            
            modItem.getAttribute().add(value);
        }
        modified = true;
        modificationItems.put(attribute, modItem);
    }
    
    /**
     * Binds as a different user/password to modify the attributes. See {@link
     * #modifyBatchAttributes(String, String) for more information}
     */
    public void modifyBatchAttributes()
    {
        modifyBatchAttributes(manager.getBindDN(), manager.getBindPassword());
    }
    
    /**
     * Runs the batch modifications requested through the {@link
     * ILdapEntry#modifyBatchAttribute(int, String, Object)}
     */
    public void modifyBatchAttributes(final String bindDN,
            final String bindPassword)
    { // BEGIN modifyBatchAttributes()
        DirContext ldapContext = null;
        
        if (modificationItems.size() == 0)
        {
            throw new IllegalStateException("No modification items for batch");
        }
        try
        {
            final Object[] tempModItems;
            final ModificationItem[] modItems;
            tempModItems = modificationItems.values().toArray();
            modItems = new ModificationItem[tempModItems.length];
            for (int index = 0; index < tempModItems.length; index++)
            { // convert to ModificationItem array
                modItems[index] = (ModificationItem) tempModItems[index];
            }
            
            ldapContext = manager.getConnection(bindDN, bindPassword);
            ldapContext.modifyAttributes(getDn(), modItems);
            
            /**
             * Update the attributes in memory
             */
            for (final ModificationItem modItem : modItems)
            {
                final Attribute attribute;
                attribute = modItem.getAttribute();
                updateAttribute(attribute.getID());
            }
            //            manager.reloadAttributes(this);
        }
        catch (NamingException namingException)
        {
            throw new LdapNamingException(namingException);
        }
        catch (Exception exception)
        {
            throw new LdapNamingException("error modifying attributes",
                    exception);
        }
        finally
        {
            try
            {
                if (ldapContext != null)
                {
                    ldapContext.close();
                }
            }
            catch (NamingException namingException)
            {
                manager.logNamingException(namingException);
            }
            
            // recreate empty batch list
            modificationItems = new LinkedHashMap();
        }
    } // END modifyBatchAttributes()
    
    /**
     * Because LDAP operations are expensive, we have a save method.  Saves any
     * changes made by setXXXX() methods, where XXXX is an attribute name.  Also
     * an alias for modifyBatchAttributes(), but will do nothing unless
     * modifyBatchAtribute() has been called
     */
    public void save()
    {
        if (modified)
        {
            modified = false;
            modifyBatchAttributes();
        }
    }
    
    @Override
    public Attributes getBindAttributes()
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }
    
    /**
     * Updates the specified attribute from LDAP.
     * <p/>
     * MINOR : Instead of using LDAPFactory.getAttributes, using
     * DirContext.getAttributes().  Then we can remove the getAttributes().
     * <p/>
     * CRITICAL reload the attribute using the reflection framework somehow.
     *
     * @param attrName the name of the attribute
     *
     * @throws NamingException if any LDAP errors occur.
     */
    protected void updateAttribute(final String attrName)
            throws NamingException
    {
        final String[] returningAttributes;
        final Attributes returnedAttributes;
        
        returningAttributes = new String[1];
        returningAttributes[0] = attrName;
        returnedAttributes = manager.getAttributes(getDn(), returningAttributes);
        
        if (returnedAttributes.size() == 1)
        { // only attempt to load the attributes if the search found them.
          // the attribute to update
            attributes.put(returnedAttributes.get(attrName));
        }
    }
    
    public Attributes getAttributes()
    {
        return attributes;
    }
    
    public List<String> getObjectClasses()
    {
        return objectClasses;
    }
    
    @Override
    public String getCN()
    {
        return getCn();
    }
    
    public void setObjectClasses(final List<String> objectClasses)
    {
        this.objectClasses = objectClasses;
    }
    
    public boolean isObjectClass(final String objectClass)
    {
        return objectClasses.contains(objectClass);
    }
    
    @Override
    public String toString()
    {
        return "LdapEntry{" + "dn=" + getDn() + ", cn='" + getCn() + '\'' + '}';
    }
    
    @SuppressWarnings({ "ChainedMethodCall", "NonFinalFieldReferenceInEquals" })
    @Override
    public boolean equals(final Object o)
    {
        final LdapEntry rhs = (LdapEntry) o;
        
        return new EqualsBuilder().appendSuper(super.equals(o))
                .append(getDn(), rhs.getDn())
                .append(getCn(), rhs.getCn())
                .append(objectClasses, rhs.objectClasses)
                .isEquals();
    }
    
    @SuppressWarnings({ "ChainedMethodCall",
            "NonFinalFieldReferencedInHashCode" })
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(getDn())
                .append(getCn())
                .append(objectClasses)
                .toHashCode();
    }
    
    @SuppressWarnings({ "ChainedMethodCall", "CompareToUsesNonFinalVariable" })
    @Override
    public int compareTo(final Object o)
    {
        final LdapEntry myClass = (LdapEntry) o;
        return new CompareToBuilder().append(getDn(), myClass.getDn())
                .append(getCn(), myClass.getCn())
                .append(objectClasses, myClass.objectClasses)
                .toComparison();
    }
    
    @Override
    public void setDn(LdapName dn)
    {
        this.dn = dn;
    }
    
    @Override
    public String getCn()
    {
        return cn;
    }
    
    @Override
    public void setCn(String cn)
    {
        this.cn = cn;
    }
}
