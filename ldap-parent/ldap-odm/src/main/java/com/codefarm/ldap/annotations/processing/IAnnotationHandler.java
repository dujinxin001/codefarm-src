package com.codefarm.ldap.annotations.processing;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;

import com.codefarm.ldap.LdapManager;

public interface IAnnotationHandler
{
    /**
     * Process the annotation on the given field.
     *
     * @param annotation the annotation
     * @param field      the field to process
    void processAnnotation(Annotation annotation, Field field);

    /**
     * Process the annotation on the given method.
     *
     * @param annotation the annotation
     * @param method     the field to process
    void processAnnotation(Annotation annotation, Method method);
     */
    
    /**
     * Process the annotation on the given annotatedClass.  This is a callback
     * to actually do something with the annotation.
     * <p/>
     * it is recommended that the implementing object keeps track of problems
     * and throws a RuntimeException in the {@link #validateProcessing()} method
     * if something went wrong.
     *
     * @param annotation     the annotation
     * @param annotatedClass the field to process
     *
     * @return true if annotation processing was successful
     *
     * @throws RuntimeException The implementor may throw a RuntimeException
     *                          from this method.  It is recommended that this
     *                          does not occur, unless it is severe.  First see
     *                          {@link #validateProcessing()}
     */
    boolean processAnnotation(final Annotation annotation,
            final Class annotatedClass);
    
    /**
     * It is expected that this handler has a reference to the object it wants
     * to operate on.  The {@link AnnotationProcessor } also needs access to
     * this object in order to determine annotations on it.
     *
     * @return the object to traverse the Class tree for.
     */
    Class getAnnotatedClass();
    
    /**
     * This is the primary Annotation (with target {@link ElementType#TYPE})
     * that this handler expects the annotated Class to be annotated with. If
     * the annotated class is annotated with this annotation, this indicates
     * that this handler supports the Class being processed.  If this handler
     * does not support the Class being processed, the handler will never be
     * called.
     *
     * @return the annotation class that this handler supports
     */
    Class<? extends Annotation> getAnnotationClass();
    
    /**
     * Called if no annotation on a particular class exists.  The handler can do
     * what it wants with it.  It may be that we need annotated classes at only
     * certain levels of the object tree, but not others.  So, the handler
     * could, for example, require that only the top level object has
     * annotations, while the super classes do not need them.  Or, perhaps it
     * requires it the other way around, for some reason.
     *
     * @param annotatedClass the class, in the object tree of {@link
     *                       #getAnnotatedClass}, that the annotation was NOT
     *                       found on.
     */
    void noAnnotation(final Class annotatedClass);
    
    /**
     * Method for validating the processing of the annotations.  This is called
     * after all classes in the class tree have been traversed and processed.
     * This is the final validation, and is meant for problems that could not be
     * detected until traversing the entire class tree.  For example, it may be
     * that you require a particular annotation on at least one class in the
     * hierarchy, but your handler doesn't know when the end of Class hierarchy
     * traversal is complete; this method is called after traversal completion.
     *
     * @throws RuntimeException if it is determined that a processing error has
     *                          occurred that could not be determined until
     *                          class hierarchy traversal is complete
     */
    void validateProcessing();
    
    /**
     * Sets the established ldap manager object, which should be
     * pre-authenticated.
     *
     * @param managerInstance the already authenticated manager
     */
    void setManager(final LdapManager managerInstance);
}
