package com.perniciouspenguins.ideaz.javadoc.fixes;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.util.IncorrectOperationException;
import com.perniciouspenguins.ideaz.javadoc.templates.JavaDocGenerator;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Class GenerateFromClass offers the ability to generate a plain JavaDoc declaration for a class that does not already
 * declare one.
 *
 * author: Raymond Brandon date: Sat Jan 20 22:08:09 CET 2007
 */
public class GenerateFromClass extends LocalQuickFixBase
{
    /**
     * Method LocalQuickFixBase creates a new instance of a Quick fix for a class that does not have JavaDoc.
     *
     * @param psiClass the class to which the quick fix applies.
     */
    public GenerateFromClass( PsiClass psiClass )
    {
        super( psiClass );
    }

    /**
     * Returns the name of the quick fix.
     *
     * @return the name of the quick fix.
     */
    @NotNull
    public String getName()
    {
        if( psiClass != null )
        {
            if( psiClass.isInterface() )
            {
                return FIX_GENERATE_FROM_INTERFACE;
            }
            else if( psiClass.isEnum() )
            {
                return FIX_GENERATE_FROM_ENUM;
            }
        }
        return FIX_GENERATE_FROM_CLASS;
    }

    /**
     * Method doFix returns the actual fix that needs to be executed to solve the detected problem.
     */
    public void doFix()
    {
        try
        {
            PsiElementFactory factory =
                    JavaPsiFacade.getInstance( psiClass.getProject() ).getElementFactory();
            String javaDocTemplateText = JavaDocGenerator.generateJavaDoc( psiClass );
            if( !psiClass.isEnum() )
            {
                PsiUtils.setPsiDocComment(
                        factory.createDocCommentFromText( javaDocTemplateText ),
                        psiClass );
            }
            else
            {
                PsiUtils.setPsiDocComment( factory.createDocCommentFromText( javaDocTemplateText ),
                                           psiClass );
            }
        }
        catch( IncorrectOperationException e )
        {
            e.printStackTrace();
        }
    }
}
