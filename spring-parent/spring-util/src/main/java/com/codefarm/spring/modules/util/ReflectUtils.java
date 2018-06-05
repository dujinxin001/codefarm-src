/*
 * @(#)ReflectUtils.java 2014年1月1日 下午23:33:33
 *
 * Copyright (c) 2011-2014 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package com.codefarm.spring.modules.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class description goes here.
 */
public class ReflectUtils {

	public static List<Method> findMethodsAnnotatedWith(final Class<?> type,
			final Class<? extends Annotation> annotation) {
		List<Method> methods = new ArrayList<Method>();
		Class<?> klass = type;
		while (klass != null && klass != Object.class) {
			final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getMethods()));
			for (final Method method : allMethods) {
				if (annotation == null || method.isAnnotationPresent(annotation)) {
					// Annotation annotInstance =
					// method.getAnnotation(annotation);
					// TODO process annotInstance
					methods.add(method);
				}
			}
			// move to the upper class in the hierarchy in search for more
			// methods
			klass = klass.getSuperclass();
		}
		return methods;
	}

	public static List<Field> findFieldsAnnotatedWith(final Class<?> type,
			final Class<? extends Annotation> annotation) {
		List<Field> fields = new ArrayList<Field>();
		Class<?> klass = type;
		while (klass != null && klass != Object.class) { // need to iterated
															// thought hierarchy
															// in order to
															// retrieve
															// methods from
															// above the current
															// instance
															// iterate though
															// the list of
															// methods declared
															// in the class
															// represented by
															// klass
															// variable, and add
															// those annotated
															// with the
															// specified
															// annotation
			final List<Field> allFields = new ArrayList<Field>(Arrays.asList(klass.getDeclaredFields()));
			for (final Field field : allFields) {
				if (annotation == null || field.isAnnotationPresent(annotation)) {
					// Annotation annotInstance =
					// method.getAnnotation(annotation);
					// TODO process annotInstance
					fields.add(field);
				}
			}
			// move to the upper class in the hierarchy in search for more
			// fields
			klass = klass.getSuperclass();
		}
		return fields;
	}
}
