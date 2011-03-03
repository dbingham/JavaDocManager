package com.perniciouspenguins.ideaz.javadoc.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.perniciouspenguins.ideaz.javadoc.fixes.AddReference;
import com.perniciouspenguins.ideaz.javadoc.fixes.CopyFromParent;
import com.perniciouspenguins.ideaz.javadoc.fixes.LocalQuickFixBase;
import com.perniciouspenguins.ideaz.javadoc.fixes.MoveToParentAndIntroduceReference;
import com.perniciouspenguins.ideaz.javadoc.fixes.ReplaceParentDoc;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The InconsistentJavaDocInspection reports differences between the JavaDoc of the scanned method and the JavaDoc in
 * the parent method or interface method declaration. <p/> Author: Raymond Brandon Date: Dec 26, 2005 1:53:04 PM
 */
public class InconsistentJavaDocInspection extends JavaDocBaseInspection
{
    /**
     * Defines the name of the inspection under the group display name.
     *
     * @return the name of this inspection
     */
    @NotNull
    public String getDisplayName()
    {
        return "Inconsistent JavaDoc";
    }

    /**
     * Defines the short name of the inspection.
     *
     * @return the short name of this inspection
     */
    @NotNull
    public String getShortName()
    {
        return "InconsistentJavaDoc";
    }

    /**
     * if Method has superMethod if superMethod has JavaDoc if JavaDoc superMethod equals method JavaDoc OK else if
     * JavaDoc superMethod doesn't equals method JavaDoc if method JavaDoc has reference to superMethod OK else if method
     * doesn't have reference WARN "Method JavaDoc differences from super method JavaDoc" - offer introduce reference -
     * offer copy from parent else if superMethod has no JavaDoc WARN "Method definition does not have any JavaDoc" -
     * offer to copy to parent - offer to move doc to parent and introduce reference else WARN "Method definition does not
     * have any JavaDoc" - offer to generate JavaDoc based on method sigmature
     *
     * @param method  the method to check
     * @param manager the inspection manager to use
     * @param onTheFly if this is on the fly or not
     *
     * @return a collection of ProblemDescriptors or an empty collection.
     */
    protected List<ProblemDescriptor> determineDocDifferences( @NotNull PsiMethod method,
                                                               @NotNull InspectionManager manager,
                                                               boolean onTheFly )
    {
        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        List<LocalQuickFix> fixes = new ArrayList<LocalQuickFix>();

        String descriptionTemplate = findFixesForInconsistency( getSuperMethod( method ), method, fixes );
        if( descriptionTemplate != null )
        {
            PsiIdentifier psiIdentifier = method.getNameIdentifier();
            if( null != psiIdentifier )
            {
                ProblemDescriptor problemDescriptor = manager.createProblemDescriptor( psiIdentifier,
                                                                                       descriptionTemplate,
                                                                                       onTheFly,
                                                                                       fixes.toArray(
                                                                                               new LocalQuickFix[fixes
                                                                                                       .size()] ),
                                                                                       ProblemHighlightType.GENERIC_ERROR_OR_WARNING );
                problems.add( problemDescriptor );
            }
        }
        return problems;
    }

    /**
     * Method findFixesForInconsistency determines which fixes are valid for the code inconsistency described by the
     * JavaDoc of the method and optionally that of its super method.
     *
     * @param superMethod the super method, optionally describing JavaDoc
     * @param method      the method with JavaDoc
     * @param fixes       the collection of quick fixes that can be applied
     *
     * @return String the description template of the inconsistency that was detected or null if the inconsistency could
     *         not be determined.
     */
    private String findFixesForInconsistency( PsiMethod superMethod, PsiMethod method, List<LocalQuickFix> fixes )
    {
        String descriptionTemplate = null;
        PsiDocComment docComment = method.getDocComment();
        if( !method.isConstructor() && null != docComment )
        {
            if( null == superMethod && PsiUtils.definesInheritDocTag( method ) && !PsiUtils.isAnonymous( method ) )
            {
                descriptionTemplate = LocalQuickFixBase.NO_SUPER_METHOD_TO_INHERIT_DOC_FROM;
            }
            else if( null != superMethod && !PsiUtils.isFromObjectClass( superMethod ) )
            {
                PsiDocComment superDocComment = superMethod.getDocComment();
                if( null != superDocComment &&
                    !PsiUtils.docsAreEqual( docComment, superDocComment ) &&
                    !PsiUtils.hasReferenceToParentMethod( method, superMethod ) &&
                    !PsiUtils.hasInheritedJavaDoc( method, superMethod ) )
                {
                    descriptionTemplate = LocalQuickFixBase.JAVADOC_DIFFERS_FROM_PARENT;
                    fixes.add( new AddReference( method, superMethod, useSingleLineReferences ) );
                    fixes.add( new CopyFromParent( method, superMethod ) );
                }
                else if( null == superDocComment && PsiUtils.definesInheritDocTag( method ) )
                {
                    descriptionTemplate = LocalQuickFixBase.NO_JAVADOC_IN_SUPER_METHOD_TO_INHERIT_FROM;
                }
                // If we cannot write to the parent there's no sense in
                // allowing options to change the parent
                else if( null == superDocComment && superMethod.getContainingFile().isWritable() )
                {
                    descriptionTemplate = LocalQuickFixBase.JAVADOC_DIFFERS_FROM_PARENT;
                    fixes.add( new MoveToParentAndIntroduceReference( method, superMethod, useSingleLineReferences ) );
                    fixes.add( new ReplaceParentDoc( method, superMethod ) );
                }
            }
        }
        return descriptionTemplate;
    }
}
