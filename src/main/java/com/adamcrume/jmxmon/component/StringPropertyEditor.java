/*******************************************************************************
 * Copyright 2012 Adam Crume
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
