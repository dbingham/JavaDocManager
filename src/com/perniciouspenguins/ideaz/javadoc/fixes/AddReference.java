package com.perniciouspenguins.ideaz.javadoc.fixes;

import com.intellij.psi.PsiMethod;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The AddReference offers the ability to add a JavaDoc
 * reference to the existing JavaDoc which references to
 * the parent method. This fix will preserve the existing
 * JavaDoc which differs with the JavaDoc in the parent 
 * method and will add a JavaDoc reference.
 *
 * Author: Raymond Brandon
 * Date: Jan 7, 2007 5:06:57 PM
 */
public class AddReference extends LocalQuickFixBase {
    private boolean useSingleLineReferences;

    /**
     * Method LocalQuickFixBase creates a new instance of a Quick fix for a method that has a parent
     * method.
     *
     * @param method the method to which the quick fix applies.
     * @param superMethod the super method of which the JavaDoc is taken into account when offering
     * quick fixes.
     * @param useSingleLineReferences indicates whether the plugin is configured to use single
     * line references or three lines
     */
    public AddReference(PsiMethod method, PsiMethod superMethod, boolean useSingleLineReferences) {
        super(method, superMethod);
        this.useSingleLineReferences = useSingleLineReferences;
    }

    /**
     * Returns the name of the quick fix.
     *
     * @return the name of the quick fix.
     */
    @NotNull
    public String getName () {
        return FIX_ADD_REFERENCE;
    }

    /**
     * Method doFix returns the actual fix that needs to be executed to solve the
     * detected problem.
     */
    public void doFix() {
        PsiUtils.addReference(method, superMethod, true, useSingleLineReferences);
    }
}
