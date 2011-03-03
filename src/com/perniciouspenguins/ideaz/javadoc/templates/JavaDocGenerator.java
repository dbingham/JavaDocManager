package com.perniciouspenguins.ideaz.javadoc.templates;

import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PropertyUtil;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;

import java.util.Properties;

/**
 * Class JavaDocGenerator
 *
 * @author Raymond Brandon
 * date: Sat Jan 27 23:24:22 CET 2007
 */
public class JavaDocGenerator
{
    private static final Logger log = new DefaultLogger( "JavaDocManager" );

    /**
     * Method generateJavaDoc provides functionality to lookup the template that is associated with
     * the type of PsiElement and merge the tokens in the template with the supplied values as found
     * in the properties map.
     *
     * @param docCommentOwner the PsiElement for which JavaDoc is generated
     * @return String the generated JavaDoc.
     */
    public static String generateJavaDoc( PsiDocCommentOwner docCommentOwner )
    {
        Template template = getPattern( docCommentOwner );
        if( null != template )
        {
            log.debug( "Using template '" + template.getName() + "'" );
            return TemplateManager.getInstance().merge( template );
        }
        else
        {
            log.debug( "No template found for " + docCommentOwner.getClass().getName() );
        }
        return null;
    }

    /**
     * Method getPattern determines which template should be loaded based
     * on the specified type of the docCommentOwner parameter.
     *
     * @param docCommentOwner the PsiElement capable of having a JavaDoc declaration.
     * @return FileTemplate the template that is associated to this type or null if no template could be found.
     */
    private static Template getPattern( PsiDocCommentOwner docCommentOwner )
    {
        Template template = null;
        if( null == docCommentOwner )
        {
            template = null;
        }
        else
        {
            TemplateManager templateManager = TemplateManager.getInstance();
            if( docCommentOwner instanceof PsiClass )
            {
                if( ((PsiClass) docCommentOwner).isInterface() )
                {
                    log.debug( "docCommentOwner is an interface" );
                    template = templateManager.loadTemplate( TemplateManager.TEMPLATE_INTERFACE );
                }
                else if( ((PsiClass) docCommentOwner).isEnum() )
                {
                    log.debug( "docCommentOwner is an enum" );
                    template = templateManager.loadTemplate( TemplateManager.TEMPLATE_ENUM );
                }
                else
                {
                    log.debug( "docCommentOwner is a class" );
                    template = templateManager.loadTemplate( TemplateManager.TEMPLATE_CLASS );
                }
            }
            else if( docCommentOwner instanceof PsiField )
            {
                log.debug( "docCommentOwner is a field" );
                template = templateManager.loadTemplate( TemplateManager.TEMPLATE_FIELD );
            }
            else if( docCommentOwner instanceof PsiMethod )
            {
                log.debug( "docCommentOwner is a method" );
                PsiMethod method = ((PsiMethod) docCommentOwner);
                if( method.isConstructor() )
                {
                    log.debug( " - method is a constuctor" );
                    template = templateManager.loadTemplate( TemplateManager.TEMPLATE_CONSTRUCTOR );
                }
                else if( PropertyUtil.isSimplePropertyGetter( method ) )
                {
                    log.debug( " - method is a getter" );
                    template = templateManager.loadTemplate( TemplateManager.TEMPLATE_GETTER_METHOD );
                }
                else if( PropertyUtil.isSimplePropertySetter( method ) )
                {
                    log.debug( " - method is a setter" );
                    template = templateManager.loadTemplate( TemplateManager.TEMPLATE_SETTER_METHOD );
                }
                else
                {
                    log.debug( " - method is not a getter/setter/constructor" );
                    template = templateManager.loadTemplate( TemplateManager.TEMPLATE_PLAIN_METHOD );
                }
            }
            if( null != template )
            {
                extendTemplateForType( template, docCommentOwner );
            }
        }
        return template;
    }

    /**
     * Method extendTemplateForType builds a collection of tokens and token values based on the supplied
     * PsiDocCommentOwner type.
     *
     * @param template the template that will be extended based on the type of the docCommentOwner.
     * @param docCommentOwner the PsiElement capable of having JavaDoc @return Map a collection of tokens and token values.
     */
    private static void extendTemplateForType( Template template, PsiDocCommentOwner docCommentOwner )
    {
        TemplateManager templateManager = TemplateManager.getInstance();
        Properties properties = FileTemplateManager.getInstance().getDefaultProperties();
        properties.put( "NAME", docCommentOwner.getName() );

        if( docCommentOwner instanceof PsiClass )
        {
            properties.put( "PACKAGE", PsiUtils.getPackage( (PsiClass) docCommentOwner ) );
        }

        String text = template.getText();
        StringBuffer sb = new StringBuffer( text.substring( 0, text.lastIndexOf( "*/" ) ) );

        if( docCommentOwner instanceof PsiMethod )
        {
            PsiMethod method = (PsiMethod) docCommentOwner;
            if( !PsiUtils.isAnonymous( method ) && null != method.getContainingClass() )
            {
                properties.put( "CLASS", method.getContainingClass().getName() );
            }
            if( PropertyUtil.isSimplePropertyGetter( method ) )
            {
                String propertyName = PropertyUtil.getPropertyName( method );
                properties.put( "FIELD_NAME", propertyName );
                properties.put( "FIELD_COMMENT", PsiUtils.getFieldDocToken( method, propertyName ) );
            }
            if( PropertyUtil.isSimplePropertySetter( method ) && text.indexOf( "@param" ) != -1 )
            {
                properties.put( "PARAM", method.getParameterList().getParameters()[0].getName() );
                String propertyName = PropertyUtil.getPropertyName( method );
                properties.put( "FIELD_NAME", propertyName );
                properties.put( "FIELD_COMMENT", PsiUtils.getFieldDocToken( method, propertyName ) );
            }

            PsiParameter[] parameters = method.getParameterList().getParameters();
            if( parameters.length > 0 )
            {
                sb.append( "*\n" );

                Template paramTemplate = templateManager.loadTemplate( TemplateManager.TEMPLATE_METHOD_PARAM );

                Properties props = new Properties();

                for( PsiParameter parameter : parameters )
                {
                    props.put( "PARAM", parameter.getName() );
                    props.put( "TYPE", parameter.getType().getPresentableText() );
                    paramTemplate.setProperties( props );

                    if( text.indexOf( "@param" ) == -1 )
                    {
                        sb.append( " * " ).append( templateManager.merge( paramTemplate ) ).append( " \n" );
                    }
                }
            }

            PsiType methodReturnType = method.getReturnType();
            //Why PsiType.VOID doesn't work is beyond me...
            if( methodReturnType != null && !methodReturnType.getPresentableText().equals( "void" ) )
            {
                if( text.indexOf( "@return" ) == -1 )
                {
                    Template returnTypeTemplate =
                            templateManager.loadTemplate( TemplateManager.TEMPLATE_METHOD_RETURN_TYPE );

                    Properties props = new Properties();
                    props.put( "RETURN_TYPE", methodReturnType.getPresentableText() );
                    returnTypeTemplate.setProperties( props );
                    sb.append( " * " ).append( templateManager.merge( returnTypeTemplate ) ).append( " \n" );
                }
                else
                {
                    properties.put( "RETURN_TYPE", methodReturnType.getPresentableText() );
                }
            }

            PsiClassType[] referencedTypes = method.getThrowsList().getReferencedTypes();
            if( referencedTypes.length > 0 )
            {
                Template throwsTemplate = templateManager.loadTemplate( TemplateManager.TEMPLATE_METHOD_THROWS );

                for( PsiClassType referencedType : referencedTypes )
                {

                    Properties props = new Properties();
                    props.put( "THROWABLE", referencedType.getClassName() );
                    throwsTemplate.setProperties( props );

                    sb.append( " * " ).append( templateManager.merge( throwsTemplate ) ).append( " \n" );
                }
            }
        }
        sb.append( " */" );

        template.setText( sb.toString() );
        template.setProperties( properties );
    }
}
