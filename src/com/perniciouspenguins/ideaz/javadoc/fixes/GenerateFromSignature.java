package com.perniciouspenguins.ideaz.javadoc.fixes;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.util.IncorrectOperationException;
import com.perniciouspenguins.ideaz.javadoc.templates.JavaDocGenerator;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The GenerateFromSignature class offers the ability to generate
 * a JavaDoc declaration based on the method signature.
 *
 * Author: Raymond Brandon
 * Date: Dec 24, 2005 6:29:36 PM
 */
public class GenerateFromSignature extends LocalQuickFixBase
{
  /**
   * Method LocalQuickFixBase creates a new instance of a Quick fix for a method that does not
   * have a parent method.
   *
   * @param method the method to which the quick fix applies.
   */
  public GenerateFromSignature( PsiMethod method )
  {
    super( method );
  }

  /**
   * Returns the name of the quick fix.
   *
   * @return the name of the quick fix.
   */
  @NotNull
  public String getName()
  {
    return FIX_GENERATE_FROM_SIGNATURE;
  }

  /**
   * Method doFix returns the actual fix that needs to be executed to solve the
   * detected problem.
   */
  public void doFix()
  {
    try
    {
      //PsiElementFactory factory = method.getManager().getElementFactory();
      PsiElementFactory factory =
          JavaPsiFacade.getInstance( method.getProject() ).getElementFactory();
      String javaDocTemplateText = JavaDocGenerator.generateJavaDoc( method );
      PsiUtils.setPsiDocComment(
          factory.createDocCommentFromText(
              javaDocTemplateText ), method );
    }
    catch( IncorrectOperationException e )
    {
      e.printStackTrace();
    }
  }
}
