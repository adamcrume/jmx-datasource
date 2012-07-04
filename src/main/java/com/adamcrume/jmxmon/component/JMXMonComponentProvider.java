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

import static com.adamcrume.jmxmon.JMXMon.bundle;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.adamcrume.jmxmon.actions.AboutAction;
import com.adamcrume.jmxmon.actions.ActivateAction;
import com.adamcrume.jmxmon.actions.DeactivateAction;
import com.adamcrume.jmxmon.telemetry.BeanDescriptorComponent;
import com.adamcrume.jmxmon.telemetry.JVMComponent;
import com.adamcrume.jmxmon.telemetry.TelemetryComponent;
import com.adamcrume.jmxmon.view.BeanDescriptorEditView;
import com.adamcrume.jmxmon.view.JMXConfigView;
import com.adamcrume.jmxmon.view.JVMEditView;

public class JMXMonComponentProvider extends AbstractComponentProvider {
    private final ComponentTypeInfo telemetryComponentType =
        new ComponentTypeInfo(
                bundle.getString("component.feed.name"),
                bundle.getString("component.feed.description"),
                TelemetryComponent.class);

    private final ComponentTypeInfo jvmComponentType =
        new ComponentTypeInfo(
                bundle.getString("component.jvm.name"),
                bundle.getString("component.jvm.description"),
                JVMComponent.class);

    private final ComponentTypeInfo beanDescriptorComponentType =
        new ComponentTypeInfo(
                bundle.getString("component.bean_descriptor.name"),
                bundle.getString("component.bean_descriptor.description"),
                BeanDescriptorComponent.class);


    @Override
    public Collection<ComponentTypeInfo> getComponentTypes() {
        return Arrays.asList(
                telemetryComponentType,
                jvmComponentType,
                beanDescriptorComponentType
        );
    }


    @Override
    public Collection<ViewInfo> getViews(String componentTypeId) {
        if(componentTypeId.equals(TelemetryComponent.class.getName())) {
            return Arrays.asList(
                    new ViewInfo(JMXConfigView.class, bundle.getString("view.feededit.title"), ViewType.OBJECT)
            );
        } else if(componentTypeId.equals(JVMComponent.class.getName())) {
            return Arrays.asList(
                    new ViewInfo(JVMEditView.class, bundle.getString("view.jvmedit.title"), ViewType.OBJECT)
            );
        } else if(componentTypeId.equals(BeanDescriptorComponent.class.getName())) {
            return Arrays.asList(
                    new ViewInfo(BeanDescriptorEditView.class, bundle.getString("view.bean_descriptor_edit.title"), ViewType.OBJECT)
            );
        }
        return Collections.emptyList();
    }


    @Override
    public Collection<MenuItemInfo> getMenuItemInfos() {
        return Arrays.asList(
                new MenuItemInfo("/help/additions", // NOI18N
                        "ABOUT_JMXMON_ACTION", //NO18N
                        MenuItemType.NORMAL, AboutAction.class),
                new MenuItemInfo(
                        "/objects/additions", // NOI18N
                        "ACTIVATE_ACTION", //NO18N
                        MenuItemType.NORMAL,
                        ActivateAction.class),
                new MenuItemInfo(
                        "/objects/additions", // NOI18N
                        "DEACTIVATE_ACTION", //NO18N
                        MenuItemType.NORMAL,
                        DeactivateAction.class)
        );
    }
}
