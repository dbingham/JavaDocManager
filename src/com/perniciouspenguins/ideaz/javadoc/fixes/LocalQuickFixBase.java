package com.perniciouspenguins.ideaz.javadoc.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * The LocalQuickFixBase implements base functionality for all
 * child classes.
 *
 * Author: Raymond Brandon
 * Date: Dec 24, 2005 6:43:01 PM
 */
public abstract class LocalQuickFixBase implements LocalQuickFix
{
  /** Field INSPECTION_FAMILY_NAME:  **/
  protected static final String INSPECTION_FAMILY_NAME = "JavaDoc inspection";
  /** Field INTERFACE_DEFINITION_NO_JAVADOC:  **/
  public static final String INTERFACE_DEFINITION_NO_JAVADOC = "Interface does not have JavaDoc declaration";
  /** Field ENUM_DEFINITION_NO_JAVADOC:  **/
  public static final String ENUM_DEFINITION_NO_JAVADOC = "Enum does not have JavaDoc declaration";
  /** Field CLASS_DEFINITION_NO_JAVADOC:  **/
  public static final String CLASS_DEFINITION_NO_JAVADOC = "Class does not have JavaDoc declaration";
  /** Field FIELD_DEFINITION_NO_JAVADOC:  **/
  public static final String FIELD_DEFINITION_NO_JAVADOC = "Field does not have JavaDoc declaration";
  /** Field BASE_METHOD_NO_JAVADOC:  **/
  public static final String BASE_METHOD_NO_JAVADOC = "Method does not have JavaDoc";
  /** Field INTERFACE_DEFINITION_NO_JAVADOC:  **/
  public static final String INTERFACE_METHOD_NO_JAVADOC = "Interface method definition does not have JavaDoc";
  /** Field IMPLEMENTATION_NO_JAVADOC:  **/
  public static final String IMPLEMENTATION_NO_JAVADOC = "Interface method implementation does not have JavaDoc";
  /** Field OVERRIDDEN_METHOD_NO_JAVADOC:  **/
  public static final String OVERRIDDEN_METHOD_NO_JAVADOC = "Overridden method does not have javadoc";
  /** Field OVERRIDDEN_METHOD_ANONYMOUS_CLASS_NO_JAVADOC:  **/
  public static final String OVERRIDDEN_METHOD_ANONYMOUS_CLASS_NO_JAVADOC =
      "Overridden method of anonymous class does not have javadoc";
  /** Field JAVADOC_DIFFERS_FROM_PARENT:  **/
  public static final String ONE_OR_MORE_METHODS_DO_NOT_DEFINE_JAVADOC = "One or more methods do not define JavaDoc";
  /** Field JAVADOC_DIFFERS_FROM_PARENT:  **/
  public static final String JAVADOC_DIFFERS_FROM_PARENT = "JavaDoc differs from JavaDoc in parent method";

  public static final String NO_SUPER_METHOD_TO_INHERIT_DOC_FROM =
      "Cannot inherit JavaDoc because no super method exists";

  public static final String NO_JAVADOC_IN_SUPER_METHOD_TO_INHERIT_FROM =
      "Cannot inherit JavaDoc, super method does not declare any";

  /** Field JAVADOC_METHOD_SIGNATURE:  **/
//  public static final String JAVADOC_METHOD_SIGNATURE = "JavaDoc does not reflect method signature";
  /** Field FIX_ADD_REFERENCE:  **/
  protected static final String FIX_ADD_REFERENCE = "Add JavaDoc reference to parent method";
  /** Field FIX_INTRODUCE_REFERENCE:  **/
  protected static final String FIX_INTRODUCE_REFERENCE = "Introduce JavaDoc reference to parent method";
  /** Field FIX_MOVE_TO_PARENT_INTRODUCE_REFERENCE:  **/
  protected static final String FIX_MOVE_TO_PARENT_INTRODUCE_REFERENCE =
      "Move JavaDoc to parent class and introduce reference";
  /** Field FIX_COPY_FROM_PARENT:  **/
  protected static final String FIX_COPY_FROM_PARENT = "Copy JavaDoc from parent method";
  /** Field FIX_COPY_DOC_TO_PARENT:  **/
  protected static final String FIX_COPY_DOC_TO_PARENT = "Copy JavaDoc to parent";
  /** Field FIX_REPLACE_PARENT_DOC:  **/
  protected static final String FIX_REPLACE_PARENT_DOC = "Replace parent method JavaDoc with this JavaDoc";
  /** Field FIX_REPLACE_COPY_WITH_REFERENCE:  **/
//  protected static final String FIX_REPLACE_COPY_WITH_REFERENCE = "Replace JavaDoc copy with reference to parent method";
  /** Field FIX_GENERATE_FROM_SIGNATURE:  **/
  protected static final String FIX_GENERATE_FROM_SIGNATURE = "Generate JavaDoc based on method signature";
  /** Field FIX_GENERATE_FROM_CLASS:  **/
  protected static final String FIX_GENERATE_FROM_CLASS = "Generate JavaDoc based on class";
  /** Field FIX_GENERATE_FROM_CLASS:  **/
  protected static final String FIX_GENERATE_FROM_INTERFACE = "Generate JavaDoc based on interface";
  /** Field FIX_GENERATE_FROM_CLASS:  **/
  protected static final String FIX_GENERATE_FROM_ENUM = "Generate JavaDoc based on enum";
  /** Field FIX_GENERATE_FROM_FIELD:  **/
  protected static final String FIX_GENERATE_FROM_FIELD = "Generate JavaDoc based on field";
  /** Field FIX_GENERATE_FROM_FIELD:  **/
  protected static final String FIX_GENERATE_FOR_ALL_METHODS = "Generate JavaDoc for all methods";

  /** Field FIX_SYNC_JAVADOC_FROM_SIGNATURE:  **/
//  protected static final String FIX_SYNC_JAVADOC_FROM_SIGNATURE = "Synchronize JavaDoc with method signature";
  /** Field psiClass:  **/
  protected PsiClass psiClass = null;
  /** Field field:  **/
  protected PsiField field = null;
  /** Field method:  **/
  protected PsiMethod method = null;
  /** Field superMethod:  **/
  protected PsiMethod superMethod = null;
  /** Field superClass:  **/
  protected PsiClass superClass = null;

  /**
   * Method LocalQuickFixBase creates a new instance of a Quick fix for a class that does not
   * have JavaDoc.
   *
   * @param psiClass the class to which the quick fix applies.
   */
  protected LocalQuickFixBase( PsiClass psiClass )
  {
    this.psiClass = psiClass;
  }

  /**
   * Method LocalQuickFixBase creates a new instance of a Quick fix for a method that does not
   * have a parent method.
   *
   * @param method the method to which the quick fix applies.
   */
  protected LocalQuickFixBase( PsiMethod method )
  {
    this.method = method;
  }

  /**
   * Method LocalQuickFixBase creates a new instance of a Quick fix for a method that has a parent
   * method.
   *
   * @param method the method to which the quick fix applies.
   * @param superMethod the super method of which the JavaDoc is taken into account when offering
   * quick fixes.
   */
  protected LocalQuickFixBase( PsiMethod method, PsiMethod superMethod )
  {
    this.method = method;
    this.superMethod = superMethod;
    superClass = superMethod.getContainingClass();
  }

  /**
   * Method LocalQuickFixBase creates a new instance of a Quick fix for a field.
   *
   * @param psiField the field to which the quick fix applies.
   */
  protected LocalQuickFixBase( PsiField psiField )
  {
    field = psiField;
  }

  /**
   * Returns the name of the quick fix.
   *
   * @return the name of the quick fix.
   */
  @NotNull
  public abstract String getName();

  /**
   * Called to apply the fix.
   * @param project {@link com.intellij.openapi.project.Project}
   * @param descriptor problem reported by the tool which provided this quick fix action
   */
  public void applyFix( @NotNull final Project project, @NotNull ProblemDescriptor descriptor )
  {
    ApplicationManager.getApplication().runWriteAction(
        new Runnable()
        {
          /**
           * Method run contains the action that needs to be executed when the dispatch thread
           * allows write actions.
           */
          public void run()
          {
            doFix();
          }
        } );
  }

  /**
   * Method doFix returns the actual fix that needs to be executed to solve the
   * detected problem.
   */
  public abstract void doFix();

  /**
   * Method getFamilyName returns the category name of
   * this inspection.
   *
   * @return the category of this inspection.
   */
  @NotNull
  public String getFamilyName()
  {
    return INSPECTION_FAMILY_NAME;
  }
}
