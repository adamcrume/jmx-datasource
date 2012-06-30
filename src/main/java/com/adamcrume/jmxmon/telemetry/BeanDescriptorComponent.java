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

import com.adamcrume.jmxmon.component.StringPropertyEditor;

public class BeanDescriptorComponent extends AbstractComponent {
    private AtomicReference<BeanDescriptor> model = new AtomicReference<BeanDescriptor>(new BeanDescriptor());


    @Override
    public boolean isLeaf() {
        return true;
    }


    @Override
    protected <T> T handleGetCapability(Class<T> capability) {
        if(ModelStatePersistence.class.isAssignableFrom(capability)) {
            JAXBModelStatePersistence<BeanDescriptor> persistence = new JAXBModelStatePersistence<BeanDescriptor>() {
                @Override
                protected BeanDescriptor getStateToPersist() {
                    return model.get();
                }


                @Override
                protected void setPersistentState(BeanDescriptor modelState) {
                    model.set(modelState);
                }


                @Override
                protected Class<BeanDescriptor> getJAXBClass() {
                    return BeanDescriptor.class;
                }
            };
            return capability.cast(persistence);
        }
        return null;
    }


    public BeanDescriptor getModel() {
        return model.get();
    }


    @Override
    public List<PropertyDescriptor> getFieldDescriptors() {
        List<PropertyDescriptor> fields = new ArrayList<PropertyDescriptor>();
        BeanDescriptor model = getModel();

        // TODO: Add validation
        PropertyDescriptor bean = new PropertyDescriptor(bundle.getString("bean_descriptor.bean.label"),
                new StringPropertyEditor(model, "bean"), VisualControlDescriptor.TextField);
        bean.setFieldMutable(true);
        fields.add(bean);

        // TODO: Add validation
        PropertyDescriptor attribute = new PropertyDescriptor(bundle.getString("bean_descriptor.attribute.label"),
                new StringPropertyEditor(model, "attribute"), VisualControlDescriptor.TextField);
        attribute.setFieldMutable(true);
        fields.add(attribute);

        return fields;
    }
}
