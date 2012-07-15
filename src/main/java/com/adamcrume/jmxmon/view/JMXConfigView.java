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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.adamcrume.jmxmon.telemetry.TelemetryComponent;
import com.adamcrume.jmxmon.telemetry.TelemetryFeed;

@SuppressWarnings("serial")
public final class JMXConfigView extends View {
    private JTextField descriptionField;

    private JTextField pollingIntervalField;

    private TelemetryFeed model = ((TelemetryComponent) getManifestedComponent()).getModel();


    // create the GUI
    public JMXConfigView(AbstractComponent component, ViewInfo info) {
        super(component, info);

        final JPanel panel = new JPanel();
        Form form = new Form();
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

        JLabel descriptionLabel = new JLabel(bundle.getString("feed.description.label"));
        panel.add(descriptionLabel, gbcLabel);
        descriptionField = new JTextField();
        descriptionField.setToolTipText(bundle.getString("feed.description.tooltip"));
        descriptionLabel.setLabelFor(descriptionField);
        panel.add(descriptionField, gbcField);

        JLabel pollingIntervalLabel = new JLabel(bundle.getString("feed.pollingInterval.label"));
        panel.add(pollingIntervalLabel, gbcLabel);
        pollingIntervalField = new JTextField();
        pollingIntervalField.setToolTipText(bundle.getString("feed.pollingInterval.tooltip"));
        pollingIntervalLabel.setLabelFor(pollingIntervalField);
        form.addValidator(pollingIntervalField, new Validator() {
            @Override
            public String validate(JComponent field) {
                String s = ((JTextField) field).getText();
                if("".equals(s)) {
                    return "Polling interval is required"; // TODO: I18N
                }
                long val;
                try {
                    val = Long.parseLong(s);
                } catch(NumberFormatException e) {
                    return "Polling interval must be an integer";
                }
                if(val <= 0) {
                    return "Polling interval must be positive";
                }
                // TODO: This check is purely to keep the user from shooting him/herself in the foot by flooding the server.  Make it a warning, or make it disable-able.
                if(val < 100) {
                    return "Polling interval must be at least 100ms";
                }
                return null;
            }
        });
        panel.add(pollingIntervalField, gbcField);

        GridBagConstraints gbcButton = (GridBagConstraints) gbcField.clone();
        gbcButton.fill = GridBagConstraints.NONE;
        JButton saveButton = new JButton(bundle.getString("button.save"));
        panel.add(saveButton, gbcButton);
        form.setSaveButton(saveButton);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final AbstractComponent component = getManifestedComponent();
                model.setDescription(descriptionField.getText());
                model.setPollingInterval(Long.valueOf(pollingIntervalField.getText()));

                PersistenceProvider provider = PlatformAccess.getPlatform().getPersistenceProvider();
                boolean successfulAction = false;
                try {
                    provider.startRelatedOperations();
                    component.save();
                    successfulAction = true;
                } catch (OptimisticLockException e2) {
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
        descriptionField.setText(model.getDescription());
        pollingIntervalField.setText(String.valueOf(model.getPollingInterval()));
    }
}
