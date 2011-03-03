package com.perniciouspenguins.ideaz.javadoc.fixes;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.util.IncorrectOperationException;
import com.perniciouspenguins.ideaz.javadoc.templates.JavaDocGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * The GenerateFromField class offers the ability to generate
 * a basic JavaDoc declaration based on the field definition.
 *
 * Author: Raymond Brandon
 * Date: Dec 24, 2005 6:29:36 PM
 */
public class GenerateFromField extends LocalQuickFixBase
{
  /**
   * Method LocalQuickFixBase creates a new instance of a Quick fix for a field.
   *
   * @param psiField the field to which the quick fix applies.
   */
  public GenerateFromField( PsiField psiField )
  {
    super( psiField );
  }

  /**
   * Returns the name of the quick fix.
   *
   * @return the name of the quick fix.
   */
  @NotNull
  public String getName()
  {
    return FIX_GENERATE_FROM_FIELD;
  }

  /**
   * Method doFix returns the actual fix that needs to be executed to solve the
   * detected problem.
   */
  public void doFix()
  {
    try
    {
      PsiElementFactory factory =
          JavaPsiFacade.getInstance( field.getProject() ).getElementFactory();
      String javaDocTemplateText = JavaDocGenerator.generateJavaDoc( field );
      PsiDocComment comment = factory.createDocCommentFromText( javaDocTemplateText );
      field.addBefore( comment, field.getModifierList() );
    }
    catch( IncorrectOperationException e )
    {
      e.printStackTrace();
    }
  }
}
