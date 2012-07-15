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
package com.adamcrume.jmxmon.telemetry;

import static com.adamcrume.jmxmon.JMXMon.bundle;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.JAXBModelStatePersistence;
import gov.nasa.arc.mct.components.ModelStatePersistence;
import gov.nasa.arc.mct.components.PropertyDescriptor;
import gov.nasa.arc.mct.components.PropertyDescriptor.VisualControlDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.adamcrume.jmxmon.view.StringPropertyEditor;

public class JVMComponent extends AbstractComponent {
    private AtomicReference<JVM> model = new AtomicReference<JVM>(new JVM());


    @Override
    public boolean isLeaf() {
        return true;
    }


    @Override
    protected <T> T handleGetCapability(Class<T> capability) {
        if(ModelStatePersistence.class.isAssignableFrom(capability)) {
            JAXBModelStatePersistence<JVM> persistence = new JAXBModelStatePersistence<JVM>() {
                @Override
                protected JVM getStateToPersist() {
                    return model.get();
                }


                @Override
                protected void setPersistentState(JVM modelState) {
                    model.set(modelState);
                }


                @Override
                protected Class<JVM> getJAXBClass() {
                    return JVM.class;
                }
            };
            return capability.cast(persistence);
        }
        return null;
    }


    public JVM getModel() {
        return model.get();
    }


    @Override
    public List<PropertyDescriptor> getFieldDescriptors() {
        List<PropertyDescriptor> fields = new ArrayList<PropertyDescriptor>();
        JVM model = getModel();

        // TODO: Add validation
        PropertyDescriptor jmxURL = new PropertyDescriptor(bundle.getString("jvm.jmxURL.label"),
                new StringPropertyEditor(model, "jmxURL"), VisualControlDescriptor.TextField);
        jmxURL.setFieldMutable(true);
        fields.add(jmxURL);

        return fields;
    }
}
