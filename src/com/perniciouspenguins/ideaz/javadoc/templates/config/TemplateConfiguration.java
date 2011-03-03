package com.perniciouspenguins.ideaz.javadoc.templates.config;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.xmlb.XmlSerializationException;
import com.intellij.util.xmlb.XmlSerializer;
import com.perniciouspenguins.ideaz.javadoc.templates.ui.JavaDocTemplateEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;

/**
 * Author: Raymond P. Brandon Date: Feb 18, 2007 Time: 9:03:09 PM
 */
@State(
        name = TemplateConfiguration.COMPONENT_NAME,
        storages = {@Storage(
            id = "JavaDoc",
            file = "$APP_CONFIG$/JavaDocManager.xml"
        )}
)
public class TemplateConfiguration implements ApplicationComponent, Configurable, PersistentStateComponent<Element>
{
    private Logger log = new DefaultLogger( "JavaDocManager" );
    private ImageIcon icon = null;
    private JavaDocTemplateEditor javaDocTemplateEditor = null;

    public static final String COMPONENT_NAME="JavaDocManager.TemplateConfiguration";

    /**
     * Constructor TemplateConfiguration creates a new TemplateConfiguration instance.
     */
    public TemplateConfiguration()
    {
        if( icon == null )
        {
            try
            {
                icon = new ImageIcon(
                        ImageIO.read( getClass().getClassLoader().getResourceAsStream( "images/jds.png" ) ) );
            }
            catch( IOException e )
            {
                log.error( "Failed to load icon: " + e.getMessage(), e );
            }
        }
    }

    /**
     * Method initComponent ...
     */
    public void initComponent()
    {
    }

    /**
     * Method disposeComponent handles the cleanup of the template related objects.
     */
    public void disposeComponent()
    {
        icon = null;
        javaDocTemplateEditor = null;
    }

    /**
     * Method getComponentName returns the componentName of this TemplateConfiguration object.
     *
     * @return the componentName (type String) of this TemplateConfiguration object.
     */
    @NotNull
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }

    /**
     * Returns the user-visible name of the settings component.
     *
     * @return the visible name of the component.
     */
    public String getDisplayName()
    {
        return "JavaDoc Manager Settings";
    }

    /**
     * Returns the icon representing the settings component. Components shown in the IDEA settings dialog have 32x32
     * icons.
     *
     * @return the icon for the component.
     */
    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Returns the topic in the help file which is shown when help for the configurable is requested.
     *
     * @return the help topic, or null if no help is available.
     */
    @Nullable
    @NonNls
    public String getHelpTopic()
    {
        return null;
    }

    /**
     * Returns the user interface component for editing the configuration.
     *
     * @return the component instance.
     */
    public JComponent createComponent()
    {
        if( null == javaDocTemplateEditor )
        {
            javaDocTemplateEditor = new JavaDocTemplateEditor();
        }
        return javaDocTemplateEditor.getMainPanel();
    }

    /**
     * Checks if the settings in the user interface component were modified by the user and need to be saved.
     *
     * @return true if the settings were modified, false otherwise.
     */
    public boolean isModified()
    {
        return null != javaDocTemplateEditor && javaDocTemplateEditor.isDataChanged();
    }

    /**
     * Store the settings from configurable to other components.
     */
    public void apply() throws ConfigurationException
    {
        if( null != javaDocTemplateEditor )
        {
            javaDocTemplateEditor.applyChange();
        }
    }

    /**
     * Load settings from other components to configurable.
     */
    public void reset()
    {

    }

    /**
     * Disposes the Swing components used for displaying the configuration.
     */
    public void disposeUIResources()
    {
        javaDocTemplateEditor = null;
    }

    public Element getState()
    {
        try
        {
            final Element e = new Element( "state" );
            XmlSerializer.serializeInto( this, e );
            return e;
        }
        catch( XmlSerializationException e1 )
        {
            log.error( e1 );
            return null;
        }
    }

    public void loadState( final Element state )
    {
        try
        {
            XmlSerializer.deserializeInto( this, state );
        }
        catch( XmlSerializationException e )
        {
            log.error( e );
        }
    }
}
