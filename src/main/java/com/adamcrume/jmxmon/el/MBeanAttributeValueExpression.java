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

import java.io.IOException;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class MBeanAttributeValueExpression extends ValueExpression {
    private MBeanServerConnection connection;

    private ObjectName mbean;

    private String name;


    public MBeanAttributeValueExpression(MBeanServerConnection connection, ObjectName mbean, String name) {
        this.connection = connection;
        this.mbean = mbean;
        this.name = name;
    }


    @Override
    public Class<?> getExpectedType() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Class<?> getType(ELContext arg0) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object getValue(ELContext arg0) {
        try {
            return connection.getAttribute(mbean, name);
        } catch(AttributeNotFoundException e) {
            throw new ELException(e);
        } catch(InstanceNotFoundException e) {
            throw new ELException(e);
        } catch(MBeanException e) {
            throw new ELException(e);
        } catch(ReflectionException e) {
            throw new ELException(e);
        } catch(IOException e) {
            throw new ELException(e);
        }
    }


    @Override
    public boolean isReadOnly(ELContext arg0) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setValue(ELContext arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean equals(Object arg0) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getExpressionString() {
        throw new UnsupportedOperationException();
    }


    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isLiteralText() {
        return false;
    }
}
