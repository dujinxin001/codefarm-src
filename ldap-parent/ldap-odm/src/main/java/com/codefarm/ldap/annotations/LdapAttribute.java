package com.codefarm.ldap.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.naming.directory.Attributes;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LdapAttribute
{
    /**
     * The name of the attribute to store in this field, or '*' to store all
     * attributes in this field.  A name of '*' requests storage of ALL
     * attributes in an {@link Attributes} field variable. Fields with a name of
     * '*' are ignored during binding, updating, and are used only for queries.
     * <p/>
     * In the case of an aggregate, where we take another LDAP entry, and inject
     * it, the name of the attribute in the current ldap entry that should have
     * it's value injected into the DN returned by {@link #referencedDNMethod}.
     *
     * @return the name of the attribute
     */
    String name();
    
    /**
     * The {@link LdapEntity} annotated class that should be used for the
     * aggregate. The default class of Object.class, implies that this ldap
     * attribute will not be stored in an aggregate {@link LdapEntity} object,
     * but instead, the values will be stored in a String (only one value), a
     * byte array (a binary object, only one), or a collection (multiple valued
     * attributes of String or byte array), depending on the field's type
     * definition. Note that for Sun's LDAP provider, String, and byte arrays
     * are the only options supported for attribute values.
     * <p/>
     * Note that only List, SortedSet, String, and a java native array are
     * supported for the field type definition.
     * <p/>
     * There is no need for any of these types to be pre-initialized, as they
     * will be replaced if they exist in LDAP.
     * <p/>
     * It is implied that when using a SortedSet, the LdapEntity annotated class
     * MUST also implement the {@link Comparable} interface.  If it does not
     * implement this interface, then the SortedSet will throw a
     * ClassCastException, because it does not implement the Comparable
     * interface.
     *
     * @return the aggregate {@link LdapEntity} annotated class, or Object.class
     *         as a default.
     */
    Class<?> aggregateClass() default Object.class;
    
    /**
     * Currently unused, may be useful in the future for something.
     * <p/>
     * Defines the objectClass type used when doing aggregation.  It may be
     * {@link ObjectClassType#STRUCTURAL} or {@link ObjectClassType#AUXILIARY}
     * defaults to {@link ObjectClassType#AUXILIARY}, as that is the most likely
     * one to be used when injecting into an existing LDAP object
     *
     * @return the type defined
     */
    ObjectClassType ocType() default ObjectClassType.AUXILIARY;
    
    /**
     * If {@link #aggregateClass()} is used, this refers to the method in the
     * same object as the field, which returns the reference DN of the entry to
     * load.  Run on sentence, READY?  The string returned must have a bind
     * variable ('?') in it, to designate the location to replace with the value
     * of the LDAP attribute defined by {@link #name()}. An LdapName will be
     * constructed from the resulting DN, and used as the location for the LDAP
     * aggregate we're loading.  If a '?' is not present, an LdapNamingException
     * will be thrown indicating as much.
     * <p/>
     * For example, you may have a DN of "cn=?,ou=bus-categories,dc=example,dc=com",
     * where '?' is replaced by the value of the attribute.  Perhaps you have a
     * "businessCategory" attribute set to "Hair Salons".  The final DN lookup
     * for the aggregation would be "cn=Hair Salons,ou=bus-categories,..."
     * <p/>
     * In addition, an aggregate that has this set to the default value, is
     * implicitly a local aggregate.  See the {@link LdapAttribute LdapAttribute
     * class} documentation for more information.
     * <p/>
     * If only a '?' bind parameter is returned by the method, then the
     * attribute is assumed to contain the entire DN, and no ldap attribute
     * escaping will occur on the attribute.
     * <p/>
     * * CRITICAL fix infinite recursion (issue-15)
     *
     * @return the method that returns the DN entry to load, with the bind
     *         variable ('?')
     */
    String referencedDNMethod() default "";
    
    /**
     * Exact same as {@link #referencedDNMethod()} except that there is no need
     * for a method, this returns the DN with replaceable parameter embedded.
     * <p/>
     * If the reference is in ${property.name} syntax, then anything inside of
     * ${...} is a property name and should be loaded from ldap.properties in
     * the classpath.  An example might be referenceDN =
     * "${LdapOrganization.categoryDN}", where the property is defined as
     * <p/>
     * <pre>LdapOrganization.categoryDN=cn=?,ou=bus-categories,dc=example,dc=com</pre>
     * <p/>
     * If only a '?' bind parameter is given, then the attribute is assumed to
     * contain the entire DN, and no ldap attribute escaping will occur on the
     * attribute.
     * <p/>
     * TEST test reference DNs properly (issue-25)
     *
     * @return the DN reference with the bind variable ('?')
     */
    String referencedDN() default "";
    
    /**
     * If a field can not be nullified on load, set this.  Currently,
     * collections are cleared, and primitive fields are nullified.
     *
     * @return true by default, false if specified
     */
    boolean canBeNull() default true;
    
    /**
     * The method for clearing the instance, or creating a new empty one, if
     * canBeNull is set to false.  If not specified, and canBeNull is set to
     * false, a simple no args constructor call is made to create a new empty
     * instance.
     */
    String clear() default "";
}
