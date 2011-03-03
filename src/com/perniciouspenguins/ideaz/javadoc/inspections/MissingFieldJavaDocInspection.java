package com.perniciouspenguins.ideaz.javadoc.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiField;
import com.perniciouspenguins.ideaz.javadoc.fixes.GenerateFromField;
import com.perniciouspenguins.ideaz.javadoc.fixes.LocalQuickFixBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The MissingFieldJavaDocInspection highlights field declarations that
 * are not preceeded by a JavaDoc declaration.
 *
 * Author: Raymond Brandon
 * Date: Dec 30, 2005 9:06:50 PM
 */
public class MissingFieldJavaDocInspection extends JavaDocBaseInspection {
    /**
     * Defines the name of the inspection under the group display name.
     * @return the name of this inspection
     */
    @NotNull
    public String getDisplayName() {
        return "Missing Field JavaDoc declaration";
    }

    /**
     * Defines the short name of the inspection.
     * @return the short name of this inspection
     */
    @NotNull
    public String getShortName() {
        return "MissingFieldJavaDoc";
    }

    /**
     * The currently scanned field does not have JavaDoc.
     *
     * @return a collection of ProblemDescriptors or an empty collection.
     * @param field the field to check
     * @param manager the inspection manager to use
     */
    protected List<ProblemDescriptor> determineIntroduceDocOptions(PsiField field,
                                                                   InspectionManager manager,
                                                                   boolean onTheFly ) {
        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        List<LocalQuickFixBase> fixes    = new ArrayList<LocalQuickFixBase>();

        ProblemDescriptor problemDescriptor;
        String descriptionTemplate;

        if (field.getDocComment() == null) {
            descriptionTemplate = LocalQuickFixBase.FIELD_DEFINITION_NO_JAVADOC;
            fixes.add(new GenerateFromField(field));

            problemDescriptor = manager.createProblemDescriptor(field.getNameIdentifier(),
                                                                descriptionTemplate,
                                                                onTheFly,
                                                                fixes.toArray(new LocalQuickFix[fixes.size()]),
                                                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            problems.add(problemDescriptor);
        }
        return problems;
    }
}
