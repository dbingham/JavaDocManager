package com.perniciouspenguins.ideaz.javadoc.inspections.ui;

import com.perniciouspenguins.ideaz.javadoc.inspections.MissingClassJavaDocInspection;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Class JavaDocSyncMethodOptionsPanel
 *
 * @author Raymond P. Brandon
 * @version $id:$
 * Created on Jul 4, 2007
 */
public class JavaDocSyncClassOptionsPanel extends JPanel {

    /**
     * Constructor JavaDocSyncMethodOptionsPanel creates a new JavaDocSyncMethodOptionsPanel instance.
     */
    public JavaDocSyncClassOptionsPanel() {
        super();
        initComponents();
    }

    /**
     * Method initComponents initializes the UI components
     */
    private void initComponents() {
        setLayout(new GridBagLayout());
        Border lineBorder = BorderFactory.createLineBorder(Color.black);
        setBorder(BorderFactory.createTitledBorder(lineBorder, "Detection level settings"));

        JCheckBox checkInnerClasses = new JCheckBox("Inspect inner classes");
        checkInnerClasses.addChangeListener(new ChangeListener() {
            /**
             * @inheritDoc javax.swing.event.ChangeListener(ChangeEvent)
             */
            public void stateChanged(ChangeEvent changeEvent) {
                JCheckBox source = (JCheckBox) changeEvent.getSource();
                MissingClassJavaDocInspection.checkInnerClasses = source.isSelected();
            }
        });
        checkInnerClasses.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                JCheckBox source = (JCheckBox) propertyChangeEvent.getSource();
                if (!source.isEnabled()) {
                    MissingClassJavaDocInspection.checkInnerClasses = false;
                }
            }
        });

        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 0, 10);
        add(checkInnerClasses, gridBagConstraints);

        checkInnerClasses.setSelected(MissingClassJavaDocInspection.checkInnerClasses);
    }
}
