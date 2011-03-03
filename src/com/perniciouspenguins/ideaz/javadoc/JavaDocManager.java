package com.perniciouspenguins.ideaz.javadoc;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializationException;
import com.intellij.util.xmlb.XmlSerializer;
import com.perniciouspenguins.ideaz.javadoc.inspections.InconsistentJavaDocInspection;
import com.perniciouspenguins.ideaz.javadoc.inspections.MissingClassJavaDocInspection;
import com.perniciouspenguins.ideaz.javadoc.inspections.MissingFieldJavaDocInspection;
import com.perniciouspenguins.ideaz.javadoc.inspections.MissingMethodJavaDocInspection;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * JavaDoc Sync plugin to provide inspections that detect missing or inconsistent JavaDoc declarations.
 *
 * Author: Raymond Brandon Date: Dec 23, 2005 9:45:54 PM
 */
@State(
        name = JavaDocManager.COMPONENT_NAME,
        storages = {@Storage(
            id = "JavaDoc",
            file = "$APP_CONFIG$/JavaDocManager.xml"
        )}
)
public class JavaDocManager
        implements ApplicationComponent, InspectionToolProvider, PersistentStateComponent<Element>
{
    private static final Logger log = Logger.getInstance( "JavaDocManager" );
    public static final String COMPONENT_NAME = "JavaDocManager";

    /**
     * Method JavaDocManager creates a new instance of the plugin.
     */
    public JavaDocManager()
    {
    }

    /**
     * Unique name of this component. If there is another component with the same name or name is null internal
     * assertion will occur.
     *
     * @return the name of this component
     */
    @NonNls
    @NotNull
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }

    /**
     * Component should do initialization and communication with another components in this method.
     */
    public void initComponent()
    {
    }

    /**
     * Component should dispose system resources or perform another cleanup in this method.
     */
    public void disposeComponent()
    {
    }

    /**
     * Query method for inspection tools provided by a plugin.
     *
     * @return classes that extend {@link com.intellij.codeInspection.LocalInspectionTool}
     */
    public Class[] getInspectionClasses()
    {
        return new Class[] {MissingClassJavaDocInspection.class,
                            MissingFieldJavaDocInspection.class,
                            MissingMethodJavaDocInspection.class,
                            InconsistentJavaDocInspection.class};
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