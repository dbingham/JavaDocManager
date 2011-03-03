package com.perniciouspenguins.ideaz.javadoc.templates.ui;

import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.VcsShowConfirmationOptionImpl;
import com.intellij.ui.DocumentAdapter;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.ConfirmationDialog;
import com.perniciouspenguins.ideaz.javadoc.templates.Template;
import com.perniciouspenguins.ideaz.javadoc.templates.TemplateManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.Iterator;
import java.util.Properties;

/**
 * Author: Raymond P. Brandon Date: Feb 11, 2007 Time: 6:29:28 PM
 */
public class JavaDocTemplateEditor
{
    private final Logger log = new DefaultLogger( "debug" );

    private JPanel mainPanel;
    private JEditorPane descriptionEditorPane;
    private JList javaDocTypeList;
    private JTextPane templateTextPane;
    private boolean dataChanged = false;
    private Template previousSelectedTemplate = null;
    private Template selectedTemplate = null;

    /**
     * Constructor JavaDocTemplateEditor creates a new JavaDocTemplateEditor instance.
     */
    public JavaDocTemplateEditor()
    {
        initComponents();
        addListeners();
    }

    /**
     * Method addListeners adds a selection listener to the list of templates.
     */
    private void addListeners()
    {
        javaDocTypeList.addListSelectionListener( new ListSelectionListener()
        {
            /**
             * Method valueChanged is called when the selection in the template
             * list changes.
             *
             * @param listSelectionEvent of type ListSelectionEvent
             */
            public void valueChanged( ListSelectionEvent listSelectionEvent )
            {
                if( dataChanged )
                {
                    if( showConfirmationDialog().isOK() )
                    {
                        applyChange();
                    }
                }
                JList list = (JList) listSelectionEvent.getSource();
                if( list.getSelectedIndex() != -1 )
                {
                    loadTemplateForSelection( (Template) list.getSelectedValue() );
                }
                dataChanged = false;
            }
        } );
    }

    /**
     * Method showConfirmationDialog shows a confirmation dialog when the user has made changes that were not yet
     * persisted.
     *
     * @return ConfirmationDialog the dialog.
     */
    private ConfirmationDialog showConfirmationDialog()
    {
        // Ask to apply changes
        ConfirmationDialog dialog = new ConfirmationDialog( ProjectManager.getInstance().getDefaultProject(),
                                                            "Save changes to template?",
                                                            "Template changed but not saved",
                                                            IconLoader.getIcon( "/general/questionDialog.png" ),
                                                            new VcsShowConfirmationOptionImpl( "", "", "", "", "" ) );
        dialog.setModal( true );
        dialog.show();
        return dialog;
    }

    /**
     * Method initComponents sets up all the components that form the UI.
     */
    private void initComponents()
    {
        fillJavaDocTypeList();
        templateTextPane.setFont( new Font( "Verdana", Font.PLAIN, 11 ) );

        StyledDocument styledDocument = new DefaultStyledDocument();
        templateTextPane.setDocument( styledDocument );
        Style keywordStyle = styledDocument.addStyle( "KeywordStyle", null );
        StyleConstants.setForeground( keywordStyle, Color.red );
        StyleConstants.setBold( keywordStyle, true );
        StyleConstants.setItalic( keywordStyle, true );

        Style noStyle = styledDocument.addStyle( "NoStyle", null );
        StyleConstants.setForeground( noStyle, Color.black );
        StyleConstants.setBold( noStyle, false );
        StyleConstants.setItalic( noStyle, false );

        styledDocument.addDocumentListener( new DocumentAdapter()
        {
            /**
             * @see com.intellij.ui.DocumentAdapter#textChanged(javax.swing.event.DocumentEvent)
             */
            protected void textChanged( DocumentEvent e )
            {
                if( e.getType() == DocumentEvent.EventType.INSERT ||
                    e.getType() == DocumentEvent.EventType.REMOVE )
                {
                    dataChanged = true;
                }
            }
        } );

        descriptionEditorPane.setEditable( false );
        descriptionEditorPane.setContentType( "text/html" );
    }

    /**
     * Method fillJavaDocTypeList builds the template list.
     */
    private void fillJavaDocTypeList()
    {
        DefaultListModel listModel = new DefaultListModel();

        TemplateManager templateManager = TemplateManager.getInstance();
        listModel.addElement( templateManager.loadTemplate( TemplateManager.TEMPLATE_CLASS ) );
        listModel.addElement( templateManager.loadTemplate( TemplateManager.TEMPLATE_CONSTRUCTOR ) );
        listModel.addElement( templateManager.loadTemplate( TemplateManager.TEMPLATE_GETTER_METHOD ) );
        listModel.addElement( templateManager.loadTemplate( TemplateManager.TEMPLATE_INTERFACE ) );
        listModel.addElement( templateManager.loadTemplate( TemplateManager.TEMPLATE_PLAIN_METHOD ) );
        listModel.addElement( templateManager.loadTemplate( TemplateManager.TEMPLATE_SETTER_METHOD ) );
        listModel.addElement( templateManager.loadTemplate( TemplateManager.TEMPLATE_METHOD_PARAM ) );
        listModel.addElement( templateManager.loadTemplate( TemplateManager.TEMPLATE_METHOD_RETURN_TYPE ) );
        listModel.addElement( templateManager.loadTemplate( TemplateManager.TEMPLATE_METHOD_THROWS ) );
        javaDocTypeList.setModel( listModel );
    }

    /**
     * Method loadTemplateForSelection loads the selected template in the template list.
     *
     * @param selectedTemplate of type Template
     */
    private void loadTemplateForSelection( Template selectedTemplate )
    {
        previousSelectedTemplate = this.selectedTemplate;
        this.selectedTemplate = selectedTemplate;
        descriptionEditorPane.setText( selectedTemplate.getDocumentation() );

        StyledDocument styledDocument = (StyledDocument) templateTextPane.getDocument();

        String text = selectedTemplate.getText();
        try
        {
            if( styledDocument.getLength() > 0 )
            {
                styledDocument.remove( 0, styledDocument.getLength() );
            }
            styledDocument.insertString( 0, text, styledDocument.getStyle( "NoStyle" ) );
        }
        catch( BadLocationException e )
        {
            log.error( e.getMessage() );
        }

        Properties tplProps = selectedTemplate.getProperties();
        for( Iterator<Object> iterator = tplProps.keySet().iterator(); iterator.hasNext(); )
        {
            String key = "${" + iterator.next() + "}";
            int keyStart = text.indexOf( key );

            while( keyStart > 0 && keyStart < text.length() )
            {
                styledDocument.setCharacterAttributes( keyStart, key.length(),
                                                       styledDocument.getStyle( "KeywordStyle" ), false );
                keyStart = text.indexOf( key, keyStart + key.length() + 1 );
            }
        }
    }

    /**
     * Method getMainPanel returns the mainPanel of this JavaDocTemplateEditor object.
     *
     * @return JPanel the mainPanel of this JavaDocTemplateEditor object.
     */
    public JPanel getMainPanel()
    {
        // This will trigger the template load
        javaDocTypeList.setSelectedIndex( 0 );
        dataChanged = false;
        return mainPanel;
    }

    /**
     * Method isDataChanged indicates whether the data has changed of this JavaDocTemplateEditor object.
     *
     * @return boolean true if the data has changed, false otherwise.
     */
    public boolean isDataChanged()
    {
        return dataChanged;
    }

    /**
     * Method applyChange persists changes to the selected template.
     */
    public void applyChange()
    {
        if( null != previousSelectedTemplate )
        {
            previousSelectedTemplate.setText( templateTextPane.getText() );
            TemplateManager.getInstance().saveTemplate( previousSelectedTemplate );
        }
        else
        {
            selectedTemplate.setText( templateTextPane.getText() );
            TemplateManager.getInstance().saveTemplate( selectedTemplate );
        }
        dataChanged = false;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        mainPanel = new JPanel();
        mainPanel.setLayout( new GridLayoutManager( 1, 1, new Insets( 5, 5, 5, 5 ), -1, -1 ) );
        mainPanel.setMinimumSize( new Dimension( 200, 100 ) );
        mainPanel.setPreferredSize( new Dimension( 450, 300 ) );
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation( 200 );
        splitPane1.setDividerSize( 5 );
        splitPane1.setEnabled( false );
        mainPanel.add( splitPane1,
                       new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                            GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                            GridConstraints.SIZEPOLICY_WANT_GROW,
                                            GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                            GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension( 200, 336 ), null,
                                            0, false ) );
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane2.setDividerLocation( 200 );
        splitPane2.setDividerSize( 5 );
        splitPane2.setEnabled( false );
        splitPane2.setOrientation( 0 );
        splitPane1.setRightComponent( splitPane2 );
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setAutoscrolls( true );
        scrollPane1.setHorizontalScrollBarPolicy( 30 );
        scrollPane1.setVerticalScrollBarPolicy( 20 );
        splitPane2.setLeftComponent( scrollPane1 );
        templateTextPane = new JTextPane();
        scrollPane1.setViewportView( templateTextPane );
        final JScrollPane scrollPane2 = new JScrollPane();
        splitPane2.setRightComponent( scrollPane2 );
        descriptionEditorPane = new JEditorPane();
        descriptionEditorPane.setText( "" );
        scrollPane2.setViewportView( descriptionEditorPane );
        javaDocTypeList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        defaultListModel1.addElement( "Interface" );
        defaultListModel1.addElement( "Class" );
        defaultListModel1.addElement( "Constructor" );
        defaultListModel1.addElement( "Getter method" );
        defaultListModel1.addElement( "Setter method" );
        defaultListModel1.addElement( "Method" );
        javaDocTypeList.setModel( defaultListModel1 );
        javaDocTypeList.setSelectionMode( 0 );
        splitPane1.setLeftComponent( javaDocTypeList );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return mainPanel;
    }
}
