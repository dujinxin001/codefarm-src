package com.sxj.ldap;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

public interface ILdapEntry
{
    /**
     * Used in setting attributes.  MEANING - add the attribute value, whether
     * other values for it exist or not.
     * <p/>
     * The values of this constant is 0
     * <p/>
     * {@link javax.naming.directory.DirContext#ADD_ATTRIBUTE}
     */
    int ADD_ATTRIBUTE = 0;
    
    /**
     * Used in setting attributes.  MEANING - modify the existing attribute. If
     * attribute already exists, replaces all existing values with new specified
     * values. If the attribute does not exist, creates it. If no value is
     * specified, deletes all the values of the attribute. Removal of the last
     * value will remove the attribute if the attribute is required to have at
     * least one value. If attempting to add more than one value to a
     * single-valued attribute, throws InvalidAttributeValueException.
     * <p/>
     * The value of this constant is 1
     * <p/>
     * {@link javax.naming.directory.DirContext#REPLACE_ATTRIBUTE}
     */
    int REPLACE_ATTRIBUTE = 1;
    
    /**
     * Used in setting attributes.  MEANING - delete the existing attribute.
     * Delete corresponding value, if *value* is specified.  Delete all if
     * *value* is null.  This may be used in a batch.  If you call the {@link
     * #modifyBatchAttribute} several times with REMOVE_ATTRIBUTE, then all the
     * attributes with the specified values will be removed.
     * <p/>
     * The value of this constant is 2
     * <p/>
     * {@link javax.naming.directory.DirContext#REMOVE_ATTRIBUTE}
     */
    int REMOVE_ATTRIBUTE = 2;
    
    /**
     * Get's the types of objects this is.
     *
     * @return a list of Strings representing the object class names
     */
    public List getObjectClasses();
    
    /**
     * @return the common name attribute.
     */
    public String getCN();
    
    /**
     * @return the distinquished name of the object. ie, fully qualified path in
     *         LDAP tree.
     */
    public LdapName getDn();
    
    /**
     * @return the description attribute.
     */
    public String getDescription();
    
    /**
     * @param type the object type to convert to
     *
     * @return the new converted object
     *
     * @throws NamingException if any conversion problems occur.
     */
    public ILdapEntry convertInstance(int type) throws NamingException;
    
    public List getAttributeValues(String attribute);
    
    public String getStringValue(String attribute);
    
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
     *
     * @param attribute the name of the attribute
     * @param value     the value of the attribute
     * @see ILdapEntry#ADD_ATTRIBUTE ADD_ATTRIBUTE
     * @see ILdapEntry#REPLACE_ATTRIBUTE REPLACE_ATTRIBUTE
     * @see ILdapEntry#REMOVE_ATTRIBUTE REMOVE_ATTRIBUTE
     */
    public void modifyAttribute(int operation, String attribute, Object value);
    
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
     *
     * @param attribute the name of the attribute
     * @param value     the value of the attribute
     * @see ILdapEntry#ADD_ATTRIBUTE ADD_ATTRIBUTE
     * @see ILdapEntry#REPLACE_ATTRIBUTE REPLACE_ATTRIBUTE
     * @see ILdapEntry#REMOVE_ATTRIBUTE REMOVE_ATTRIBUTE
     */
    public void modifyBatchAttribute(int operation, String attribute,
            Object value);
    
    /**
     * Runs the batch modifications requested through the {@link
     * ILdapEntry#modifyBatchAttribute(int, String, Object)}
     */
    public void modifyBatchAttributes();
    
    /**
     * Because LDAP operations are expensive, we have a save method.  Saves any
     * changes made by setXXXX() methods, where XXXX is an attribute name.  Also
     * an alias for modifyBatchAttributes(), but will do nothing unless
     * modifyBatchAtribute() has been called
     */
    public void save();
    
    public Attributes getBindAttributes();
    
    /*
       public String getStringValue(Attributes attributes,
            String attribute) throws NamingException;
        public List getAttributeValues(String attribute);
        public String getStringValue(String attribute);
        */
    
    void setDn(LdapName dn);
    
    String getCn();
    
    void setCn(String cn);
}
