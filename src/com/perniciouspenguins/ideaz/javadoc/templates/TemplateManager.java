package com.perniciouspenguins.ideaz.javadoc.templates;

import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Raymond P. Brandon
 * Date: Feb 24, 2007
 * Time: 11:50:56 PM
 */
public class TemplateManager
{
    public static final String TEMPLATE_CLASS = "Class";
    public static final String TEMPLATE_ENUM = "Enum";
    public static final String TEMPLATE_CONSTRUCTOR = "Constructor";
    public static final String TEMPLATE_GETTER_METHOD = "Getter method";
    public static final String TEMPLATE_FIELD = "Field";
    public static final String TEMPLATE_INTERFACE = "Interface";
//    public static final String TEMPLATE_OVERRIDDEN_METHOD = "Overridden method";
    public static final String TEMPLATE_PLAIN_METHOD = "Plain method";
    public static final String TEMPLATE_METHOD_PARAM = "Method parameter";
    public static final String TEMPLATE_METHOD_THROWS = "Method throws clause";
    public static final String TEMPLATE_METHOD_RETURN_TYPE = "Method return type";
    public static final String TEMPLATE_SETTER_METHOD = "Setter method";

    private static final String TEMPLATE_CLASS_JAVADOC = "Class JavaDoc";
    private static final String TEMPLATE_ENUM_JAVADOC = "Enum JavaDoc";
    private static final String TEMPLATE_INTERFACE_JAVADOC = "Interface JavaDoc";
    private static final String TEMPLATE_FIELD_JAVADOC = "Field JavaDoc";
    private static final String TEMPLATE_CONSTRUCTOR_JAVADOC = "Constructor JavaDoc";
    private static final String TEMPLATE_METHOD_JAVADOC = "Method JavaDoc";
    private static final String TEMPLATE_PARAMETER_JAVADOC = "Parameter JavaDoc";
    private static final String TEMPLATE_RETURN_TYPE_JAVADOC = "Return type JavaDoc";
    private static final String TEMPLATE_THROWS_CLAUSE_JAVADOC = "Throws clause JavaDoc";
    private static final String TEMPLATE_GET_METHOD_JAVADOC = "Get Method JavaDoc";
    private static final String TEMPLATE_SET_METHOD_JAVADOC = "Set Method JavaDoc";

    private static final String TEMPLATE_DEFINITION_EXTENSION = ".tpl";
    private static final String TEMPLATE_DESCRIPTION_EXTENSION = ".html";

    private static final String TEMPLATE_LOCATION = "fileTemplates/jds/";
    private final Logger log = new DefaultLogger( "JavaDocManager" );
    private static TemplateManager instance = null;

    /**
     * Returns the singleton instance of this class
     * @return the singleton instance of this class.
     */
    public static TemplateManager getInstance()
    {
        if( null == instance )
        {
            instance = new TemplateManager();
        }
        return instance;
    }

    /**
     * Method loadTemplate loads the template data based on the type name.
     * @param type the type of template to load
     * @return an intialized template object representing the specified type.
     */
    public Template loadTemplate( String type )
    {
        Template template = null;
        if( TEMPLATE_INTERFACE.equals( type ) )
        {
            log.debug( "docCommentOwner is an interface" );
            template = readTemplateAndDocumentation( TEMPLATE_INTERFACE, TEMPLATE_INTERFACE_JAVADOC );
        }
        else if( TEMPLATE_CLASS.equals( type ) )
        {
            log.debug( "docCommentOwner is a class" );
            template = readTemplateAndDocumentation( TEMPLATE_CLASS, TEMPLATE_CLASS_JAVADOC );
        }
        else if( TEMPLATE_ENUM.equals( type ) )
        {
            log.debug( "docCommentOwner is an enum" );
            template = readTemplateAndDocumentation( TEMPLATE_ENUM, TEMPLATE_ENUM_JAVADOC );
        }
        else if( TEMPLATE_FIELD.equals( type ) )
        {
            log.debug( "docCommentOwner is a field" );
            template = readTemplateAndDocumentation( TEMPLATE_FIELD, TEMPLATE_FIELD_JAVADOC );
        }
        else if( TEMPLATE_CONSTRUCTOR.equals( type ) )
        {
            log.debug( " - method is a constuctor" );
            template = readTemplateAndDocumentation( TEMPLATE_CONSTRUCTOR, TEMPLATE_CONSTRUCTOR_JAVADOC );
        }
        else if( TEMPLATE_GETTER_METHOD.equals( type ) )
        {
            log.debug( " - method is a getter" );
            template = readTemplateAndDocumentation( TEMPLATE_GETTER_METHOD, TEMPLATE_GET_METHOD_JAVADOC );
        }
        else if( TEMPLATE_SETTER_METHOD.equals( type ) )
        {
            log.debug( " - method is a setter" );
            template = readTemplateAndDocumentation( TEMPLATE_SETTER_METHOD, TEMPLATE_SET_METHOD_JAVADOC );
        }
        else if( TEMPLATE_METHOD_PARAM.equals( type ) )
        {
            log.debug( " - method parameter template" );
            template = readTemplateAndDocumentation( TEMPLATE_METHOD_PARAM, TEMPLATE_PARAMETER_JAVADOC );
        }
        else if( TEMPLATE_METHOD_RETURN_TYPE.equals( type ) )
        {
            log.debug( " - method return type template" );
            template = readTemplateAndDocumentation( TEMPLATE_METHOD_RETURN_TYPE, TEMPLATE_RETURN_TYPE_JAVADOC );
        }
        else if( TEMPLATE_METHOD_THROWS.equals( type ) )
        {
            log.debug( " - method throws clause template" );
            template = readTemplateAndDocumentation( TEMPLATE_METHOD_THROWS, TEMPLATE_THROWS_CLAUSE_JAVADOC );
        }
        else if( TEMPLATE_PLAIN_METHOD.equals( type ) )
        {
            log.debug( " - method is not a getter/setter/constructor" );
            template = readTemplateAndDocumentation( TEMPLATE_PLAIN_METHOD, TEMPLATE_METHOD_JAVADOC );
        }
        if( null != template )
        {
            template.setProperties( extractTokensFromTemplate( template ) );
        }
        return template;
    }

    /**
     * Method extractTokensFromTemplate scans for template tokens in the text of the specified
     * template.
     *
     * @param template of type Template
     * @return Properties a collection of tokens that were found in the template.
     */
    private Properties extractTokensFromTemplate( Template template )
    {
        Properties properties = new Properties();
        String templateText = template.getText();
        int tokenStart = 0;
        while( tokenStart > -1 && tokenStart < templateText.length() )
        {
            tokenStart = templateText.indexOf( "${", tokenStart );
            if( tokenStart != -1 )
            {
                String token = templateText.substring( tokenStart + 2, templateText.indexOf( "}", tokenStart ) );
                properties.put( token, "" );
                tokenStart = tokenStart + token.length();
            }
        }
        return properties;
    }


    /**
     * Method readTemplateAndDocumentation loads the template data from disk or, if not present, from the jar file.
     * @param type template type
     * @param name template name
     * @return the template representing the specified type and name.
     */
    private Template readTemplateAndDocumentation( String type, String name )
    {
        Template template = new Template( type, name );
        log.info( "Retrieving template " + name );
        File configFolder = new File( PathManager.getConfigPath() );
        File templateFolder = new File( configFolder, TEMPLATE_LOCATION );

        File tpl = new File( templateFolder, name + TEMPLATE_DEFINITION_EXTENSION );
        InputStream inputStream = null;
        ClassLoader loader = getClass().getClassLoader();
        if( tpl.exists() )
        {
            log.info( "Template " + name + " already exists at " + templateFolder.getPath() );
            try
            {
                template.setText( readTemplate( new FileInputStream( tpl ) ) );
            }
            catch( FileNotFoundException e )
            {
                log.error( e.getMessage() );
            }
        }
        else
        {
            log.info( "Template " + name + " not found at " + templateFolder.getPath() + ", reading from jar..." );

            try
            {
                inputStream = loader.getResourceAsStream( TEMPLATE_LOCATION + name + TEMPLATE_DEFINITION_EXTENSION );
                template.setText( extractAndReadTemplate( inputStream, tpl ) );
            }
            finally
            {
                if( null != inputStream )
                {
                    try
                    {
                        inputStream.close();
                    }
                    catch( IOException e )
                    {
                    }
                }
            }
        }

        try
        {
            inputStream = loader.getResourceAsStream( TEMPLATE_LOCATION + name + TEMPLATE_DESCRIPTION_EXTENSION );
            template.setDocumentation( readTemplate( inputStream ) );
        }
        finally
        {
            if( null != inputStream )
            {
                try
                {
                    inputStream.close();
                }
                catch( IOException e ){/*Nothing we can do*/}
            }
        }
        return template;
    }


    /**
     * Method extractAndReadTemplate reads the template data from the jar and saves a local disk copy.
     *
     * @param template the stream to read the template
     * @param outputFile the file to write to on disk
     * @return String the template text
     */
    private String extractAndReadTemplate( InputStream template, File outputFile )
    {
        FileOutputStream fileOutputStream = null;
        String tpl = null;
        try
        {
            tpl = readTemplate( template );
            if( !outputFile.getParentFile().exists() )
            {
                //noinspection ResultOfMethodCallIgnored
                outputFile.getParentFile().mkdirs();
            }
            fileOutputStream = new FileOutputStream( outputFile );
            fileOutputStream.write( tpl.getBytes() );
            fileOutputStream.flush();
        }
        catch( FileNotFoundException e )
        {
            log.error( e.getMessage() );
        }
        catch( IOException e )
        {
            log.error( e.getMessage() );
        }
        finally
        {
            if( null != fileOutputStream )
            {
                try
                {
                    fileOutputStream.close();
                }
                catch( IOException e )
                {
                }
            }
        }
        return tpl;
    }

    /**
     * Method readTemplate reads the text of a template from the specified stream.
     *
     * @param template the stream to read from.
     * @return String the text of the template.
     */
    private String readTemplate( InputStream template )
    {
        String tpl = null;
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader( new InputStreamReader( template ) );
            tpl = readTemplateFromReader( reader );
        }
        catch( Exception e )
        {
            log.error( e.getMessage() );
        }
        finally
        {
            if( null != reader )
            {
                try
                {
                    reader.close();
                }
                catch( IOException e )
                {
                }
            }
        }
        return tpl;
    }

    /**
     * Method saveTemplate writes the new template text to disk.
     *
     * @param template the template to save
     */
    public void saveTemplate( Template template )
    {
        if( null != template )
        {
            String templateLocation = TEMPLATE_LOCATION;

            File configFolder = new File( PathManager.getConfigPath() );
            File templateFolder = new File( configFolder, templateLocation );

            File tpl = new File( templateFolder, template.getName() + TEMPLATE_DEFINITION_EXTENSION );
            FileOutputStream fileOutputStream = null;
            try
            {
                fileOutputStream = new FileOutputStream( tpl );
                fileOutputStream.write( template.getText().getBytes() );
                fileOutputStream.flush();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                if( null != fileOutputStream )
                {
                    try
                    {
                        fileOutputStream.close();
                    }
                    catch( IOException e )
                    {
                    }
                }
            }
        }
    }

    /**
     * Method readTemplateFromReader reads the template text from the specified reader and appends new line feed
     * characters.
     *
     * @param reader the stream to read from.
     * @return String the template text including line feeds.
     * @throws IOException when reading from the stream fails.
     */
    private String readTemplateFromReader( BufferedReader reader ) throws IOException
    {
        StringBuffer sb = new StringBuffer();
        String line = reader.readLine();
        while( null != line )
        {
            sb.append( line );
            line = reader.readLine();
            if( null != line )
            {
                sb.append( "\n" );
            }
        }
        return sb.toString();
    }

    /**
     * Singleton
     */
    private TemplateManager()
    {
    }

    /**
     * Method merge ...
     *
     * @param template of type Template
     * @return String
     */
    public String merge( Template template )
    {
        String mergedText = null;
        try
        {
            mergedText = FileTemplateUtil.mergeTemplate(
                    template.getProperties(), template.getText().replaceAll( "\n\n", "\n" ) );
        }
        catch( IOException e )
        {
            log.error( "Caught exception while merging " + template.getName() + " with properties: " + e.getMessage() );
        }
        return mergedText;
    }
}
