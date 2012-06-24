package com.adamcrume.jmxmon.component;

import gov.nasa.arc.mct.components.PropertyEditor;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;


public final class StringPropertyEditor implements PropertyEditor<Object> {
    private Object model;

	private String fieldName;


	public StringPropertyEditor(Object model, String fieldName) {
        this.model = model;
		this.fieldName = fieldName;
	}


	private PropertyDescriptor getDescriptor(Object o, String field) throws IntrospectionException {
		for(PropertyDescriptor d : Introspector.getBeanInfo(o.getClass()).getPropertyDescriptors()) {
			if(d.getName().equals(field)) {
				return d;
			}
		}
		return null;
	}


	@Override
	public String getAsText() {
		try {
			PropertyDescriptor d = getDescriptor(model, fieldName);
			if(d == null) {
				throw new RuntimeException("Field not found: " + fieldName);
			}
			return (String) d.getReadMethod().invoke(model);
		} catch(IntrospectionException e) {
			throw new RuntimeException(e);
		} catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch(InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void setAsText(String newValue) throws IllegalArgumentException {
		try {
			PropertyDescriptor d = getDescriptor(model, fieldName);
			if(d == null) {
				throw new RuntimeException("Field not found: " + fieldName);
			}
			d.getWriteMethod().invoke(model, newValue);
		} catch(IntrospectionException e) {
			throw new RuntimeException(e);
		} catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch(InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public String getValue() {
		throw new UnsupportedOperationException();
	}


	@Override
	public void setValue(Object selection) {
		throw new UnsupportedOperationException();
	}


	@Override
	public List<Object> getTags() {
		throw new UnsupportedOperationException();
	}
}
