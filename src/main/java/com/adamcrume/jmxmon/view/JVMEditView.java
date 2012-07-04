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
package com.adamcrume.jmxmon.view;

import static com.adamcrume.jmxmon.JMXMon.bundle;
import gov.nasa.arc.mct.api.persistence.OptimisticLockException;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.adamcrume.jmxmon.telemetry.JVM;
import com.adamcrume.jmxmon.telemetry.JVMComponent;

@SuppressWarnings("serial")
public final class JVMEditView extends View {
    private JTextField jmxURLField;

    private JVM jvm = ((JVMComponent) getManifestedComponent()).getModel();


    public JVMEditView(AbstractComponent component, ViewInfo info) {
        super(component, info);

        final JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.anchor = GridBagConstraints.EAST;
        gbcLabel.weightx = 0;
        gbcLabel.weighty = 0;
        gbcLabel.fill = GridBagConstraints.NONE;
        gbcLabel.insets = new Insets(5, 5, 0, 0);
        GridBagConstraints gbcField = (GridBagConstraints) gbcLabel.clone();
        gbcField.gridwidth = GridBagConstraints.REMAINDER;
        gbcField.weightx = 1;
        gbcField.fill = GridBagConstraints.HORIZONTAL;

        JLabel jmxURLLabel = new JLabel(bundle.getString("jvm.jmxURL.label"));
        panel.add(jmxURLLabel, gbcLabel);

        jmxURLField = new JTextField();
        jmxURLField.setToolTipText(bundle.getString("jvm.jmxURL.tooltip"));
        jmxURLLabel.setLabelFor(jmxURLField);
        panel.add(jmxURLField, gbcField);

        GridBagConstraints gbcButton = (GridBagConstraints) gbcField.clone();
        gbcButton.fill = GridBagConstraints.NONE;
        JButton saveButton = new JButton(bundle.getString("button.save"));
        panel.add(saveButton, gbcButton);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractComponent component = getManifestedComponent();
                jvm.setJmxURL(jmxURLField.getText());

                PersistenceProvider provider = PlatformAccess.getPlatform().getPersistenceProvider();
                boolean successfulAction = false;
                try {
                    provider.startRelatedOperations();
                    component.save();
                    successfulAction = true;
                } catch(OptimisticLockException e2) {
                    e2.printStackTrace(); // TODO
                } finally {
                    provider.completeRelatedOperations(successfulAction);
                }
            }
        });

        add(panel);
        updateMonitoredGUI();
    }


    @Override
    public void updateMonitoredGUI() {
        JVM jvm = ((JVMComponent) getManifestedComponent()).getModel();
        jmxURLField.setText(jvm.getJmxURL());
    }
}
