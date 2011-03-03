package com.perniciouspenguins.ideaz.javadoc.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.ProjectScope;
import com.perniciouspenguins.ideaz.javadoc.fixes.CopyFromParent;
import com.perniciouspenguins.ideaz.javadoc.fixes.GenerateFromSignature;
import com.perniciouspenguins.ideaz.javadoc.fixes.IntroduceReference;
import com.perniciouspenguins.ideaz.javadoc.fixes.LocalQuickFixBase;
import com.perniciouspenguins.ideaz.javadoc.inspections.ui.JavaDocSyncMethodOptionsPanel;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The MissingMethodJavaDocInspection highlights all methods
 * that do not define JavaDoc.
 *
 * Author: Raymond Brandon
 * Date: Dec 23, 2005 9:37:26 PM
 */
public class MissingMethodJavaDocInspection extends JavaDocBaseInspection
{
  /**
   * Defines the name of the inspection under the group display name.
   * @return the name of this inspection
   */
  @NonNls
  @NotNull
  public String getDisplayName()
  {
    return "Missing Method JavaDoc declaration";
  }

  /**
   * Defines the short name of the inspection.
   * @return the short name of this inspection
   */
  @NonNls
  @NotNull
  public String getShortName()
  {
    return "MissingMethodJavaDoc";
  }

  /**
   * @return null if no UI options required
   */
  @Nullable
  public JComponent createOptionsPanel()
  {
    return new JavaDocSyncMethodOptionsPanel( this );
  }

  /**
   * The currently scanned method does not have JavaDoc.
   *  if method is impl/override and interface/base has javadoc
   *    - offer introduce reference
   *    - offer copy from parent
   *  else
   *    - offer to generate
   *
   * @return a collection of ProblemDescriptors or an empty collection.
   * @param method the method to check
   * @param manager the inspection manager to use
   */
  protected List<ProblemDescriptor> determineIntroduceDocOptions( PsiMethod method,
                                                                  InspectionManager manager,
                                                                  boolean onTheFly )
  {
    List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();
    ProblemDescriptor problemDescriptor;

    String descriptionTemplate = determineIntroduceDocOptions( method, fixes );
    if( null != descriptionTemplate )
    {
      PsiIdentifier psiIdentifier = method.getNameIdentifier();
      if( null != psiIdentifier )
      {
        problemDescriptor = manager.createProblemDescriptor( psiIdentifier,
                                                             descriptionTemplate,
                                                             onTheFly,
                                                             fixes.toArray( new LocalQuickFix[fixes.size()] ),
                                                             ProblemHighlightType.GENERIC_ERROR_OR_WARNING );
        problems.add( problemDescriptor );
      }
    }
    return problems;
  }

  /**
   * Method for determining the cause and the solution for the missing JavaDoc for the
   * specified PsiMethod.
   * @param method the PsiMethod that needs to be analyzed.
   * @param fixes the list of LocalQuickFixBase objects for possible solutions.
   * @return the message text describing the missing JavaDoc.
   */
  private String determineIntroduceDocOptions( PsiMethod method,
                                               List<LocalQuickFixBase> fixes )
  {
    String descriptionTemplate = null;

    if( null != method && method.getDocComment() == null && null != fixes )
    {
      PsiMethod superMethod = getSuperMethod( method );

      descriptionTemplate = determineDescriptionTemplate( method, superMethod );
      if( null != superMethod )
      {
        PsiDocComment superDocComment = superMethod.getDocComment();
        IntroduceReference introduceReference = new IntroduceReference( method, superMethod, useSingleLineReferences );
        if( null != superDocComment )
        {
          fixes.add( new CopyFromParent( method, superMethod ) );
          fixes.add( introduceReference );
        }
        else
        {
          fixes.add( new GenerateFromSignature( method ) );
        }

        if( PsiUtils.isAnonymous( method ) && !fixes.contains( introduceReference ) )
        {
          fixes.add( introduceReference );
        }
      }
      else
      {
        fixes.add( new GenerateFromSignature( method ) );
        if( PsiUtils.isAnonymous( method ) )
        {
          @SuppressWarnings( {"ConstantConditions"} )
          PsiJavaCodeReferenceElement baseReference =
              ((PsiAnonymousClass) method.getContainingClass()).getBaseClassReference();
          PsiClass psiClass =
              JavaPsiFacade.getInstance( method.getProject() )
                  .findClass( baseReference.getCanonicalText(), ProjectScope.getAllScope( method.getProject() ) );
          if( null != psiClass )
          {
            PsiMethod virtualSuperMethod = psiClass.findMethodBySignature( method, false );
            if( null != virtualSuperMethod )
            {
              fixes.add( new IntroduceReference( method, virtualSuperMethod, useSingleLineReferences ) );
            }
          }
        }
      }
    }
    return descriptionTemplate;
  }
}
