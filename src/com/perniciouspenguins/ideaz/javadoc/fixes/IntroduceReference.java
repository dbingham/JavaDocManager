package com.perniciouspenguins.ideaz.javadoc.fixes;

import com.intellij.psi.PsiMethod;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The IntroduceReference offers the ability to replace the existing JavaDoc
 * with a reference to the parent method.
 *
 * Author: Raymond Brandon
 * Date: Dec 24, 2005 6:29:36 PM
 */
public class IntroduceReference extends LocalQuickFixBase {
    private boolean useSingleLineReferences;

    /**
     * Method LocalQuickFixBase creates a new instance of a Quick fix for a method that has a parent
     * method.
     *
     * @param method the method to which the quick fix applies.
     * @param superMethod the super method of which the JavaDoc is taken into account when offering
     * @param useSingleLineReferences indicates whether the plugin is configured to use single
     * line references or three lines
     */
    public IntroduceReference(PsiMethod method, PsiMethod superMethod, boolean useSingleLineReferences) {
        super(method, superMethod);
        this.useSingleLineReferences = useSingleLineReferences;
    }

    /**
     * Returns the name of the quick fix.
     *
     * @return the name of the quick fix.
     */
    @NotNull
    public String getName() {
        return FIX_INTRODUCE_REFERENCE;
    }

    /**
     * Method doFix returns the actual fix that needs to be executed to solve the
     * detected problem.
     */
    public void doFix() {
        PsiUtils.addReference(method, superMethod, false, useSingleLineReferences);
    }
}
