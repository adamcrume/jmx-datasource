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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;

import java.util.Collection;

import com.adamcrume.jmxmon.telemetry.BeanDescriptorComponent;
import com.adamcrume.jmxmon.telemetry.JVMComponent;
import com.adamcrume.jmxmon.telemetry.TelemetryComponent;
import static com.adamcrume.jmxmon.JMXMon.bundle;

/**
 * Checks that a feed only has JVMs and MBean Descriptors as children, and only one of each.
 * @author Adam Crume
 */
public class FeedChildPolicy implements Policy {
    @Override
    public ExecutionResult execute(PolicyContext context) {
        char action = context.getProperty("ACTION", Character.class);
        if(action != 'w') {
            return new ExecutionResult(context, true, "");
        }

        AbstractComponent target = (AbstractComponent) context.getProperty("TARGET");
        if(!(target instanceof TelemetryComponent)) {
            return new ExecutionResult(context, true, "");
        }
        int jvmCount = 0;
        int beanCount = 0;
        for(AbstractComponent c : target.getComponents()) {
            if(c instanceof JVMComponent) {
                jvmCount++;
            } else if(c instanceof BeanDescriptorComponent) {
                beanCount++;
            }
        }

        @SuppressWarnings("unchecked")
        Collection<AbstractComponent> sources = (Collection<AbstractComponent>) context.getProperty("SOURCES");
        if(sources != null) {
            for(AbstractComponent source : sources) {
                if(source instanceof JVMComponent) {
                    jvmCount++;
                    if(jvmCount > 1) {
                        return fail(context, "error.oneJVMPerFeed");
                    }
                } else if(source instanceof BeanDescriptorComponent) {
                    beanCount++;
                    if(beanCount > 1) {
                        return fail(context, "error.oneBeanPerFeed");
                    }
                } else {
                    return fail(context, "error.wrongFeedChildType");
                }
            }
        }

        return new ExecutionResult(context, true, "");
    }


    private ExecutionResult fail(PolicyContext context, String msg) {
        return new ExecutionResult(context, false, bundle.getString(msg));
    }
}
