package com.perniciouspenguins.ideaz.javadoc.inspections;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.perniciouspenguins.ideaz.javadoc.fixes.LocalQuickFixBase;
import com.perniciouspenguins.ideaz.javadoc.inspections.ui.JavaDocSyncMethodOptionsPanel;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class offering shared functionality for all inspection options.
 *
 * Author: Raymond Brandon Date: Dec 23, 2005 9:37:26 PM
 */
@SuppressWarnings( {"UnusedParameters"} )
public abstract class JavaDocBaseInspection extends BaseJavaLocalInspectionTool
{
    public String methodDetectionLevel = JavaDocSyncMethodOptionsPanel.METHOD_ACCESS_PRIVATE;
    public static boolean checkInnerClasses = true;
    public boolean checkAnonymousClasses = true;
    public boolean useSingleLineReferences = true;

    /**
     * Method getGroupDisplayName defines the name of the group of the inspections under which all sub classes of this
     * class are ordered.
     *
     * @return String
     */
    @NonNls
    @NotNull
    public final String getGroupDisplayName()
    {
        return "JavaDoc Issues";
    }

    /**
     * Defines the name of the inspection under the group display name.
     *
     * @return the name of this inspection
     */
    @NonNls
    @NotNull
    public abstract String getDisplayName();

    /**
     * Defines the short name of the inspection.
     *
     * @return the short name of this inspection
     */
    @NonNls
    @NotNull
    public abstract String getShortName();

    /**
     * Override this to report problems at class level.
     *
     * @param psiClass   to check.
     * @param manager    InspectionManager to ask for ProblemDescriptor's from.
     * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
     *
     * @return <code>null</code> if no problems found or not applicable at class level.
     */
    @Nullable
    public ProblemDescriptor[] checkClass( @NotNull final PsiClass psiClass,
                                           @NotNull final InspectionManager manager,
                                           final boolean isOnTheFly )
    {
        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();
        ApplicationManager.getApplication().runReadAction( new Runnable()
        {
            /**
             * Method run contains the action that needs to be executed when the dispatch thread
             * allows write actions.
             */
            public void run()
            {
                boolean isInnerClass = PsiUtils.isInnerClass( psiClass );
                boolean isAnonymous = PsiUtils.isAnonymous( psiClass );

                if( (isInnerClass && !checkInnerClasses) || (isAnonymous && !checkAnonymousClasses) )
                {
                    return;
                }

                descriptors.addAll( determineIntroduceDocOptions( psiClass, manager, isOnTheFly ) );
            }
        } );
        if( descriptors.isEmpty() )
        {
            return super.checkClass( psiClass, manager, isOnTheFly );
        }
        else
        {
            return descriptors.toArray( new ProblemDescriptor[descriptors.size()] );
        }
    }

    /**
     * Override this to report problems at method level.
     *
     * @param method     to check.
     * @param manager    InspectionManager to ask for ProblemDescriptor's from.
     * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
     *
     * @return <code>null</code> if no problems found or not applicable at method level.
     */
    @Nullable
    public ProblemDescriptor[] checkMethod( @NotNull final PsiMethod method,
                                            @NotNull final InspectionManager manager,
                                            final boolean isOnTheFly )
    {
        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();
        ApplicationManager.getApplication().runReadAction( new Runnable()
        {
            /**
             * Method run contains the action that needs to be executed when the dispatch thread
             * allows write actions.
             */
            public void run()
            {
                boolean isAnonymous = PsiUtils.isAnonymous( method.getContainingClass() );
                boolean isInnerClass = PsiUtils.isInnerClass( method.getContainingClass() );

                if( !PsiUtils.accessModifierLevelInRange( method.getModifierList(), methodDetectionLevel ) ||
                    ( isAnonymous && !checkAnonymousClasses ) ||
                    ( isInnerClass && !checkInnerClasses ) )
                {
                    return;
                }

                PsiDocComment docComment = method.getDocComment();
                if( docComment == null )
                {
                    descriptors.addAll( determineIntroduceDocOptions( method, manager, isOnTheFly ) );
                }
                else
                {
                    descriptors.addAll( determineDocDifferences( method, manager, isOnTheFly ) );
                }
            }
        } );
        if( descriptors.isEmpty() )
        {
            return super.checkMethod( method, manager, isOnTheFly );
        }
        else
        {
            return descriptors.toArray( new ProblemDescriptor[descriptors.size()] );
        }
    }

    /**
     * Override this to report problems at field level.
     *
     * @param field      to check.
     * @param manager    InspectionManager to ask for ProblemDescriptor's from.
     * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
     *
     * @return <code>null</code> if no problems found or not applicable at field level.
     */
    @Nullable
    public ProblemDescriptor[] checkField( @NotNull final PsiField field,
                                           @NotNull final InspectionManager manager,
                                           final boolean isOnTheFly )
    {
        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();
        ApplicationManager.getApplication().runReadAction( new Runnable()
        {
            /**
             * Method run contains the action that needs to be executed when the dispatch thread
             * allows write actions.
             */
            public void run()
            {
                boolean isAnonymous = PsiUtils.isAnonymous( field.getContainingClass() );
                boolean isInnerClass = PsiUtils.isInnerClass( field.getContainingClass() );

                if( ( isAnonymous && !checkAnonymousClasses ) || ( isInnerClass && !checkInnerClasses ) )
                {
                    return;
                }

                PsiDocComment docComment = field.getDocComment();
                if( docComment == null )
                {
                    descriptors.addAll( determineIntroduceDocOptions( field, manager, isOnTheFly ) );
                }
            }
        } );
        if( descriptors.isEmpty() )
        {
            return super.checkField( field, manager, isOnTheFly );
        }
        else
        {
            return descriptors.toArray( new ProblemDescriptor[descriptors.size()] );
        }
    }

    /**
     * The currently scanned class does not have JavaDoc.
     *
     * @param psiClass the class to check
     * @param manager  the inspection manager to use
     * @param onTheFly if this is on the fly or not
     *
     * @return a collection of ProblemDescriptors or an empty collection.
     */
    protected List<ProblemDescriptor> determineIntroduceDocOptions( PsiClass psiClass,
                                                                    InspectionManager manager,
                                                                    boolean onTheFly )
    {
        return new ArrayList<ProblemDescriptor>();
    }

    /**
     * The currently scanned method does not have JavaDoc. if method is impl/override and interface/base has javadoc -
     * offer introduce reference - offer copy from parent else - offer to generate
     *
     * @param method  the method to check
     * @param manager the inspection manager to use
     * @param onTheFly if this is on the fly or not
     *
     * @return a collection of ProblemDescriptors or an empty collection.
     */
    protected List<ProblemDescriptor> determineIntroduceDocOptions( PsiMethod method,
                                                                    InspectionManager manager,
                                                                    boolean onTheFly )
    {
        return new ArrayList<ProblemDescriptor>();
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
    protected List<ProblemDescriptor> determineDocDifferences( PsiMethod method,
                                                               InspectionManager manager,
                                                               boolean onTheFly )
    {
        return new ArrayList<ProblemDescriptor>();
    }

    /**
     * The currently scanned field does not have JavaDoc.
     *
     * @param field   the field to check
     * @param manager the inspection manager to use
     * @param onTheFly if this is on the fly or not
     *
     * @return a collection of ProblemDescriptors or an empty collection.
     */
    protected List<ProblemDescriptor> determineIntroduceDocOptions( PsiField field,
                                                                    InspectionManager manager,
                                                                    boolean onTheFly )
    {
        return new ArrayList<ProblemDescriptor>();
    }


    /**
     * Method to find the specified psiMethod in the parent class/interface
     *
     * @param psiMethod the method of which the super implementation/definition is requested
     *
     * @return the superMethod or null if the direct super class/interface (if available) does not define the specified
     *         method or is the containing class of the method does not extend or implement another class/interface.
     */
    @Nullable
    protected PsiMethod getSuperMethod( PsiMethod psiMethod )
    {
        if( null != psiMethod )
        {
            PsiClass containingClass = psiMethod.getContainingClass();
            if( null != containingClass )
            {
                return PsiUtils.checkParents( containingClass.getSuperClass(), psiMethod );
            }
        }
        return null;
    }

    /**
     * Method to determine the correct description template based on the definition of the specified parameters.
     *
     * @param method      the PsiMethod being checked
     * @param superMethod the super PsiMethod or null if this method has no super method
     *
     * @return the description template or null if none could be determined.
     */
    @SuppressWarnings( {"ConstantConditions"} )
    protected String determineDescriptionTemplate( PsiMethod method, PsiMethod superMethod )
    {
        boolean isInterfaceMethod = method != null && method.getContainingClass().isInterface();
        boolean superIsInterface = superMethod != null && superMethod.getContainingClass().isInterface();
        boolean superIsObject = superMethod != null && PsiUtils.isFromObjectClass( superMethod );

        String descriptionTemplate = null;
        if( null != method )
        {
            if( null != superMethod )
            {
                if( isInterfaceMethod )
                {
                    // Interface extending other interface, overriding declared method
                    descriptionTemplate = LocalQuickFixBase.OVERRIDDEN_METHOD_NO_JAVADOC;
                }
                else
                {
                    if( superIsInterface )
                    {
                        // Class implementing interface method
                        descriptionTemplate = LocalQuickFixBase.IMPLEMENTATION_NO_JAVADOC;
                    }
                    else if( superIsObject )
                    {
                        // Base class declaring method
                        descriptionTemplate = LocalQuickFixBase.BASE_METHOD_NO_JAVADOC;
                    }
                    else
                    {
                        // Class extending other class, overriding declared method
                        descriptionTemplate = LocalQuickFixBase.OVERRIDDEN_METHOD_NO_JAVADOC;
                    }
                }
            }
            else
            {
                if( isInterfaceMethod )
                {
                    // Base interface declaring method
                    descriptionTemplate = LocalQuickFixBase.INTERFACE_METHOD_NO_JAVADOC;
                }
                else if( method.getContainingClass() instanceof PsiAnonymousClass )
                {
                    // Method is an anonymous override
                    descriptionTemplate = LocalQuickFixBase.OVERRIDDEN_METHOD_ANONYMOUS_CLASS_NO_JAVADOC;
                }
                else
                {
                    // Base class declaring method
                    descriptionTemplate = LocalQuickFixBase.BASE_METHOD_NO_JAVADOC;
                }
            }
        }
        return descriptionTemplate;
    }
}
