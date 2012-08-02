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
package com.adamcrume.jmxmon.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.management.openmbean.CompositeData;

public class CompositeDataResolver extends ELResolver {
    @Override
    public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if(base instanceof CompositeData) {
            context.setPropertyResolved(true);
            return ((CompositeData) base).get((String) property);
        }
        context.setPropertyResolved(false);
        return null;
    }


    @Override
    public Class<?> getType(ELContext arg0, Object arg1, Object arg2) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Class<?> getCommonPropertyType(ELContext arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }
}
