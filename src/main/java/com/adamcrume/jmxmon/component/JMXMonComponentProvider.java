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
import com.adamcrume.jmxmon.telemetry.TelemetryComponent;
import com.adamcrume.jmxmon.view.JMXConfigView;

public class JMXMonComponentProvider extends AbstractComponentProvider {
    private final ComponentTypeInfo telemetryComponentType =
        new ComponentTypeInfo(
                bundle.getString("component.feed.name"),
                bundle.getString("component.feed.description"),
                TelemetryComponent.class);


    @Override
    public Collection<ComponentTypeInfo> getComponentTypes() {
        return Arrays.asList(
                telemetryComponentType
        );
    }


    @Override
    public Collection<ViewInfo> getViews(String componentTypeId) {
        if(componentTypeId.equals(TelemetryComponent.class.getName())) {
            return Arrays.asList(
                    new ViewInfo(JMXConfigView.class, bundle.getString("view.feededit.title"), ViewType.OBJECT)
            );
        }
        return Collections.emptyList();
    }


    @Override
    public Collection<MenuItemInfo> getMenuItemInfos() {
        return Arrays.asList(
                new MenuItemInfo("/help/additions", // NOI18N
                        "ABOUT_JMXMON_ACTION", //NO18N
                        MenuItemType.NORMAL, AboutAction.class)
        );
    }
}
