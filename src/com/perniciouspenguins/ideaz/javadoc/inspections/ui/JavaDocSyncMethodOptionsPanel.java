package com.perniciouspenguins.ideaz.javadoc.inspections.ui;

import com.perniciouspenguins.ideaz.javadoc.inspections.MissingMethodJavaDocInspection;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class JavaDocSyncMethodOptionsPanel
 *
 * @author Raymond P. Brandon
 * @version $id:$
 * Created on Jul 4, 2007
 */
public class JavaDocSyncMethodOptionsPanel extends JPanel {
    public static final String METHOD_ACCESS_PUBLIC    = "public";
    public static final String METHOD_ACCESS_PROTECTED = "protected";
    public static final String METHOD_ACCESS_DEFAULT   = "default";
    public static final String METHOD_ACCESS_PRIVATE   = "private";

    public static final String[] detectionLevels = new String[]{
            METHOD_ACCESS_PUBLIC,
            METHOD_ACCESS_PROTECTED,
            METHOD_ACCESS_DEFAULT,
            METHOD_ACCESS_PRIVATE};

    private MissingMethodJavaDocInspection owner = null;

    /**
     * Constructor JavaDocSyncMethodOptionsPanel creates a new JavaDocSyncMethodOptionsPanel instance.
     * @param owner of type JavaDocBaseInspection
     */
    public JavaDocSyncMethodOptionsPanel(MissingMethodJavaDocInspection owner) {
        super();
        this.owner = owner;

        initComponents();
    }

    /**
     * Method initComponents initializes the UI components
     */
    private void initComponents() {
        setLayout(new GridBagLayout());
        Border lineBorder = BorderFactory.createLineBorder(Color.black);
        setBorder(BorderFactory.createTitledBorder(lineBorder, "Detection level settings"));

        JLabel methodDetectionLabel = new JLabel("Method detection level");
        JComboBox methodDetectionLevel = new JComboBox(detectionLevels);
        methodDetectionLevel.addActionListener(new ActionListener() {
            /**
             * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
             */
            public void actionPerformed(ActionEvent actionEvent) {
                JComboBox source = (JComboBox) actionEvent.getSource();
                if (source.getSelectedIndex() > -1) {
                    owner.methodDetectionLevel = (String) source.getSelectedItem();
                }
            }
        });
        JTextArea textArea = new JTextArea(
                "Selected value includes methods with " +
                "a wider access modifier, so 'public' " +
                "is only 'public', 'protected' is " +
                "'public' and 'protected', default is " +
                "'public', 'protected' and methods " +
                "with 'default' scope etc.");
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setRows(6);
        textArea.setBackground(getBackground());
        textArea.setFont(methodDetectionLabel.getFont());

        JCheckBox checkAnonymousClasses = new JCheckBox("Inspect methods of anonymous classes");
        checkAnonymousClasses.addChangeListener(new ChangeListener() {
            /**
             * @see javax.swing.event.ChangeListener
             */
            public void stateChanged(ChangeEvent changeEvent) {
                JCheckBox source = (JCheckBox) changeEvent.getSource();
                owner.checkAnonymousClasses = source.isSelected();
            }
        });

        JCheckBox useSingleLineReferences = new JCheckBox("Generate single line JavaDoc references");
        useSingleLineReferences.addChangeListener(new ChangeListener() {
            /**
             * @see javax.swing.event.ChangeListener
             */
            public void stateChanged(ChangeEvent changeEvent) {
                JCheckBox source = (JCheckBox) changeEvent.getSource();
                owner.useSingleLineReferences = source.isSelected();
            }
        });

        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        add(methodDetectionLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 0, 10);
        add(methodDetectionLevel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        add(textArea, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10);
        add(checkAnonymousClasses, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 10, 10);
        add(useSingleLineReferences, gridBagConstraints);

        checkAnonymousClasses.setSelected(owner.checkAnonymousClasses);
        useSingleLineReferences.setSelected(owner.useSingleLineReferences);
        String detectionLevel = owner.methodDetectionLevel;

        if (null == detectionLevel) {
            detectionLevel = METHOD_ACCESS_PRIVATE; // Everything, including private methods
        }
        methodDetectionLevel.setSelectedItem(detectionLevel);
    }
}
