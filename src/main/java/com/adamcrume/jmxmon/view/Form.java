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

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class Form {
    private static final Color defaultTextBackground = new JTextField().getBackground();

    private Map<JComponent, List<Validator>> validators = new HashMap<JComponent, List<Validator>>();

    private Set<JComponent> invalidFields = new HashSet<JComponent>();

    private JButton saveButton;

    private KeyListener keyListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
            validateField((JComponent) e.getSource());
        }


        @Override
        public void keyReleased(KeyEvent e) {
            validateField((JComponent) e.getSource());
        }


        @Override
        public void keyPressed(KeyEvent e) {
            validateField((JComponent) e.getSource());
        }
    };


    public void addValidator(JComponent field, Validator validator) {
        List<Validator> list = validators.get(field);
        if(list == null) {
            list = new ArrayList<Validator>();
            validators.put(field, list);
            field.addKeyListener(keyListener);
        }
        list.add(validator);
    }


    protected void validateField(JComponent field) {
        List<Validator> list = validators.get(field);
        if(list == null) {
            return;
        }
        boolean valid = true;
        for(Validator validator : list) {
            if(validator.validate(field) != null) {
                valid = false;
                break;
            }
        }
        if(valid) {
            invalidFields.remove(field);
        } else {
            invalidFields.add(field);
        }
        if(field instanceof JTextField) {
            field.setBackground(valid ? defaultTextBackground : Color.red);
        }
        validateForm();
    }


    private void validateForm() {
        saveButton.setEnabled(invalidFields.isEmpty());
    }


    public JButton getSaveButton() {
        return saveButton;
    }


    public void setSaveButton(JButton saveButton) {
        this.saveButton = saveButton;
    }
}
