package com.adamcrume.jmxmon.view;

import static com.adamcrume.jmxmon.JMXMon.bundle;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
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

import com.adamcrume.jmxmon.telemetry.TelemetryComponent;
import com.adamcrume.jmxmon.telemetry.TelemetryFeed;

@SuppressWarnings("serial")
public final class JMXConfigView extends View {
    private JTextField descriptionField;

    private JTextField jmxURLField;

    private JTextField mbeanField;

    private JTextField attributeField;

    private TelemetryFeed model = ((TelemetryComponent) getManifestedComponent()).getModel();


    // create the GUI
    public JMXConfigView(AbstractComponent component, ViewInfo info) {
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

        JLabel descriptionLabel = new JLabel(bundle.getString("feed.description.label"));
        panel.add(descriptionLabel, gbcLabel);

        descriptionField = new JTextField();
        descriptionField.setText(model.getDescription());
        descriptionField.setToolTipText(bundle.getString("feed.description.tooltip"));
        descriptionLabel.setLabelFor(descriptionField);
        panel.add(descriptionField, gbcField);

        GridBagConstraints gbcButton = (GridBagConstraints) gbcField.clone();
        gbcButton.fill = GridBagConstraints.NONE;
        JButton saveButton = new JButton(bundle.getString("button.save"));
        panel.add(saveButton, gbcButton);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractComponent component = getManifestedComponent();
                model.setDescription(descriptionField.getText());
                component.save();
            }
        });

        add(panel);
    }


    @Override
    public void updateMonitoredGUI() {
        TelemetryFeed mr = ((TelemetryComponent) getManifestedComponent()).getModel();
        descriptionField.setText(mr.getDescription());
    }
}
