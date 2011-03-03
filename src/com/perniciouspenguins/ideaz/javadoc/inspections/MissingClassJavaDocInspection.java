package com.perniciouspenguins.ideaz.javadoc.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.perniciouspenguins.ideaz.javadoc.fixes.GenerateForAllMethods;
import com.perniciouspenguins.ideaz.javadoc.fixes.GenerateFromClass;
import com.perniciouspenguins.ideaz.javadoc.fixes.LocalQuickFixBase;
import com.perniciouspenguins.ideaz.javadoc.inspections.ui.JavaDocSyncClassOptionsPanel;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The MissingClassJavaDocInspection highlights class declarations that are not preceeded by a JavaDoc declaration.
 *
 * Author: Raymond Brandon Date: Dec 30, 2005 9:06:50 PM
 */
public class MissingClassJavaDocInspection extends JavaDocBaseInspection
{

    /**
     * Defines the name of the inspection under the group display name.
     *
     * @return the name of this inspection
     */
    @NotNull
    public String getDisplayName()
    {
        return "Missing Class JavaDoc declaration";
    }

    /**
     * Defines the short name of the inspection.
     *
     * @return the short name of this inspection
     */
    @NotNull
    public String getShortName()
    {
        return "MissingClassJavaDoc";
    }

    /**
     * @return null if no UI options required
     */
    @Nullable
    public JComponent createOptionsPanel()
    {
        return new JavaDocSyncClassOptionsPanel();
    }

    /**
     * The currently scanned class does not have JavaDoc.
     *
     * @param psiClass the class to check
     * @param manager  the inspection manager to use
     *
     * @return a collection of ProblemDescriptors or an empty collection.
     */
    protected List<ProblemDescriptor> determineIntroduceDocOptions( PsiClass psiClass,
                                                                    InspectionManager manager,
                                                                    boolean onTheFLy )
    {
        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();
        ProblemDescriptor problemDescriptor;
        String descriptionTemplate = null;

        if( PsiUtils.isInnerClass( psiClass ) && !checkInnerClasses )
        {
            return problems;
        }

        // Check all methods and allow generation of JavaDoc for all methods at once
        PsiMethod[] psiMethods = psiClass.getMethods();
        boolean foundMethodsWithoutJavaDoc = false;
        for( int i = 0; !foundMethodsWithoutJavaDoc && i < psiMethods.length; i++ )
        {
            PsiMethod psiMethod = psiMethods[i];
            if( null == psiMethod.getDocComment() )
            {
                foundMethodsWithoutJavaDoc = true;
            }
        }

        PsiIdentifier psiIdentifier = psiClass.getNameIdentifier();
        if( psiClass.getDocComment() == null )
        {
            if( psiClass.isInterface() )
            {
                descriptionTemplate = LocalQuickFixBase.INTERFACE_DEFINITION_NO_JAVADOC;
            }
            else if( psiClass.isEnum() )
            {
                descriptionTemplate = LocalQuickFixBase.ENUM_DEFINITION_NO_JAVADOC;
            }
            else if( psiClass.getName() != null )
            {
                descriptionTemplate = LocalQuickFixBase.CLASS_DEFINITION_NO_JAVADOC;
            }
            fixes.add( new GenerateFromClass( psiClass ) );
            if( null != psiIdentifier && null != descriptionTemplate )
            {
                problemDescriptor = manager.createProblemDescriptor( psiIdentifier,
                                                                     descriptionTemplate,
                                                                     onTheFLy,
                                                                     fixes.toArray( new LocalQuickFix[fixes.size()] ),
                                                                     ProblemHighlightType.GENERIC_ERROR_OR_WARNING );
                problems.add( problemDescriptor );
            }
        }

        if( foundMethodsWithoutJavaDoc && !PsiUtils.isAnonymous( psiClass ) )
        {
            descriptionTemplate = LocalQuickFixBase.ONE_OR_MORE_METHODS_DO_NOT_DEFINE_JAVADOC;
            fixes.clear();
            fixes.add( new GenerateForAllMethods( psiClass ) );
            if( null != psiIdentifier )
            {
                problemDescriptor = manager.createProblemDescriptor( psiIdentifier,
                                                                     descriptionTemplate,
                                                                     onTheFLy,
                                                                     fixes.toArray( new LocalQuickFix[fixes.size()] ),
                                                                     ProblemHighlightType.GENERIC_ERROR_OR_WARNING );
                problems.add( problemDescriptor );
            }
        }
        return problems;
    }
}
