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
package com.adamcrume.jmxmon.actions;

import static com.adamcrume.jmxmon.JMXMon.bundle;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.View;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;

import javax.management.MalformedObjectNameException;

import com.adamcrume.jmxmon.telemetry.BeanDescriptorComponent;
import com.adamcrume.jmxmon.telemetry.DataPoller;
import com.adamcrume.jmxmon.telemetry.JVMComponent;
import com.adamcrume.jmxmon.telemetry.TelemetryComponent;

@SuppressWarnings("serial")
public class ActivateAction extends ContextAwareAction {
    private Collection<View> selectedManifestations;


    public ActivateAction() {
        this(bundle.getString("action.activate.name"));
    }


    protected ActivateAction(String name) {
        super(name);
    }


    private boolean isActivatable(AbstractComponent c) {
        if(!(c instanceof TelemetryComponent)) {
            return false;
        }
        int jvmCount = 0;
        int beanCount = 0;
        int otherCount = 0;
        for(AbstractComponent child : c.getComponents()) {
            if(child instanceof JVMComponent) {
                jvmCount++;
            } else if(child instanceof BeanDescriptorComponent) {
                beanCount++;
            } else {
                otherCount++;
            }
        }
        return jvmCount == 1 && beanCount == 1 && otherCount == 0;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        DataPoller dataPoller = DataPoller.getInstance();
        int skippedFeedCount = 0;
        for(View view : selectedManifestations) {
            AbstractComponent manifestedComponent = view.getManifestedComponent();
            if(manifestedComponent instanceof TelemetryComponent) {
                if(isActivatable(manifestedComponent)) {
                    TelemetryComponent component = (TelemetryComponent) manifestedComponent;
                    try {
                        dataPoller.start(component);
                    } catch(MalformedObjectNameException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch(IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    skippedFeedCount++;
                }
            }
        }
        if(skippedFeedCount > 0) {
            OptionBox.showMessageDialog(null, bundle.getString("warning.feedsNotActivated"), bundle
                    .getString("warning.feedsNotActivated.title"), OptionBox.WARNING_MESSAGE);
        }
        selectedManifestations = null;
    }


    @Override
    public boolean canHandle(ActionContext context) {
        selectedManifestations = context.getSelectedManifestations();
        for(View view : selectedManifestations) {
            if(isActivatable(view.getManifestedComponent())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
