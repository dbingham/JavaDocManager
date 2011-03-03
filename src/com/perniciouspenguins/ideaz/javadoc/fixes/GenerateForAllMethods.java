package com.perniciouspenguins.ideaz.javadoc.fixes;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.util.IncorrectOperationException;
import com.perniciouspenguins.ideaz.javadoc.templates.JavaDocGenerator;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The GenerateForAllMethods class offers the ability to generate
 * a JavaDoc declaration for all methods that don't specify JavaDoc.
 *
 * Author: Raymond Brandon
 * Date: May 14, 2007 10:00:23 PM
 */
public class GenerateForAllMethods extends LocalQuickFixBase
{
  /**
   * Method LocalQuickFixBase creates a new instance of a Quick fix for a method that does not
   * have a parent method.
   *
   * @param psiClass the psiClass to which the quick fix applies.
   */
  public GenerateForAllMethods( PsiClass psiClass )
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
    return FIX_GENERATE_FOR_ALL_METHODS;
  }

  /**
   * Method doFix returns the actual fix that needs to be executed to solve the
   * detected problem.
   */
  public void doFix()
  {
    PsiMethod[] psiMethods = psiClass.getMethods();

    for(PsiMethod psiMethod : psiMethods )
    {
      try
      {
        if( null == psiMethod.getDocComment() )
        {
          fixMethod( psiMethod );
        }
      }
      catch( IncorrectOperationException e )
      {
        e.printStackTrace();
      }
    }
  }

  /**
   * Method fixMethod applies the defined JavaDoc template to the specified method.
   * @param psiMethod the method that does not define JavaDoc
   * @throws IncorrectOperationException when creating the JavaDoc fails
   */
  private void fixMethod( PsiMethod psiMethod ) throws IncorrectOperationException
  {
    //PsiElementFactory factory = psiClass.getManager().getElementFactory();
    PsiElementFactory factory =
        JavaPsiFacade.getInstance( psiMethod.getProject() ).getElementFactory();
    String javaDocTemplateText = JavaDocGenerator.generateJavaDoc( psiMethod );

    PsiUtils.setPsiDocComment( factory.createDocCommentFromText( javaDocTemplateText ), psiMethod );
  }
}
