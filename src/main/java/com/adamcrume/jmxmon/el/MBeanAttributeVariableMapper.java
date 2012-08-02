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

import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


public class MBeanAttributeVariableMapper extends VariableMapper {
    private MBeanServerConnection connection;

    private ObjectName mbean;


    public MBeanAttributeVariableMapper(MBeanServerConnection connection, ObjectName mbean) {
        this.connection = connection;
        this.mbean = mbean;
    }


    @Override
    public ValueExpression setVariable(String arg0, ValueExpression arg1) {
        throw new UnsupportedOperationException();
    }


    @Override
    public ValueExpression resolveVariable(String name) {
        return new MBeanAttributeValueExpression(connection, mbean, name);
    }
}
