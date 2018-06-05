package com.codefarm.ldap.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LdapEntity
{
    /**
     * The object classes that must be present for loading of this object to be
     * successful.
     * <p/>
     * e.g. organization, labeledURIObject, person, inetOrgPerson,
     * organizationalPerson, etc
     *
     * @return the String array of objectClasses
     */
    String[] requiredObjectClasses() default {};
}
