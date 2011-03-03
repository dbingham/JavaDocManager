package com.perniciouspenguins.ideaz.javadoc.templates;

import java.util.Properties;

/**
 * Class Template ...
 *
 * @author Raymond P. Brandon
 * Created on Mar 3, 2007
 */
public class Template {
    private String name;
    private String type;
    private String text = null;
    private String documentation = null;
    private Properties properties = null;

    /**
     * Constructor Template creates a new Template instance.
     *
     * @param type of type String
     * @param name of type String
     */
    public Template(String type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Method getName returns the name of this Template object.
     *
     * @return String the name of this Template object.
     * */
    public String getName() {
        return name;
    }

    /**
     * Method getText returns the text of this Template object.
     *
     * @return String the text of this Template object.
     * */
    public String getText() {
        return text;
    }

    /**
     * Method setText sets the text of this Template object.
     *
     * @param text the text of this Template object.
     *
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Method getDocumentation returns the HTML documentation describing this text.
     *
     * @return String the HTML documentation describing this text.
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * Method setDocumentation sets the HTML documentation describing this text.
     *
     * @param documentation the HTML documentation describing this text.
     */
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    /**
     * Method getProperties returns the available properties for this text.
     *
     * @return Properties the available properties for this text.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Method setProperties allows the available properties for this text to be set.
     *
     * @param properties the available properties for this text.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Method toString returns the type name of this text.
     *
     * @return String the type name of this text.
     */
    public String toString() {
        return type;
    }
}
