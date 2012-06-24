package com.adamcrume.jmxmon.actions;

import static com.adamcrume.jmxmon.JMXMon.bundle;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.OptionBox;

import java.awt.Component;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class AboutAction extends ContextAwareAction {
    public AboutAction() {
        super(bundle.getString("about_text"));
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        OptionBox.showMessageDialog((Component) e.getSource(), bundle.getString("about_message"));
    }


    @Override
    public boolean canHandle(ActionContext context) {
        return true;
    }


    @Override
    public boolean isEnabled() {
        return true;
    }
}
