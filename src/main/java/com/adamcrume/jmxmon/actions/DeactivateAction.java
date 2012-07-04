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
import gov.nasa.arc.mct.gui.View;

import java.awt.event.ActionEvent;
import java.util.Collection;

import com.adamcrume.jmxmon.telemetry.DataPoller;
import com.adamcrume.jmxmon.telemetry.TelemetryComponent;

@SuppressWarnings("serial")
public class DeactivateAction extends ContextAwareAction {
    private Collection<View> selectedManifestations;


    public DeactivateAction() {
        this(bundle.getString("action.deactivate.name"));
    }


    protected DeactivateAction(String name) {
        super(name);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        DataPoller dataPoller = DataPoller.getInstance();
        for(View view : selectedManifestations) {
            AbstractComponent manifestedComponent = view.getManifestedComponent();
            if(manifestedComponent instanceof TelemetryComponent) {
                TelemetryComponent component = (TelemetryComponent) manifestedComponent;
                dataPoller.stop(component);
            }
        }
        selectedManifestations = null;
    }


    @Override
    public boolean canHandle(ActionContext context) {
        selectedManifestations = context.getSelectedManifestations();
        for(View view : selectedManifestations) {
            if(view.getManifestedComponent() instanceof TelemetryComponent) {
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
