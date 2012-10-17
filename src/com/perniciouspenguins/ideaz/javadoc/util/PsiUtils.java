package com.perniciouspenguins.ideaz.javadoc.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.util.IncorrectOperationException;
import com.perniciouspenguins.ideaz.javadoc.inspections.ui.JavaDocSyncMethodOptionsPanel;
import org.apache.commons.lang.StringUtils;

/**
 * Utility class for Psi operations.
 *
 * Author: Raymond Brandon
 * Date: Jan 14, 2007
 *
 * Author: Daniel Bingham
 * Date: Apr 14, 2009
 */
@SuppressWarnings( {"ConstantConditions"} )
public final class PsiUtils
{
    /** Field log: logger for this class **/
    private static final Logger log = Logger.getInstance( "JavaDocManager" );
    private static final String TOKEN_INHERIT_DOC = "@inheritDoc";
    private static final String PSI_MODIFIER_STATIC = "static";

    /**
     * Method to retrieve the package name of the specified class.
     *
     * @param clazz the PSI class of which the packae needs to be determined
     * @return the package name or empty string fo default package.
     */
    public static String getPackage( PsiClass clazz )
    {
        return ((PsiJavaFile) clazz.getContainingFile()).getPackageName();
    }


    /**
     * Method to determine whether the specified psiMethod is owned by the Object class.
     * @param psiMethod the method to check.
     * @return true if this method is owned by the Object class, false otherwise.
     */
    public static boolean isFromObjectClass( PsiMethod psiMethod )
    {
        return null != psiMethod && psiMethod.getContainingClass() != null &&
               "Object".equals( psiMethod.getContainingClass().getName() );
    }

    public static boolean docsAreEqual( PsiDocComment psiMethod, PsiDocComment superMethod )
    {
        String methodComment = psiMethod.getText().replaceAll( "(?m)^\\s*", "" ).replaceAll("(?m)\\s*$","");
        String parentComment = superMethod.getText().replaceAll( "(?m)^\\s*", "" ).replaceAll("(?m)\\s*$","");

        return StringUtils.equals( methodComment, parentComment );
    }

    /**
     * Method to determine whether the current scanned method contains
     * a JavaDoc reference to the parent definition.
     * @param superMethod the parent method to compare to
     * @param psiMethod the overridden or implementing method to check
     * @return true if the method defines JavaDoc @see methodName<signature>, false otherwise
     */
    public static boolean hasReferenceToParentMethod( PsiMethod psiMethod, PsiMethod superMethod )
    {
        boolean isReference = false;
        if( null != superMethod )
        {
            PsiComment psiComment = psiMethod.getDocComment();
            if( null != psiComment )
            {
                String comment = psiComment.getText();
                int startIndex = comment.indexOf( "@see" );
                if( startIndex != -1 )
                {
                    String pattern = generateReference( psiMethod, superMethod, true );

                    int patternEnd = comment.indexOf( ")", startIndex ) + 1;
                    if( patternEnd < startIndex )
                    {
                        patternEnd = startIndex;
                    }

                    String thisReference = comment.substring( startIndex, patternEnd );

                    if( thisReference.matches( pattern ) )
                    {
                        isReference = true;
                    }
                }
            }
        }
        return isReference;
    }

    /**
     * Method hasInheritedJavaDoc checks whether the specified method uses the @InheritDoc keyword
     * and verifies that the parent method actually defines JavaDoc to inherit from.
     *
     * @param method the method to check
     * @param superMethod the super method defining JavaDoc
     * @return true if the method contains the @Inherit leyword and the parent method defines JavaDoc.
     */
    public static boolean hasInheritedJavaDoc( PsiMethod method, PsiMethod superMethod )
    {
        boolean result = false;
        if( !isStatic( method ) && null != superMethod && definesInheritDocTag( method ) )
        {
            PsiDocComment superJavaDocComment = superMethod.getDocComment();
            if( null != superJavaDocComment )
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Method definesInheritDocTag determines whether the specified method defines the
     * @inheritDoc tag in the JavaDoc declaration.
     *
     * @param method of type PsiMethod
     * @return boolean true if the tag is found, false otherwise.
     */
    @SuppressWarnings( {"InconsistentJavaDoc"} )
    public static boolean definesInheritDocTag( PsiMethod method )
    {
        PsiDocComment docComment = method.getDocComment();
        return null != docComment && docComment.getText().indexOf( TOKEN_INHERIT_DOC ) != -1;
    }

    /**
     * Method to generate a JavaDoc reference based on the method and it's parent method.
     * @param method the method that possibly defines a reference to the parent method
     * @param superMethod the parent method that is being referenced
     * @param isRegExp flag to indicate whether the reference should be a regular expression or not
     * @return the JavaDoc reference based on the two methods
     */
    public static String generateReference( PsiMethod method, PsiMethod superMethod, boolean isRegExp )
    {
        StringBuffer qualifiedReference = new StringBuffer();
        if( isRegExp )
        {
            qualifiedReference.append( "^" );
        }
        qualifiedReference.append( "@see " );

        PsiClass superClass = superMethod.getContainingClass();
        String superPackageName = PsiUtils.getPackage( superClass );
        String packageName = PsiUtils.getPackage( method.getContainingClass() );

        if( isRegExp )
        {
            qualifiedReference
                    .append( "(" )
                    .append( "(?!" )  // Package not present
                    .append( superPackageName ).append( "." )
                    .append( ")" )
                    .append( "|" )    // or
                    .append( "(?:" )  // Package is present
                    .append( superPackageName ).append( "." )
                    .append( ")" )
                    .append( ")" );
        }
        else if( !packageName.equals( superPackageName ) )
        {
            qualifiedReference.append( superPackageName ).append( "." );
        }
        qualifiedReference.append( superClass.getName() ).append( "#" );

        String superMethodSignature = getSignature( superMethod )
                .replaceAll( "\\(", "\\\\(" )
                .replaceAll( "\\)", "\\\\)" )
                .replaceAll( " ", "[\\\\s]*" );
        qualifiedReference.append( superMethod.getName() ).append( superMethodSignature );

        String reference = qualifiedReference.toString();
        if( isRegExp )
        {
            reference = reference.replaceAll( " ", "[\\\\s]+" );
        }
        return reference;
    }

    /**
     * Method createReferenceComment creates a JavaDoc reference to the super class
     * using the &quot;@see&quot; JavaDoc keyword.
     *
     * @param psiSuperMethod the method on which the reference should be based.
     *
     * @return String a JavaDoc reference to the parent method.
     */
    public static String createReferenceComment( PsiMethod psiSuperMethod )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "@see " )
                .append( psiSuperMethod.getContainingClass().getQualifiedName() )
                .append( "#" )
                .append( psiSuperMethod.getName() )
                .append( getSignature( psiSuperMethod ) );
        return sb.toString();
    }

    /**
     * Method getSignature generates a String containing the signature of the
     * specified method without the method name and parameters.
     *
     * @param psiMethod the method to get the signature from.
     * @return String the signature without the method name.
     */
    private static String getSignature( PsiMethod psiMethod )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "(" );
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        for( int i = 0; i < parameters.length; i++ )
        {
            PsiParameter parameter = parameters[i];
            sb.append( parameter.getTypeElement().getText().replaceAll( "\\[", "\\\\[" ).replaceAll( "\\]", "\\\\]" ) );
            // As a workaround for the bug were intellij removes the spaces between the parameter type
            // and the parameter name, the JavaDoc reference will consist of only the type
            //                    .append(" ")
            //                    .append(parameter.getName());
            if( i < parameters.length - 1 )
            {
                sb.append( ", " );
            }
        }
        sb.append( ")" );
        return sb.toString();
    }


    /**
     * Method copyPsiDocComment copies the JavaDoc from the source PsiMethod
     * to the destination PsiMethod
     *
     * @param source the PsiMethod to copy from.
     * @param destination the PsiMethod to copy to.
     */
    public static void copyPsiDocComment( PsiMethod source, PsiMethod destination )
    {
        if( source != null && source.getDocComment() != null && destination != null )
        {
            PsiDocComment psiDocComment = source.getDocComment();
            if( null != psiDocComment )
            {
                setPsiDocComment( (PsiDocComment) psiDocComment.copy(), destination );
            }
        }
    }

    /**
     * Method setPsiDocComment sets the PsiDocComment of the destination PsiMethod to the
     * specified psiDocComment parameter.
     *
     * @param psiDocComment the PsiDocComment that serves as the source.
     * @param destination the psi element which is capable of having a doc comment declaration.
     */
    public static void setPsiDocComment(PsiDocComment psiDocComment, PsiDocCommentOwner destination)
    {
        if (psiDocComment != null) {
            try {
                PsiElement anchor = destination.getDocComment();
                if (anchor != null) {
                    anchor.replace(psiDocComment);  // Replace THIS PsiElement with the new element
                } else {
                    PsiElement parent = destination.getParent();
                    if (parent != null) {
                        parent.addBefore(psiDocComment, destination);
                    }
                }
            } catch (IncorrectOperationException e) {
                log.error("Failed to copy PsiDocComment " + e.getMessage());
            }
        }
    }

    /**
     * Method createDocCommentWithReference creates a PsiDocComment object containing
     * JavaDoc that references the method identified by the psiSuperMethod parameter.
     * Based on the parameter preserveOriginal, this method will either replace the
     * entire JavaDoc of the psiMethod parameter, or insert a JavaDoc reference in the
     * existing JavaDoc declaration of this method.
     *
     * @param psiMethod the method that will get the new JavaDoc declaration
     * @param psiSuperMethod the super method that will be used to determine the reference
     * @param preserveOriginal flag true to preserve the original JavaDoc and only add the
     * reference, false will replace the entire JavaDoc.
     * @param useSingleLineJavaDocReference indicates whether the plugin is configured to use single
     * line references or three lines
     * @return PsiDocComment the PsiElement defining the new JavaDoc or null if either methods was null.
     */
    public static PsiDocComment createDocCommentWithReference( final PsiMethod psiMethod,
                                                               final PsiMethod psiSuperMethod,
                                                               final boolean preserveOriginal,
                                                               final boolean useSingleLineJavaDocReference )
    {
        PsiDocComment refComment = null;

        if( null != psiMethod && null != psiSuperMethod )
        {
            PsiDocComment psiDocComment = psiMethod.getDocComment();

            PsiElementFactory elementFactory =
                    JavaPsiFacade.getInstance( psiMethod.getProject() ).getElementFactory();

            StringBuffer newComment = new StringBuffer();
            try
            {
                if( preserveOriginal && null != psiDocComment )
                {
                    String oldCommentText = psiDocComment.getText();
                    int commentEnd = oldCommentText.lastIndexOf( "*/" );
                    if( commentEnd > 0 )
                    {
                        oldCommentText = oldCommentText.substring( 0, commentEnd );
                        String commentIndent = null;
                        int breakLine = oldCommentText.lastIndexOf( "\n", commentEnd );
                        if( breakLine > 0 )
                        {
                            commentIndent = oldCommentText.substring( breakLine, commentEnd );
                            // If the break line was on the same line as a part of the JavaDoc text,
                            // than we cannot use this substring for indentation.
                            if( commentIndent.indexOf( "@" ) > 0 )
                            {
                                commentIndent = "\n     ";
                                oldCommentText += commentIndent;
                            }
                        }

                        // Create new reference javaDoc
                        newComment.append( oldCommentText )
                                .append( "*" )
                                .append( commentIndent != null && !useSingleLineJavaDocReference ? commentIndent : "" )
                                .append( !useSingleLineJavaDocReference ? "* " : " " )
                                .append( PsiUtils.createReferenceComment( psiSuperMethod ) )
                                .append( commentIndent != null ? commentIndent : "" )
                                .append( "*/\n" );
                        psiDocComment.delete();
                        refComment = elementFactory.createDocCommentFromText( newComment.toString() );
                    }
                }
                else
                {
                    // Create new reference javaDoc
                    if( useSingleLineJavaDocReference )
                    {
                        newComment.append( "/** " )
                                .append( PsiUtils.createReferenceComment( psiSuperMethod ) )
                                .append( " */" );
                    }
                    else
                    {
                        newComment.append( "/**\n" )
                                .append( " * " )
                                .append( PsiUtils.createReferenceComment( psiSuperMethod ) )
                                .append( "\n" )
                                .append( " */" );
                    }
                    refComment = elementFactory.createDocCommentFromText( newComment.toString() );
                }
            }
            catch( IncorrectOperationException e )
            {
                log.error( "Failed to create PsiDocComment object", e.getMessage() );
            }
        }
        return refComment;
    }

    /**
     * Method addReference defines the actual fix for adding a JavaDoc reference
     * to a method that wants to preserve its existing JavaDoc.
     *
     * @param psiMethod the method of which the JavaDoc needs to contain a reference
     * @param superMethod the super method on which the reference is based.
     * @param preserveComment flag to indicate whether the reference should keep the existing JavaDoc and
     * add a reference (true) or replace the entire JavaDoc with only the reference (false)
     * @param useSingleLineJavaDocReference indicates whether the plugin is configured to use single
     * line references or three lines
     */
    public static void addReference( PsiMethod psiMethod, PsiMethod superMethod,
                                     boolean preserveComment, boolean useSingleLineJavaDocReference )
    {
        if( null != psiMethod && null != superMethod )
        {
            PsiDocComment docComment = createDocCommentWithReference( psiMethod, superMethod,
                                                                      preserveComment, useSingleLineJavaDocReference );
            PsiUtils.setPsiDocComment( docComment, psiMethod );
        }
    }

    /**
     * Method to find the specified psiMethod in the parent class/interface
     * @param psiMethod the method of which the super implementation/definition is requested
     * @return the superMethod or null if the direct super class/interface (if available) does not
     * define the specified method or is the containing class of the method does not extend or
     * implement another class/interface.
     */
    public static PsiMethod getSuperMethod( PsiMethod psiMethod )
    {
        if( null != psiMethod )
        {
            PsiClass containingClass = psiMethod.getContainingClass();
            if( null != containingClass )
            {
                return checkParents( containingClass.getSuperClass(), psiMethod );
            }
        }
        return null;
    }

    /**
     * Method to search the specified method in super classes or implemented interfaces
     *
     * @param superClass the superClass to check for the occurrence of the specified method if not null.
     * @param psiMethod the psi method to find in the super class or implemented interface
     * @return PsiMethod the super method or null if not found
     */
    public static PsiMethod checkParents( PsiClass superClass, PsiMethod psiMethod )
    {
        PsiMethod superMethod = null;

        if( null != superClass )
        {
            superMethod = checkClassHierarchy( psiMethod, superClass );
        }

        if( null == superMethod )
        {
            PsiClass containingClass = psiMethod.getContainingClass();

            PsiReferenceList referenceList;
            if( containingClass.isInterface() )
            {
                referenceList = containingClass.getExtendsList();
            }
            else
            {
                referenceList = containingClass.getImplementsList();
            }
            if( null != referenceList )
            {
                PsiClassType[] interfaceTypes = referenceList.getReferencedTypes();
                for( int i = 0; superMethod == null && i < interfaceTypes.length; i++ )
                {
                    PsiClassType interfaceType = interfaceTypes[i];
                    PsiClass anInterface = interfaceType.resolve();
                    if( null != anInterface )
                    {
                        superMethod = checkInterfaceHierarchy( psiMethod, anInterface );
                    }
                }
            }
        }
        return superMethod;
    }

    /**
     * Method to find the specified method in the implemented interface hierarchy of the specified
     * class.
     * @param psiMethod the method to find
     * @param containingClass the interface possibly defining the method
     * @return the method definition or null if not found.
     */
    private static PsiMethod checkInterfaceHierarchy( PsiMethod psiMethod, PsiClass containingClass )
    {
        PsiMethod superMethod = null;
        if( null != containingClass )
        {
            // First check if the containing class defines the method
            superMethod = containingClass.findMethodBySignature( psiMethod, false );

            // If not, go up in the inhertance tree
            if( superMethod == null )
            {
                PsiReferenceList superExtendsList = containingClass.getExtendsList();
                if( superExtendsList != null && superExtendsList.getReferencedTypes().length > 0 )
                {
                    for( int j = 0; superMethod == null && j < superExtendsList.getReferencedTypes().length; j++ )
                    {
                        PsiClassType psiClassType = superExtendsList.getReferencedTypes()[j];
                        PsiClass psiClass = psiClassType.resolve();
                        superMethod = checkInterfaceHierarchy( psiMethod, psiClass );
                    }
                }
            }
        }
        return superMethod;
    }

    /**
     * Method to find the specified method in the extended class hierarchy of the specified
     * class.
     * @param psiMethod the method to find
     * @param superClass the class possibly defining the method
     * @return the method definition or null if not found.
     */
    private static PsiMethod checkClassHierarchy( PsiMethod psiMethod, PsiClass superClass )
    {
        PsiMethod superMethod = null;
        if( !psiMethod.isConstructor() )
        {
//            superMethod = superClass.findMethodBySignature(psiMethod, true);  // We cannot trust this method!
            PsiMethod[] psiMethods = superClass.getAllMethods();
            for( int i = 0; superMethod == null && i < psiMethods.length; i++ )
            {
                if( psiMethod.getName().equals( psiMethods[i].getName() ) &&
                    methodsMatchInParameterTypes( psiMethod, psiMethods[i] ) )
                {
                    superMethod = psiMethods[i];
                }
            }
        }
        else
        {
            PsiMethod[] superConstructors = superClass.getConstructors();
            for( int i = 0; superMethod == null && i < superConstructors.length; i++ )
            {
                PsiMethod superConstructor = superConstructors[i];
                if( psiMethod.isConstructor() && superConstructor.isConstructor() &&
                    methodsMatchInParameterTypes( psiMethod, superConstructor ) )
                {
                    superMethod = superConstructor;
                }
            }
        }

        if( null == superMethod && null != superClass.getSuperClass() )
        {
            superMethod = checkParents( superClass.getSuperClass(), psiMethod );
        }
        return superMethod;
    }


    /**
     * Method to determine whether two methods, based soley on their parameter
     * types are identical.
     *
     * @param methodA psiMethod A
     * @param methodB psiMethod B
     * @return true if the two methods define exactly the same parameter types
     * and in the same order, false otherwise. Note that this methodA does not take the methodA names
     * into account and should therefor be only used for constructors.
     */
    private static boolean methodsMatchInParameterTypes( PsiMethod methodA, PsiMethod methodB )
    {
        PsiParameterList parameterListA = methodA.getParameterList();
        PsiParameterList parameterListB = methodB.getParameterList();

        PsiParameter[] parametersOfA = parameterListA.getParameters();
        PsiParameter[] parametersOfB = parameterListB.getParameters();
        if( parametersOfA.length == parametersOfB.length )
        {
            for( int i = 0; i < parametersOfA.length; i++ )
            {
                if( !parametersOfA[i].getType().equals( parametersOfB[i].getType() ) )
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Method isAnonymous determines whether the passed element is either
     * an anonymous class or part of it.
     *
     * @param element class or method
     * @return boolean true if anonymous or inside anonymous, false otherwise.
     */
    public static boolean isAnonymous( PsiElement element )
    {
        return element != null &&
               (element instanceof PsiAnonymousClass ||
                (element instanceof PsiMethod &&
                 ((PsiMethod) element).getContainingClass() instanceof PsiAnonymousClass));
    }

    /**
     * Method isStatic determines whether the specified PsiMethod is a static method.
     *
     * @param method of type PsiMethod
     * @return boolean true if the method is static, false otherwise.
     */
    public static boolean isStatic( PsiMethod method )
    {
        return method.getModifierList().hasExplicitModifier( PSI_MODIFIER_STATIC );
    }

    /**
     * Method isInnerClass determines whether the specified element is an inner class or a part of an innner class.
     *
     * @param psiElement of type PsiClass
     * @return boolean thrue if the specified class is an inner class, false otherwise.
     */
    public static boolean isInnerClass( PsiElement psiElement )
    {
        return psiElement instanceof PsiClass && ((PsiClass) psiElement).getContainingClass() != null
               || psiElement instanceof PsiField
                  && ((PsiField) psiElement).getContainingClass() != null
                  && ((PsiField) psiElement).getContainingClass().getContainingClass() != null
               || psiElement instanceof PsiMethod
                  && ((PsiMethod) psiElement).getContainingClass() != null
                  && ((PsiMethod) psiElement).getContainingClass().getContainingClass() != null;
    }

    /**
     * Method accessModifierLevelInRange compares the selected access modifier level with the one declared
     * by the specified method. If the selected level is higher than the modifier of the method, false is returned/
     *
     * @param modifierList of type PsiModifierList
     * @param methodDetectionLevel the configured level
     * @return boolean true if the modifier is within the selected range, false otherwise.
     */
    public static boolean accessModifierLevelInRange( PsiModifierList modifierList, String methodDetectionLevel )
    {
        String declaredModifier = modifierList.getText();
        if( null == declaredModifier || "".equals( declaredModifier ) )
        {
            declaredModifier = "default";
        }

        boolean found = false;
        int declaredLevel = 0;
        for( ; !found && declaredLevel < JavaDocSyncMethodOptionsPanel.detectionLevels.length; declaredLevel++ )
        {
            String detectionLevel = JavaDocSyncMethodOptionsPanel.detectionLevels[declaredLevel];
            if( detectionLevel.equals( declaredModifier ) )
            {
                found = true;
            }
        }

        found = false;
        int selectedLevel = 0;
        for( ; !found && selectedLevel < JavaDocSyncMethodOptionsPanel.detectionLevels.length; selectedLevel++ )
        {
            String detectionLevel = JavaDocSyncMethodOptionsPanel.detectionLevels[selectedLevel];
            if( detectionLevel.equals( methodDetectionLevel ) )
            {
                found = true;
            }
        }

        return declaredLevel <= selectedLevel;
    }

    /**
     * Method getFieldDocToken finds the class level doc comment for the specified property and retuns it.
     *
     * @param method of type PsiMethod
     * @param propertyName of type String
     * @return String
     */
    public static String getFieldDocToken( PsiMethod method, String propertyName )
    {
        PsiClass containingClass = method.getContainingClass();
        if( containingClass == null )
        {
            return null;
        }
        PsiField[] allFields = containingClass.getAllFields();
        for( PsiField field : allFields )
        {
            if( propertyName.equals( field.getName() ) )
            {
                PsiDocComment comment = field.getDocComment();
                if( comment != null )
                {
                    PsiElement[] elements = comment.getDescriptionElements();
                    for( PsiElement element : elements )
                    {
                        if( element instanceof PsiDocToken )
                        {
                            return element.getText();
                        }
                    }
                }
            }
        }
        return "";
    }

    /**
     * Constructor PsiUtils cannot be called.
     */
    private PsiUtils()
    {
    }
}
