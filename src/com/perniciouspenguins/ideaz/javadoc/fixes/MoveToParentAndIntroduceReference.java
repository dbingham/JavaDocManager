package com.perniciouspenguins.ideaz.javadoc.fixes;

import com.intellij.psi.PsiMethod;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The MoveToParentAndIntroduceReference offers the ability to move
 * the existing JavaDoc from the overridden/implementation method to
 * the method in the parent class and replace the existing JavaDoc
 * with a reference to the method in the parent class.
 *
 * Author: Raymond Brandon
 * Date: Dec 24, 2005 6:29:36 PM
 */
public class MoveToParentAndIntroduceReference extends LocalQuickFixBase {
    private boolean useSingleLineReference = false;

    /**
     * Method LocalQuickFixBase creates a new instance of a Quick fix for a method that has a parent
     * method.
     *
     * @param method the method to which the quick fix applies.
     * @param superMethod the super method of which the JavaDoc is taken into account when offering
     * quick fixes.
     * @param useSingleLineReference boolean
     */
    public MoveToParentAndIntroduceReference(PsiMethod method, PsiMethod superMethod, boolean useSingleLineReference) {
        super(method, superMethod);
        this.useSingleLineReference = useSingleLineReference;
    }

    /**
     * Returns the name of the quick fix.
     *
     * @return the name of the quick fix.
     */
    @NotNull
    public String getName() {
        return FIX_MOVE_TO_PARENT_INTRODUCE_REFERENCE;
    }

    /**
     * Method doFix returns the actual fix that needs to be executed to solve the
     * detected problem.
     */
    public void doFix() {
        PsiUtils.copyPsiDocComment(method, superMethod);
        // Create new reference javaDoc
        PsiUtils.addReference(method, superMethod, false, useSingleLineReference);
    }
}
