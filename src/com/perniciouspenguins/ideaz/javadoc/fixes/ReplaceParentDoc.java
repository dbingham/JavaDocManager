package com.perniciouspenguins.ideaz.javadoc.fixes;

import com.intellij.psi.PsiMethod;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The ReplaceParentDoc offers the ability to copy the JavaDoc
 * from the current method to the method in the parent class.
 *
 * Author: Raymond Brandon
 * Date: Dec 24, 2005 6:29:36 PM
 */
public class ReplaceParentDoc extends LocalQuickFixBase {
    /**
     * Method LocalQuickFixBase creates a new instance of a Quick fix for a method that has a parent
     * method.
     *
     * @param method the method to which the quick fix applies.
     * @param superMethod the super method of which the JavaDoc is taken into account when offering
     * quick fixes.
     */
    public ReplaceParentDoc(PsiMethod method, PsiMethod superMethod) {
        super(method, superMethod);
    }

    /**
     * Returns the name of the quick fix.
     *
     * @return the name of the quick fix.
     */
    @NotNull
    public String getName() {
        if (superMethod.getDocComment() != null) {
            return FIX_REPLACE_PARENT_DOC;
        } else {
            return FIX_COPY_DOC_TO_PARENT;
        }
    }

    /**
     * Method doFix returns the actual fix that needs to be executed to solve the
     * detected problem.
     */
    public void doFix() {
        PsiUtils.copyPsiDocComment(method, superMethod);
    }
}
