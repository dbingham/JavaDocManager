import com.intellij.codeInspection.InspectionManager;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.testFramework.InspectionTestCase;
import com.intellij.util.IncorrectOperationException;
import com.perniciouspenguins.ideaz.javadoc.JavaDocManager;
import com.perniciouspenguins.ideaz.javadoc.fixes.AddReference;
import com.perniciouspenguins.ideaz.javadoc.fixes.CopyFromParent;
import com.perniciouspenguins.ideaz.javadoc.fixes.GenerateFromClass;
import com.perniciouspenguins.ideaz.javadoc.fixes.GenerateFromField;
import com.perniciouspenguins.ideaz.javadoc.fixes.GenerateFromSignature;
import com.perniciouspenguins.ideaz.javadoc.fixes.IntroduceReference;
import com.perniciouspenguins.ideaz.javadoc.fixes.LocalQuickFixBase;
import com.perniciouspenguins.ideaz.javadoc.fixes.MoveToParentAndIntroduceReference;
import com.perniciouspenguins.ideaz.javadoc.fixes.ReplaceParentDoc;
import com.perniciouspenguins.ideaz.javadoc.inspections.InconsistentJavaDocInspection;
import com.perniciouspenguins.ideaz.javadoc.inspections.JavaDocBaseInspection;
import com.perniciouspenguins.ideaz.javadoc.inspections.MissingClassJavaDocInspection;
import com.perniciouspenguins.ideaz.javadoc.inspections.MissingFieldJavaDocInspection;
import com.perniciouspenguins.ideaz.javadoc.inspections.MissingMethodJavaDocInspection;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The Test class.
 *
 * To successfully execute this class, the run configuration requires the following
 * VM parameters to be set:
 *
 * -Didea.plugins.load=false -Xbootclasspath/p:$IDEA_HOME/lib/boot.jar
 *
 * @author Raymond P. Brandon
 * @version 1.0
 */
public class JavaDocBaseInspectionTests extends InspectionTestCase
{
  /** Field classImplementingInterfaceClass:  **/
  private PsiClass classImplementingInterfaceClass;
  /** Field someInterfaceClass:  **/
  private PsiClass someInterfaceClass;
  /** Field extendingInterfaceClass:  **/
  private PsiClass extendingInterfaceClass;
  /** Field classObjectClass:  **/
  private PsiClass classObjectClass;
  /** Field classExtendingObjectClass:  **/
  private PsiClass classExtendingObjectClass;
  /** Field classExtendingCustomClassClass:  **/
  private PsiClass classExtendingCustomClassClass;
  /** Field classDefiningEnumClass: **/
  private PsiClass classDefiningEnumClass;

  /**
   * Method setupDummyPsiClasses
   *
   */
  private void setupDummyPsiClasses()
  {
    PsiJavaFile someInterface = (PsiJavaFile) PsiFileFactory.getInstance( getProject() ).createFileFromText(
        "SomeInterface.java",
        "package p1.p2;\n" +
        "\n" +
        "/**\n" +
        " * The SomeInterface class.\n" +
        " *\n" +
        " * @author Raymond P. Brandon\n" +
        " * @version 1.0\n" +
        " */\n" +
        "public interface SomeInterface {\n" +
        "    /**\n" +
        "     * Some JavaDoc here\n" +
        "     * @param one One\n" +
        "     * @param two Two\n" +
        "     * @return String\n" +
        "     */\n" +
        "    // Should say nothing\n" +
        "    public String anAbstractMethodWithJavaDoc(int one, long two);\n" +
        "\n" +
        "    /**\n" +
        "     * Method anAbstractMethodWithoutJavaDoc\n" +
        "     */\n" +
        "    // Should say nothing\n" +
        "    public void anAbstractMethodWithoutJavaDoc();\n" +
        "\n" +
        "    /**\n" +
        "     * Method anAbstractMethodWithoutJavaDoc\n" +
        "     */\n" +
        "    // Should say nothing\n" +
        "    public void overrideMe();\n" +
        "}" );

    PsiJavaFile extendingSomeInterface = (PsiJavaFile) PsiFileFactory.getInstance( getProject() ).createFileFromText(
        "ExtendingSomeInterface.java",
        "package p1.p2;\n" +
        "\n" +
        "import java.util.List;\n" +
        "\n" +
        "/**\n" +
        " * The ExtendingSomeInterface class.\n" +
        " *\n" +
        " * @author Raymond P. Brandon\n" +
        " * @version 1.0\n" +
        " */\n" +
        "public interface ExtendingSomeInterface extends SomeInterface {\n" +
        "    // Should say: Interface method does not have JavaDoc\n" +
        "    public int aMethodWithoutJavaDoc(String s);\n" +
        "\n" +
        "    /**\n" +
        "     * The JavaDoc\n" +
        "     *\n" +
        "     * @param system the system\n" +
        "     * @return byte array\n" +
        "     */\n" +
        "    // Should say nothing\n" +
        "    public byte[] aMethodWithJavaDoc(System system);\n" +
        "\n" +
        "    /**\n" +
        "     * @param objects objects\n" +
        "     * @return bills\n" +
        "     */\n" +
        "    // Should say nothing\n" +
        "    public List aDifferentMethodWithJavaDoc(List objects);\n" +
        "\n" +
        "    // Should say: overridden method does not have any JavaDoc\n" +
        "    public void overrideMe();\n" +
        "}" );

    PsiJavaFile classImplementingInterface =
        (PsiJavaFile) PsiFileFactory.getInstance( getProject() ).createFileFromText(
            "ClassImplementingInterface.java",
            "package p1.p2;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class ClassImplementingInterface implements ExtendingSomeInterface {\n" +
            "    private boolean myMockField;\n" +
            "    // Should say: method implementation does not have JavaDoc\n" +
            "    public int aMethodWithoutJavaDoc(String s) {\n" +
            "        // Inspection should mention mising JavaDoc\n" +
            "        // Class without JavaDoc implements interface definition\n" +
            "        return 0;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * The JavaDoc\n" +
            "     *\n" +
            "     * @param system the system\n" +
            "     * @return byte array\n" +
            "     */\n" +
            "    // Should say nothing\n" +
            "    public byte[] aMethodWithJavaDoc(System system) {\n" +
            "        return new byte[0];\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * Some different JavaDoc here\n" +
            "     *\n" +
            "     * @param one One\n" +
            "     * @param two Two\n" +
            "     * @return String\n" +
            "     *\n" +
            "     * @see p1.p2.SomeInterface#anAbstractMethodWithJavaDoc(int,long)\n" +
            "     */\n" +
            "    // Should say: JavaDoc differs from JavaDoc in parent method\n" +
            "    public String anAbstractMethodWithJavaDoc(int one, long two) {\n" +
            "        // Inspection should mention javadoc differs from parent\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * @see SomeInterface#anAbstractMethodWithoutJavaDoc()\n" +
            "     */\n" +
            "    // Should say nothing\n" +
            "    public void anAbstractMethodWithoutJavaDoc() {\n" +
            "        Object o = new Object() {\n" +
            "            public String toString() {\n" +
            "                return super.toString();\n" +
            "            }\n" +
            "        };\n" +
            "    }\n" +
            "\n" +
            "    // Should say: method implementation does not have Javadoc\n" +
            "    public List aDifferentMethodWithJavaDoc(List objects) {\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * Method anAbstractMethodWithoutJavaDoc\n" +
            "     */\n" +
            "    // Should say nothing\n" +
            "    public void overrideMe() {\n" +
            "    }\n" +
            "}" );

    PsiJavaFile classObject = (PsiJavaFile) PsiFileFactory.getInstance( getProject() ).createFileFromText(
        "Object.java",
        "package java.lang;\n" +
        "\n" +
        "public class Object {\n" +
        "    \n" +
        "    public String toString() {\n" +
        "        return \"\";\n" +
        "    }\n" +
        "\n" +
        "    public int hashCode() {\n" +
        "       return 0;\n" +
        "    }\n" +
        "}" );

    PsiJavaFile classExtendingObject = (PsiJavaFile) PsiFileFactory.getInstance( getProject() ).createFileFromText(
        "ClassExtendingObject.java",
        "package p1.p2;\n" +
        "\n" +
        "public class ClassExtendingObject {\n" +
        "\n" +
        "    // Should say: method implementation does not have JavaDoc\n" +
        "    public ClassExtendingObject() {\n" +
        "    }\n" +
        "\n" +
        "    /**\n" +
        "     * Some JavaDoc here\n" +
        "     * @param x the parameter\n" +
        "     */\n" +
        "    // Should say nothing\n" +
        "    public ClassExtendingObject(String x) {\n" +
        "        System.out.println(x);\n" +
        "    }\n" +
        "\n" +
        "    // Should say: method in base class does not have JavaDoc\n" +
        "    public void methodWithoutJavaDoc() {\n" +
        "    }\n" +
        "\n" +
        "    /**\n" +
        "     * JavaDoc for this method\n" +
        "     */\n" +
        "    // Should say nothing\n" +
        "    public void methodWithJavaDoc() {\n" +
        "    }\n" +
        "\n" +
        "    /**\n" +
        "     * Method overridden from Object class\n" +
        "     * @return the string\n" +
        "     */\n" +
        "    // Should say nothing\n" +
        "    public String toString() {\n" +
        "        return super.toString();\n" +
        "    }\n" +
        "}" );

    PsiJavaFile classExtendingCustomClass = (PsiJavaFile) PsiFileFactory.getInstance( getProject() ).createFileFromText(
        "ClassExtendingCustomClass.java",
        "package p1.p2;\n" +
        "\n" +
        "public class ClassExtendingCustomClass extends ClassExtendingObject {\n" +
        "\n" +
        "    /**\n" +
        "     * JavaDoc here\n" +
        "     */\n" +
        "    // Should say nothing because constructor doc does not have to match\n" +
        "    public ClassExtendingCustomClass() {\n" +
        "    }\n" +
        "\n" +
        "    /**\n" +
        "     * Some JavaDoc here\n" +
        "     * @param xy the parameter\n" +
        "     */\n" +
        "    // Should say nothing because constructor doc does not have to match\n" +
        "    public ClassExtendingCustomClass(String xy) {\n" +
        "        super(xy);\n" +
        "    }\n" +
        "\n" +
        "    // Should say: overridden method does not have JavaDoc\n" +
        "    public void methodWithoutJavaDoc() {\n" +
        "        super.methodWithoutJavaDoc();\n" +
        "    }\n" +
        "\n" +
        "    /**\n" +
        "     * Some JavaDoc here\n" +
        "     * @return v\n" +
        "     */\n" +
        "    // Should say nothing\n" +
        "    public int hashCode() {\n" +
        "        return super.hashCode();\n" +
        "    }\n" +
        "\n" +
        "    /**\n" +
        "     * Different JavaDoc here\n" +
        "     *\n" +
        "     * @return the string\n" +
        "     */\n" +
        "    // Should say: JavaDoc differs from JavaDoc in parent method\n" +
        "    public String toString() {\n" +
        "        return super.toString();\n" +
        "    }\n" +
        "}" );

    PsiJavaFile classDefiningEnum = (PsiJavaFile) PsiFileFactory.getInstance( getProject() ).createFileFromText(
        "Test.java",
        "package p1.p2;\n" +
        "\n" +
        "public enum Test {\n" +
        "}" );

    someInterfaceClass = someInterface.getClasses()[0];
    extendingInterfaceClass = extendingSomeInterface.getClasses()[0];
    classImplementingInterfaceClass = classImplementingInterface.getClasses()[0];
    classObjectClass = classObject.getClasses()[0];
    classExtendingObjectClass = classExtendingObject.getClasses()[0];
    classExtendingCustomClassClass = classExtendingCustomClass.getClasses()[0];
    classDefiningEnumClass = classDefiningEnum.getClasses()[0];

    PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance( getProject() ).getElementFactory();
    try
    {
      // Make interface ExtendingSomeInterface extend SomeInterface
      PsiJavaCodeReferenceElement referenceElement1 =
          psiElementFactory.createClassReferenceElement( someInterfaceClass );
      PsiReferenceList psiReference1 = psiElementFactory.createReferenceList(
          new PsiJavaCodeReferenceElement[] {referenceElement1} );
      assertTrue( extendingInterfaceClass.isInterface() );
      PsiReferenceList extendsList1 = extendingInterfaceClass.getExtendsList();
      assertNotNull( extendsList1 );
      extendsList1.add( psiReference1 );

      // Make ClassImplementingInterface implement ExtendingSomeInterface
      PsiJavaCodeReferenceElement referenceElement2 =
          psiElementFactory.createClassReferenceElement( extendingInterfaceClass );
      PsiReferenceList psiReference2 = psiElementFactory.createReferenceList(
          new PsiJavaCodeReferenceElement[] {referenceElement2} );
      assertFalse( classImplementingInterfaceClass.isInterface() );
      PsiReferenceList implementsList = classImplementingInterfaceClass.getImplementsList();
      assertNotNull( implementsList );
      implementsList.add( psiReference2 );

      // Make ClassExtendingCustomClass extend ClassExtendingObject
      PsiJavaCodeReferenceElement referenceElement3 =
          psiElementFactory.createClassReferenceElement( classExtendingObjectClass );
      PsiReferenceList psiReference3 = psiElementFactory.createReferenceList(
          new PsiJavaCodeReferenceElement[] {referenceElement3} );
      PsiReferenceList extendsList2 = classExtendingCustomClassClass.getExtendsList();
      assertNotNull( extendsList2 );
      extendsList2.add( psiReference3 );
    }
    catch( IncorrectOperationException e )
    {
      fail( e.getMessage() );
    }
  }

  /**
   * Method setUp
   *
   * @throws Exception when
   */
  protected void setUp() throws Exception
  {
    super.setUp();

    LanguageLevelProjectExtension.getInstance( getProject() ).setLanguageLevel( LanguageLevel.JDK_1_5 );
//        getPsiManager().setEffectiveLanguageLevel(LanguageLevel.JDK_1_5);
    setupDummyPsiClasses();
  }

  /**
   * Method testHasReferenceToParentMethod
   *
   */
  public void testHasReferenceToParentMethod()
  {
    PsiMethod psiMethod = findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithoutJavaDoc" );
    assertNotNull( psiMethod );

    PsiMethod superMethod = findPsiMethod( someInterfaceClass, "anAbstractMethodWithoutJavaDoc" );
    assertNotNull( superMethod );

    Boolean value = null;
    try
    {
      value = PsiUtils.hasReferenceToParentMethod( psiMethod, superMethod );
    }
    catch( Exception e )
    {
      fail( e.getMessage() );
    }
    assertTrue( value );

    // Test with package differences
    psiMethod = findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithJavaDoc" );
    assertNotNull( psiMethod );

    superMethod = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    assertNotNull( superMethod );

    value = null;
    try
    {
      value = PsiUtils.hasReferenceToParentMethod( psiMethod, superMethod );
    }
    catch( Exception e )
    {
      fail( e.getMessage() );
    }
    assertTrue( value );
  }

  /**
   * Method testGenerateFromSignature
   *
   */
  public void testGenerateFromSignature()
  {
    PsiMethod psiMethod = findPsiMethod( someInterfaceClass, "anAbstractMethodWithoutJavaDoc" );
    assertNotNull( psiMethod );

    LocalQuickFixBase localQuickFixBase = new GenerateFromSignature( psiMethod );
    assertEquals( "Generate JavaDoc based on method signature", localQuickFixBase.getName() );
    assertEquals( "JavaDoc inspection", localQuickFixBase.getFamilyName() );
    localQuickFixBase.doFix();

    PsiDocComment docComment = psiMethod.getDocComment();
    assertNotNull( docComment );
    String javaDoc = docComment.getText();
    assertEquals( -1, javaDoc.indexOf( "@return" ) );
    assertTrue( javaDoc.indexOf( "Method" ) > 0 );
  }

  /**
   * Method testGenerateFromSignature
   *
   */
  public void testGenerateFromEnum()
  {
    LocalQuickFixBase localQuickFixBase = new GenerateFromClass( classDefiningEnumClass );
    assertEquals( "Generate JavaDoc based on enum", localQuickFixBase.getName() );
    assertEquals( "JavaDoc inspection", localQuickFixBase.getFamilyName() );
    localQuickFixBase.doFix();

    PsiDocComment docComment = classDefiningEnumClass.getDocComment();
    assertNotNull( docComment );
    String javaDoc = docComment.getText();
    assertTrue( javaDoc.indexOf( "Enum" ) > 0 );
    assertTrue( javaDoc.indexOf( "Created on" ) > 0 );
  }

  /**
   * Method testFindSuperConstructor
   *
   */
  public void testFindSuperConstructor()
  {
    PsiMethod[] constructors = classExtendingCustomClassClass.getConstructors();
    PsiMethod overriddenConstructor = null;
    for( PsiMethod constructor : constructors )
    {
      if( constructor.getParameterList().getParametersCount() == 1 )
      {
        overriddenConstructor = constructor;
      }
    }
    assertNotNull( overriddenConstructor );

    JavaDocBaseInspection inspection = new InconsistentJavaDocInspection();
    assertEquals( "InconsistentJavaDoc", inspection.getShortName() );
    assertEquals( "Inconsistent JavaDoc", inspection.getDisplayName() );
    Method checkParents;
    try
    {
      checkParents = PsiUtils.class
          .getDeclaredMethod( "checkParents", PsiClass.class, PsiMethod.class );
      checkParents.setAccessible( true );

      PsiMethod superConstructor = (PsiMethod) checkParents.invoke( inspection,
                                                                    classExtendingObjectClass, overriddenConstructor );
      PsiDocComment docComment = superConstructor.getDocComment();
      assertNotNull( docComment );
      assertFalse( docComment.getText().indexOf( "@param x" ) == -1 );
    }
    catch( Exception e )
    {
      fail( e.getMessage() );
    }
  }

  /**
   * Method testImplementingMethodShouldNotSaySayOverride
   *
   */
  public void testImplementingMethodShouldNotSayOverride()
  {
    final PsiMethod interfaceMethod = findPsiMethod( extendingInterfaceClass, "aDifferentMethodWithJavaDoc" );
    PsiMethod methodImplementation = findPsiMethod( classImplementingInterfaceClass, "aDifferentMethodWithJavaDoc" );

    assertNotNull( interfaceMethod );
    assertNotNull( methodImplementation );

    // Workaround for non-working inheritance tree with UnitTests that use PSI
    MissingMethodJavaDocInspection mmji = new MissingMethodJavaDocInspection()
    {
      /** @inheritDoc */
      protected PsiMethod getSuperMethod( PsiMethod psiMethod )
      {
        return interfaceMethod;
      }
    };

    Method determineIntroduceDocOptions = findMethod( MissingMethodJavaDocInspection.class,
                                                      "determineIntroduceDocOptions", PsiMethod.class, List.class );
    assertNotNull( determineIntroduceDocOptions );
    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();
    String description = null;
    try
    {
      description = (String) determineIntroduceDocOptions.invoke( mmji, methodImplementation, fixes );
    }
    catch( Exception e )
    {
      fail( e.getMessage() );
    }

    assertEquals( LocalQuickFixBase.IMPLEMENTATION_NO_JAVADOC, description );
  }

  /**
   * Method testAddThreeLineReference
   *
   */
  public void testAddThreeLineReference()
  {
    PsiMethod interfaceMethod = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    assertNotNull( interfaceMethod );
    PsiMethod psiMethod = findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithJavaDoc" );
    assertNotNull( psiMethod );

    assertNotNull( interfaceMethod );
    assertNotNull( psiMethod );

    PsiDocComment childDocComment = psiMethod.getDocComment();
    assertNotNull( childDocComment );

    String originalMethodComment = childDocComment.getText();

    LocalQuickFixBase localQuickFixBase = new AddReference( psiMethod, interfaceMethod, false );
    assertEquals( "Add JavaDoc reference to parent method", localQuickFixBase.getName() );

    int lineCount = 1;
    int newLine = 0;
    for( int i = 0; newLine != -1 && i < originalMethodComment.length(); )
    {
      newLine = originalMethodComment.indexOf( "\n", i );
      if( newLine != -1 )
      {
        lineCount++;
        i = newLine + 1;
      }
    }
    assertEquals( 9, lineCount );
    System.out.println( originalMethodComment );

    localQuickFixBase.doFix();
    PsiDocComment psiDocComment = psiMethod.getDocComment();
    assertNotNull( psiDocComment );
    String text = psiDocComment.getText();
    assertTrue( text.indexOf( "@see" ) > 0 );

    lineCount = 1;
    newLine = 0;
    for( int i = 0; newLine != -1 && i < text.length(); )
    {
      newLine = text.indexOf( "\n", i );
      if( newLine != -1 )
      {
        lineCount++;
        i = newLine + 1;
      }
    }
    assertEquals( 11, lineCount );

    System.out.println( text );
  }

  /**
   * Method testAddSingleLineReference
   *
   */
  public void testAddSingleLineReference()
  {
    PsiMethod interfaceMethod = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    assertNotNull( interfaceMethod );
    PsiMethod psiMethod = findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithJavaDoc" );
    assertNotNull( psiMethod );

    assertNotNull( interfaceMethod );
    assertNotNull( psiMethod );

    PsiDocComment childDocComment = psiMethod.getDocComment();
    assertNotNull( childDocComment );

    String originalMethodComment = childDocComment.getText();
    int lineCount = 1;
    int newLine = 0;
    for( int i = 0; newLine != -1 && i < originalMethodComment.length(); )
    {
      newLine = originalMethodComment.indexOf( "\n", i );
      if( newLine != -1 )
      {
        lineCount++;
        i = newLine + 1;
      }
    }
    assertEquals( 9, lineCount );

    LocalQuickFixBase localQuickFixBase = new AddReference( psiMethod, interfaceMethod, true );
    assertEquals( "Add JavaDoc reference to parent method", localQuickFixBase.getName() );

    System.out.println( originalMethodComment );

    localQuickFixBase.doFix();
    PsiDocComment psiDocComment = psiMethod.getDocComment();
    assertNotNull( psiDocComment );
    String text = psiDocComment.getText();
    assertTrue( text.indexOf( "@see" ) > 0 );

    lineCount = 1;
    newLine = 0;
    for( int i = 0; newLine != -1 && i < text.length(); )
    {
      newLine = text.indexOf( "\n", i );
      if( newLine != -1 )
      {
        lineCount++;
        i = newLine + 1;
      }
    }
    assertEquals( 10, lineCount );

    System.out.println( text );
  }

  /**
   * Method testIntroduceThreeLineReference
   *
   */
  public void testIntroduceThreeLineReference()
  {
    PsiMethod interfaceMethod = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    assertNotNull( interfaceMethod );
    PsiMethod psiMethod = findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithJavaDoc" );
    assertNotNull( psiMethod );

    assertNotNull( interfaceMethod );
    assertNotNull( psiMethod );

    PsiDocComment childDocComment = psiMethod.getDocComment();
    assertNotNull( childDocComment );

    String originalMethodComment = childDocComment.getText();
    int lineCount = 1;
    int newLine = 0;
    for( int i = 0; newLine != -1 && i < originalMethodComment.length(); )
    {
      newLine = originalMethodComment.indexOf( "\n", i );
      if( newLine != -1 )
      {
        lineCount++;
        i = newLine + 1;
      }
    }
    assertEquals( 9, lineCount );

    LocalQuickFixBase localQuickFixBase = new IntroduceReference( psiMethod, interfaceMethod, false );
    assertEquals( "Introduce JavaDoc reference to parent method", localQuickFixBase.getName() );
    System.out.println( originalMethodComment );

    localQuickFixBase.doFix();
    PsiDocComment psiDocComment = psiMethod.getDocComment();
    assertNotNull( psiDocComment );
    String text = psiDocComment.getText();
    assertTrue( text.indexOf( "@see" ) > 0 );

    assertTrue( text.indexOf( "@return" ) == -1 );
    assertTrue( text.indexOf( "@param" ) == -1 );

    lineCount = 1;
    newLine = 0;
    for( int i = 0; newLine != -1 && i < text.length(); )
    {
      newLine = text.indexOf( "\n", i );
      if( newLine != -1 )
      {
        lineCount++;
        i = newLine + 1;
      }
    }
    assertEquals( 3, lineCount );
    System.out.println( text );
  }

  /**
   * Method testIntroduceSingleLineReference
   *
   */
  public void testIntroduceSingleLineReference()
  {
    PsiMethod interfaceMethod = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    assertNotNull( interfaceMethod );
    PsiMethod psiMethod = findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithJavaDoc" );
    assertNotNull( psiMethod );

    assertNotNull( interfaceMethod );
    assertNotNull( psiMethod );

    PsiDocComment childDocComment = psiMethod.getDocComment();
    assertNotNull( childDocComment );

    String originalMethodComment = childDocComment.getText();
    int lineCount = 1;
    int newLine = 0;
    for( int i = 0; newLine != -1 && i < originalMethodComment.length(); )
    {
      newLine = originalMethodComment.indexOf( "\n", i );
      if( newLine != -1 )
      {
        lineCount++;
        i = newLine + 1;
      }
    }
    assertEquals( 9, lineCount );

    LocalQuickFixBase localQuickFixBase = new IntroduceReference( psiMethod, interfaceMethod, true );
    assertEquals( "Introduce JavaDoc reference to parent method", localQuickFixBase.getName() );
    System.out.println( originalMethodComment );

    localQuickFixBase.doFix();
    PsiDocComment psiDocComment = psiMethod.getDocComment();
    assertNotNull( psiDocComment );
    String text = psiDocComment.getText();
    assertTrue( text.indexOf( "@see" ) > 0 );

    assertTrue( text.indexOf( "@return" ) == -1 );
    assertTrue( text.indexOf( "@param" ) == -1 );

    lineCount = 1;
    newLine = 0;
    for( int i = 0; newLine != -1 && i < text.length(); )
    {
      newLine = text.indexOf( "\n", i );
      if( newLine != -1 )
      {
        lineCount++;
        i = newLine + 1;
      }
    }
    assertEquals( 1, lineCount );
    System.out.println( text );
  }

  /**
   * Method testCopyDocComment
   *
   */
  public void testCopyFromParent()
  {
    PsiMethod interfaceMethod = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    PsiMethod psiMethod = findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithJavaDoc" );

    assertNotNull( interfaceMethod );
    assertNotNull( psiMethod );

    PsiDocComment superDocComment = interfaceMethod.getDocComment();
    PsiDocComment childDocComment = psiMethod.getDocComment();
    assertNotNull( superDocComment );
    assertNotNull( childDocComment );

    String originalSuperComment = superDocComment.getText();

    LocalQuickFixBase localQuickFixBase = new CopyFromParent( psiMethod, interfaceMethod );
    assertEquals( "Copy JavaDoc from parent method", localQuickFixBase.getName() );
    localQuickFixBase.doFix();
    PsiDocComment docComment = psiMethod.getDocComment();
    assertNotNull( docComment );
    assertEquals( originalSuperComment, docComment.getText() );
  }

  /**
   * Method testCopyDocComment
   *
   */
  public void testMoveToParentAndIntroduceReference()
  {
    PsiMethod interfaceMethod = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    PsiMethod psiMethod = findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithJavaDoc" );

    assertNotNull( interfaceMethod );
    assertNotNull( psiMethod );

    PsiDocComment superDocComment = interfaceMethod.getDocComment();
    PsiDocComment childDocComment = psiMethod.getDocComment();
    assertNotNull( superDocComment );
    assertNotNull( childDocComment );

    String originalChildComment = childDocComment.getText();
    String originalSuperComment = superDocComment.getText();

    LocalQuickFixBase localQuickFixBase = new MoveToParentAndIntroduceReference( psiMethod, interfaceMethod, false );
    assertEquals( "Move JavaDoc to parent class and introduce reference", localQuickFixBase.getName() );

    localQuickFixBase.doFix();
    childDocComment = psiMethod.getDocComment();
    superDocComment = interfaceMethod.getDocComment();
    assertNotNull( childDocComment );
    assertNotNull( superDocComment );

    System.out.println( childDocComment.getText() );
    assertFalse( originalChildComment.equals( childDocComment.getText() ) );
    assertFalse( originalSuperComment.equals( superDocComment.getText() ) );

    assertEquals( originalChildComment, superDocComment.getText() );
    assertTrue( childDocComment.getText().indexOf( "@see" ) > 0 );
  }

  /**
   * Method testReplaceParentDoc
   *
   */
  public void testReplaceParentDoc()
  {
    PsiMethod interfaceMethod = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    PsiMethod psiMethod = findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithJavaDoc" );

    assertNotNull( interfaceMethod );
    assertNotNull( psiMethod );

    PsiDocComment superDocComment = interfaceMethod.getDocComment();
    PsiDocComment childDocComment = psiMethod.getDocComment();
    assertNotNull( superDocComment );
    assertNotNull( childDocComment );

    String originalMethodComment = childDocComment.getText();

    LocalQuickFixBase localQuickFixBase = new ReplaceParentDoc( psiMethod, interfaceMethod );
    if( interfaceMethod.getDocComment() != null )
    {
      assertEquals( "Replace parent method JavaDoc with this JavaDoc", localQuickFixBase.getName() );
    }
    else
    {
      assertEquals( "Copy JavaDoc to parent", localQuickFixBase.getName() );
    }
    localQuickFixBase.doFix();

    PsiDocComment docComment = interfaceMethod.getDocComment();
    assertNotNull( docComment );
    assertEquals( originalMethodComment, docComment.getText() );
  }

  /**
   * Method testTryToBreakMissingMethodJavaDocInspection ...
   */
  public void testTryToBreakMissingMethodJavaDocInspection()
  {
    MissingMethodJavaDocInspection mmjdi = new MissingMethodJavaDocInspection();
    Method determineIntroduceDocOptionsA = findMethod( MissingMethodJavaDocInspection.class,
                                                       "determineIntroduceDocOptions", PsiMethod.class,
                                                       InspectionManager.class );
    Method determineIntroduceDocOptionsB = findMethod( MissingMethodJavaDocInspection.class,
                                                       "determineIntroduceDocOptions", PsiMethod.class, List.class );
    assertNotNull( determineIntroduceDocOptionsA );
    assertNotNull( determineIntroduceDocOptionsB );

    boolean caughtException = false;
    try
    {
      determineIntroduceDocOptionsA.invoke( mmjdi, null, null );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );

    try
    {
      PsiElementFactory psiElementFactory = getJavaFacade().getElementFactory();
      PsiMethod mockMethod = psiElementFactory.createMethod( "MockMethod", PsiType.VOID );
      determineIntroduceDocOptionsB.invoke( mmjdi, mockMethod, new ArrayList() );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testMissingFieldJavaDoc ...
   */
  public void testMissingFieldJavaDoc()
  {
    MissingFieldJavaDocInspection fieldJavaDocInspection = new MissingFieldJavaDocInspection();
    assertEquals( "Missing Field JavaDoc declaration", fieldJavaDocInspection.getDisplayName() );

    PsiField field = classImplementingInterfaceClass.getAllFields()[0];
    GenerateFromField generateFromField = new GenerateFromField( field );
    assertNull( field.getDocComment() );

    assertEquals( "Generate JavaDoc based on field", generateFromField.getName() );
    generateFromField.doFix();

    assertNotNull( field.getDocComment() );
  }

  /**
   * Method testMissingClassJavaDoc ...
   */
  public void testMissingClassJavaDoc()
  {
    MissingClassJavaDocInspection fieldJavaDocInspection = new MissingClassJavaDocInspection();
    assertEquals( "Missing Class JavaDoc declaration", fieldJavaDocInspection.getDisplayName() );

    GenerateFromClass generateFromClass = new GenerateFromClass( classImplementingInterfaceClass );
    assertNull( classImplementingInterfaceClass.getDocComment() );

    assertEquals( "Generate JavaDoc based on class", generateFromClass.getName() );
    generateFromClass.doFix();

    assertNotNull( classImplementingInterfaceClass.getDocComment() );
  }

  /**
   * Method testSomeInterfaceInconsistencies ...
   */
  public void testSomeInterfaceInconsistencies()
  {
    PsiMethod anAbstractMethodWithJavaDoc = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    PsiMethod anAbstractMethodWithoutJavaDoc = findPsiMethod( someInterfaceClass, "anAbstractMethodWithoutJavaDoc" );
    PsiMethod overrideMe = findPsiMethod( someInterfaceClass, "overrideMe" );

    InconsistentJavaDocInspection inconsistentJavaDocInspection = new InconsistentJavaDocInspection();
    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();

    Method findFixesForInconsistency = findMethod( InconsistentJavaDocInspection.class,
                                                   "findFixesForInconsistency", PsiMethod.class, PsiMethod.class,
                                                   List.class );

    boolean caughtException = false;
    try
    {
      String problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                             null, anAbstractMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      null, anAbstractMethodWithoutJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      null, overrideMe, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testSomeInterfaceMissingJavaDoc ...
   */
  public void testSomeInterfaceMissingJavaDoc()
  {
    PsiMethod anAbstractMethodWithJavaDoc = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    PsiMethod anAbstractMethodWithoutJavaDoc = findPsiMethod( someInterfaceClass, "anAbstractMethodWithoutJavaDoc" );
    PsiMethod overrideMe = findPsiMethod( someInterfaceClass, "overrideMe" );

    MissingMethodJavaDocInspection missingMethodJavaDocInspection = new MissingMethodJavaDocInspection();
    Method determineIntroduceDocOptions = findMethod( MissingMethodJavaDocInspection.class,
                                                      "determineIntroduceDocOptions", PsiMethod.class, List.class );

    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();

    boolean caughtException = false;
    try
    {
      String problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                                anAbstractMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         anAbstractMethodWithoutJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         overrideMe, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testExtendingSomeInterfaceInconsistencies ...
   */
  public void testExtendingSomeInterfaceInconsistencies()
  {
    PsiMethod anAbstractMethodWithJavaDoc = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );
    PsiMethod anAbstractMethodWithoutJavaDoc = findPsiMethod( someInterfaceClass, "anAbstractMethodWithoutJavaDoc" );
    PsiMethod overrideMeSuper = findPsiMethod( someInterfaceClass, "overrideMe" );
    PsiMethod overrideMe = findPsiMethod( extendingInterfaceClass, "overrideMe" );
    PsiMethod aMethodWithoutJavaDoc = findPsiMethod( extendingInterfaceClass, "aMethodWithoutJavaDoc" );
    PsiMethod aMethodWithJavaDoc = findPsiMethod( extendingInterfaceClass, "aMethodWithJavaDoc" );
    PsiMethod aDifferentMethodWithJavaDoc = findPsiMethod( extendingInterfaceClass, "aDifferentMethodWithJavaDoc" );

    InconsistentJavaDocInspection inconsistentJavaDocInspection = new InconsistentJavaDocInspection();
    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();

    Method findFixesForInconsistency = findMethod( InconsistentJavaDocInspection.class,
                                                   "findFixesForInconsistency", PsiMethod.class, PsiMethod.class,
                                                   List.class );
    boolean caughtException = false;
    try
    {
      String problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                             null, anAbstractMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      null, anAbstractMethodWithoutJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      null, aMethodWithoutJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      null, aMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      null, aDifferentMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      overrideMeSuper, overrideMe, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testExtendingSomeInterfaceMissingJavaDoc ...
   */
  public void testExtendingSomeInterfaceMissingJavaDoc()
  {
    final PsiMethod overrideMeSuper = findPsiMethod( someInterfaceClass, "overrideMe" );
    PsiMethod overrideMe = findPsiMethod( extendingInterfaceClass, "overrideMe" );
    PsiMethod aMethodWithoutJavaDoc = findPsiMethod( extendingInterfaceClass, "aMethodWithoutJavaDoc" );
    PsiMethod aMethodWithJavaDoc = findPsiMethod( extendingInterfaceClass, "aMethodWithJavaDoc" );
    PsiMethod aDifferentMethodWithJavaDoc = findPsiMethod( extendingInterfaceClass, "aDifferentMethodWithJavaDoc" );

    MissingMethodJavaDocInspection missingMethodJavaDocInspection = new MissingMethodJavaDocInspection();
    Method determineIntroduceDocOptions = findMethod( MissingMethodJavaDocInspection.class,
                                                      "determineIntroduceDocOptions", PsiMethod.class, List.class );

    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();

    boolean caughtException = false;
    try
    {
      String problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                                aMethodWithoutJavaDoc, fixes );
      assertNotNull( problemDescription );
      assertEquals( LocalQuickFixBase.INTERFACE_METHOD_NO_JAVADOC, problemDescription );
      assertTrue( fixes.get( 0 ) instanceof GenerateFromSignature );

      fixes.clear();
      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         aMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         aDifferentMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      fixes.clear();
      missingMethodJavaDocInspection = new MissingMethodJavaDocInspection()
      {
        /** @inheritDoc */
        protected PsiMethod getSuperMethod( PsiMethod psiMethod )
        {
          return overrideMeSuper;
        }
      };
      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         overrideMe, fixes );
      assertNotNull( problemDescription );
      assertEquals( LocalQuickFixBase.OVERRIDDEN_METHOD_NO_JAVADOC, problemDescription );
      assertEquals( 2, fixes.size() );
      LocalQuickFixBase fix1 = fixes.get( 0 );
      LocalQuickFixBase fix2 = fixes.get( 1 );
      assertFalse( fix1.getClass().getName().equals( fix2.getClass().getName() ) );
      assertTrue( fix1 instanceof CopyFromParent );
      assertTrue( fix2 instanceof IntroduceReference );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testClassImplementingInterfaceInconsistencies ...
   */
  public void testClassImplementingInterfaceInconsistencies()
  {
    PsiMethod aMethodWithoutJavaDoc = findPsiMethod( classImplementingInterfaceClass, "aMethodWithoutJavaDoc" );
    PsiMethod superAMethodWithoutJavaDoc = findPsiMethod( extendingInterfaceClass, "aMethodWithoutJavaDoc" );

    PsiMethod aMethodWithJavaDoc = findPsiMethod( classImplementingInterfaceClass, "aMethodWithJavaDoc" );
    PsiMethod superAMethodWithJavaDoc = findPsiMethod( extendingInterfaceClass, "aMethodWithJavaDoc" );

    PsiMethod anAbstractMethodWithJavaDoc =
        findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithJavaDoc" );
    PsiMethod superAnAbstractMethodWithJavaDoc = findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );

    PsiMethod anAbstractMethodWithoutJavaDoc =
        findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithoutJavaDoc" );
    PsiMethod superAnAbstractMethodWithoutJavaDoc =
        findPsiMethod( someInterfaceClass, "anAbstractMethodWithoutJavaDoc" );

    PsiMethod aDifferentMethodWithJavaDoc =
        findPsiMethod( classImplementingInterfaceClass, "aDifferentMethodWithJavaDoc" );
    PsiMethod superADifferentMethodWithJavaDoc = findPsiMethod( extendingInterfaceClass, "aDifferentMethodWithJavaDoc" )
        ;

    PsiMethod overrideMe = findPsiMethod( classImplementingInterfaceClass, "overrideMe" );
    PsiMethod superOverrideMe = findPsiMethod( extendingInterfaceClass, "overrideMe" );

    InconsistentJavaDocInspection inconsistentJavaDocInspection = new InconsistentJavaDocInspection();
    assertEquals( "Inconsistent JavaDoc", inconsistentJavaDocInspection.getDisplayName() );
    assertEquals( "InconsistentJavaDoc", inconsistentJavaDocInspection.getShortName() );

    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();

    Method findFixesForInconsistency = findMethod( InconsistentJavaDocInspection.class,
                                                   "findFixesForInconsistency", PsiMethod.class, PsiMethod.class,
                                                   List.class );

    boolean caughtException = false;
    try
    {
      String problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                             superAMethodWithoutJavaDoc,
                                                                             aMethodWithoutJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      superAMethodWithJavaDoc, aMethodWithJavaDoc,
                                                                      fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      superAnAbstractMethodWithJavaDoc,
                                                                      anAbstractMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      superAnAbstractMethodWithoutJavaDoc,
                                                                      anAbstractMethodWithoutJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      superADifferentMethodWithJavaDoc,
                                                                      aDifferentMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      superOverrideMe, overrideMe, fixes );
      assertEquals( LocalQuickFixBase.JAVADOC_DIFFERS_FROM_PARENT, problemDescription );
      assertEquals( 2, fixes.size() );
      assertTrue( fixes.get( 0 ) instanceof MoveToParentAndIntroduceReference );
      assertTrue( fixes.get( 1 ) instanceof ReplaceParentDoc );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testClassImplementingInterfaceMissingJavaDoc ...
   */
  public void testClassImplementingInterfaceMissingJavaDoc()
  {
    PsiMethod aMethodWithoutJavaDoc = findPsiMethod( classImplementingInterfaceClass, "aMethodWithoutJavaDoc" );
    final PsiMethod superAMethodWithoutJavaDoc = findPsiMethod( extendingInterfaceClass, "aMethodWithoutJavaDoc" );

    PsiMethod aMethodWithJavaDoc = findPsiMethod( classImplementingInterfaceClass, "aMethodWithJavaDoc" );
    final PsiMethod superAMethodWithJavaDoc = findPsiMethod( extendingInterfaceClass, "aMethodWithJavaDoc" );

    PsiMethod anAbstractMethodWithJavaDoc =
        findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithJavaDoc" );
    final PsiMethod superAnAbstractMethodWithJavaDoc =
        findPsiMethod( someInterfaceClass, "anAbstractMethodWithJavaDoc" );

    PsiMethod anAbstractMethodWithoutJavaDoc =
        findPsiMethod( classImplementingInterfaceClass, "anAbstractMethodWithoutJavaDoc" );
    final PsiMethod superAnAbstractMethodWithoutJavaDoc =
        findPsiMethod( someInterfaceClass, "anAbstractMethodWithoutJavaDoc" );

    PsiMethod aDifferentMethodWithJavaDoc =
        findPsiMethod( classImplementingInterfaceClass, "aDifferentMethodWithJavaDoc" );
    final PsiMethod superADifferentMethodWithJavaDoc =
        findPsiMethod( extendingInterfaceClass, "aDifferentMethodWithJavaDoc" );

    PsiMethod overrideMe = findPsiMethod( classImplementingInterfaceClass, "overrideMe" );
    final PsiMethod superOverrideMe = findPsiMethod( extendingInterfaceClass, "overrideMe" );

    MissingMethodJavaDocInspection missingMethodJavaDocInspection;
    Method determineIntroduceDocOptions = findMethod( MissingMethodJavaDocInspection.class,
                                                      "determineIntroduceDocOptions", PsiMethod.class, List.class );

    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();

    boolean caughtException = false;
    try
    {
      missingMethodJavaDocInspection = new MissingMethodJavaDocInspection()
      {
        /** @inheritDoc */
        protected PsiMethod getSuperMethod( PsiMethod psiMethod )
        {
          return superAMethodWithoutJavaDoc;
        }
      };
      assertEquals( "Missing Method JavaDoc declaration", missingMethodJavaDocInspection.getDisplayName() );
      assertEquals( "MissingMethodJavaDoc", missingMethodJavaDocInspection.getShortName() );

      String problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                                aMethodWithoutJavaDoc, fixes );
      assertEquals( LocalQuickFixBase.IMPLEMENTATION_NO_JAVADOC, problemDescription );
      assertEquals( 1, fixes.size() );
      assertTrue( fixes.get( 0 ) instanceof GenerateFromSignature );

      fixes.clear();
      missingMethodJavaDocInspection = new MissingMethodJavaDocInspection()
      {
        /** @inheritDoc */
        protected PsiMethod getSuperMethod( PsiMethod psiMethod )
        {
          return superAMethodWithJavaDoc;
        }
      };
      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         aMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      missingMethodJavaDocInspection = new MissingMethodJavaDocInspection()
      {
        /** @inheritDoc */
        protected PsiMethod getSuperMethod( PsiMethod psiMethod )
        {
          return superAnAbstractMethodWithJavaDoc;
        }
      };
      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         anAbstractMethodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      missingMethodJavaDocInspection = new MissingMethodJavaDocInspection()
      {
        /** @inheritDoc */
        protected PsiMethod getSuperMethod( PsiMethod psiMethod )
        {
          return superAnAbstractMethodWithoutJavaDoc;
        }
      };
      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         anAbstractMethodWithoutJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

      missingMethodJavaDocInspection = new MissingMethodJavaDocInspection()
      {
        /** @inheritDoc */
        protected PsiMethod getSuperMethod( PsiMethod psiMethod )
        {
          return superADifferentMethodWithJavaDoc;
        }
      };
      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         aDifferentMethodWithJavaDoc, fixes );
      assertEquals( LocalQuickFixBase.IMPLEMENTATION_NO_JAVADOC, problemDescription );
      assertEquals( 2, fixes.size() );
      assertTrue( fixes.get( 0 ) instanceof CopyFromParent );
      assertTrue( fixes.get( 1 ) instanceof IntroduceReference );

      PsiElement[] psiElements = aDifferentMethodWithJavaDoc.getChildren();
      assertTrue( psiElements[0] instanceof PsiComment );
      assertTrue( psiElements[1] instanceof PsiWhiteSpace );
      assertTrue( psiElements[2] instanceof PsiModifierList );

      IntroduceReference ir =
          new IntroduceReference( aDifferentMethodWithJavaDoc, superADifferentMethodWithJavaDoc, false );
      ir.doFix();

      psiElements = aDifferentMethodWithJavaDoc.getChildren();
      assertTrue( psiElements[0] instanceof PsiDocComment );
      assertTrue( psiElements[1] instanceof PsiWhiteSpace );
      assertTrue( psiElements[2] instanceof PsiComment );
      assertTrue( psiElements[3] instanceof PsiWhiteSpace );
      assertTrue( psiElements[4] instanceof PsiModifierList );

      fixes.clear();
      missingMethodJavaDocInspection = new MissingMethodJavaDocInspection()
      {
        /** @inheritDoc */
        protected PsiMethod getSuperMethod( PsiMethod psiMethod )
        {
          return superOverrideMe;
        }
      };
      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         overrideMe, fixes );
      assertEquals( null, problemDescription );
      assertTrue( fixes.isEmpty() );

//          Unclear how to obtain a handle to the PsiMethod of the Anonymous class...
//            PsiMethod[] psiMethods = classImplementingInterfaceClass.getAllMethods();
//            PsiMethod psiMethod = null;
//            for (int i = 0; i < psiMethods.length; i++) {
//                if ("toString".equals(psiMethods[i].getName()) &&
//                    psiMethods[i].getContainingClass() instanceof PsiAnonymousClass) {
//                    psiMethod = psiMethods[i];
//                }
//            }
//            assertNotNull(psiMethod);
//            missingMethodJavaDocInspection = new MissingMethodJavaDocInspection() {
//                protected PsiMethod getSuperMethod(PsiMethod psiMethod) {
//                    return psiMethod;
//                }
//            };
//
//            problemDescription = (String) determineIntroduceDocOptions.invoke(missingMethodJavaDocInspection,
//                    overrideMe, fixes);
//            assertEquals(LocalQuickFixBase.OVERRIDDEN_METHOD_ANONYMOUS_CLASS_NO_JAVADOC, problemDescription);
//            assertEquals(2, fixes.isEmpty());
//            assertTrue(fixes.get(0) instanceof CopyFromParent);
//            assertTrue(fixes.get(1) instanceof IntroduceReference);
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testClassImplementingInterfaceMissingClassJavaDoc ...
   */
  public void testClassImplementingInterfaceMissingClassJavaDoc()
  {
    assertNull( classImplementingInterfaceClass.getDocComment() );

    GenerateFromClass generateFromClass = new GenerateFromClass( classImplementingInterfaceClass );
    generateFromClass.doFix();

    assertNotNull( classImplementingInterfaceClass.getDocComment() );
  }

  /**
   * Method testClassExtendingObjectInconsistencies ...
   */
  public void testClassExtendingObjectInconsistencies()
  {
    PsiMethod[] constructors = classExtendingObjectClass.getConstructors();
    PsiMethod defaultConstuctor = null;
    PsiMethod nonDefaultConstuctor = null;
    for( PsiMethod constructor : constructors )
    {
      if( constructor.getParameterList().getParametersCount() == 0 )
      {
        defaultConstuctor = constructor;
      }
      else
      {
        nonDefaultConstuctor = constructor;
      }
    }
    assertNotNull( defaultConstuctor );
    assertNotNull( nonDefaultConstuctor );

    PsiMethod methodWithoutJavaDoc = findPsiMethod( classExtendingObjectClass, "methodWithoutJavaDoc" );
    PsiMethod methodWithJavaDoc = findPsiMethod( classExtendingObjectClass, "methodWithJavaDoc" );
    PsiMethod toString = findPsiMethod( classExtendingObjectClass, "toString" );
    PsiMethod superToString = findPsiMethod( classObjectClass, "toString" );

    InconsistentJavaDocInspection inconsistentJavaDocInspection;
    Method findFixesForInconsistency = findMethod( InconsistentJavaDocInspection.class,
                                                   "findFixesForInconsistency", PsiMethod.class, PsiMethod.class,
                                                   List.class );

    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();

    boolean caughtException = false;
    try
    {
      inconsistentJavaDocInspection = new InconsistentJavaDocInspection();
      String problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                             null, defaultConstuctor, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      null, nonDefaultConstuctor, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      null, methodWithoutJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      null, methodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      superToString, toString, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testClassExtendingObjectMissingJavaDoc ...
   */
  public void testClassExtendingObjectMissingJavaDoc()
  {
    PsiMethod[] constructors = classExtendingObjectClass.getConstructors();
    PsiMethod defaultConstuctor = null;
    PsiMethod nonDefaultConstuctor = null;
    for( PsiMethod constructor : constructors )
    {
      if( constructor.getParameterList().getParametersCount() == 0 )
      {
        defaultConstuctor = constructor;
      }
      else
      {
        nonDefaultConstuctor = constructor;
      }
    }
    assertNotNull( defaultConstuctor );
    assertNotNull( nonDefaultConstuctor );

    PsiMethod methodWithoutJavaDoc = findPsiMethod( classExtendingObjectClass, "methodWithoutJavaDoc" );
    PsiMethod methodWithJavaDoc = findPsiMethod( classExtendingObjectClass, "methodWithJavaDoc" );
    PsiMethod toString = findPsiMethod( classExtendingObjectClass, "toString" );

    MissingMethodJavaDocInspection missingMethodJavaDocInspection;
    Method determineIntroduceDocOptions = findMethod( MissingMethodJavaDocInspection.class,
                                                      "determineIntroduceDocOptions", PsiMethod.class, List.class );

    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();

    boolean caughtException = false;
    try
    {
      missingMethodJavaDocInspection = new MissingMethodJavaDocInspection();
      String problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                                defaultConstuctor, fixes );
      assertEquals( LocalQuickFixBase.BASE_METHOD_NO_JAVADOC, problemDescription );
      assertEquals( 1, fixes.size() );
      assertTrue( fixes.get( 0 ) instanceof GenerateFromSignature );

      fixes.clear();
      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         nonDefaultConstuctor, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         methodWithoutJavaDoc, fixes );
      assertEquals( LocalQuickFixBase.BASE_METHOD_NO_JAVADOC, problemDescription );
      assertEquals( 1, fixes.size() );
      assertTrue( fixes.get( 0 ) instanceof GenerateFromSignature );
      fixes.clear();

      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         methodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         toString, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testClassExtendingCustomClassInconsistencies ...
   */
  public void testClassExtendingCustomClassInconsistencies()
  {
    PsiMethod[] superConstructors = classExtendingObjectClass.getConstructors();
    PsiMethod superDefaultConstuctor = null;
    PsiMethod superNonDefaultConstuctor = null;
    for( PsiMethod superConstructor : superConstructors )
    {
      if( superConstructor.getParameterList().getParametersCount() == 0 )
      {
        superDefaultConstuctor = superConstructor;
      }
      else
      {
        superNonDefaultConstuctor = superConstructor;
      }
    }
    assertNotNull( superDefaultConstuctor );
    assertNotNull( superNonDefaultConstuctor );

    PsiMethod[] constructors = classExtendingCustomClassClass.getConstructors();
    PsiMethod defaultConstuctor = null;
    PsiMethod nonDefaultConstuctor = null;
    for( PsiMethod constructor : constructors )
    {
      if( constructor.getParameterList().getParametersCount() == 0 )
      {
        defaultConstuctor = constructor;
      }
      else
      {
        nonDefaultConstuctor = constructor;
      }
    }
    assertNotNull( defaultConstuctor );
    assertNotNull( nonDefaultConstuctor );

    PsiMethod methodWithoutJavaDoc = findPsiMethod( classExtendingCustomClassClass, "methodWithoutJavaDoc" );
    PsiMethod superMethodWithoutJavaDoc = findPsiMethod( classExtendingObjectClass, "methodWithoutJavaDoc" );
    PsiMethod toString = findPsiMethod( classExtendingCustomClassClass, "toString" );
    PsiMethod superToString = findPsiMethod( classObjectClass, "toString" );
    PsiMethod hashCode = findPsiMethod( classExtendingCustomClassClass, "hashCode" );
    PsiMethod superHashCode = findPsiMethod( classObjectClass, "hashCode" );

    InconsistentJavaDocInspection inconsistentJavaDocInspection;
    Method findFixesForInconsistency = findMethod( InconsistentJavaDocInspection.class,
                                                   "findFixesForInconsistency", PsiMethod.class, PsiMethod.class,
                                                   List.class );

    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();

    boolean caughtException = false;
    try
    {
      inconsistentJavaDocInspection = new InconsistentJavaDocInspection();
      String problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                             superDefaultConstuctor, defaultConstuctor,
                                                                             fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      superNonDefaultConstuctor, nonDefaultConstuctor,
                                                                      fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      superMethodWithoutJavaDoc, methodWithoutJavaDoc,
                                                                      fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      superHashCode, hashCode, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) findFixesForInconsistency.invoke( inconsistentJavaDocInspection,
                                                                      superToString, toString, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testClassExtendingCustomClassMissingJavaDoc ...
   */
  public void testClassExtendingCustomClassMissingJavaDoc()
  {
    PsiMethod[] constructors = classExtendingObjectClass.getConstructors();
    PsiMethod defaultConstuctor = null;
    PsiMethod nonDefaultConstuctor = null;
    for( PsiMethod constructor : constructors )
    {
      if( constructor.getParameterList().getParametersCount() == 0 )
      {
        defaultConstuctor = constructor;
      }
      else
      {
        nonDefaultConstuctor = constructor;
      }
    }
    assertNotNull( defaultConstuctor );
    assertNotNull( nonDefaultConstuctor );

    PsiMethod methodWithoutJavaDoc = findPsiMethod( classExtendingObjectClass, "methodWithoutJavaDoc" );
    PsiMethod methodWithJavaDoc = findPsiMethod( classExtendingObjectClass, "methodWithJavaDoc" );
    PsiMethod toString = findPsiMethod( classExtendingObjectClass, "toString" );

    MissingMethodJavaDocInspection missingMethodJavaDocInspection;
    Method determineIntroduceDocOptions = findMethod( MissingMethodJavaDocInspection.class,
                                                      "determineIntroduceDocOptions", PsiMethod.class, List.class );

    List<LocalQuickFixBase> fixes = new ArrayList<LocalQuickFixBase>();

    boolean caughtException = false;
    try
    {
      missingMethodJavaDocInspection = new MissingMethodJavaDocInspection();
      String problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                                defaultConstuctor, fixes );
      assertEquals( LocalQuickFixBase.BASE_METHOD_NO_JAVADOC, problemDescription );
      assertEquals( 1, fixes.size() );
      assertTrue( fixes.get( 0 ) instanceof GenerateFromSignature );

      fixes.clear();
      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         nonDefaultConstuctor, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         methodWithoutJavaDoc, fixes );
      assertEquals( LocalQuickFixBase.BASE_METHOD_NO_JAVADOC, problemDescription );
      assertEquals( 1, fixes.size() );
      assertTrue( fixes.get( 0 ) instanceof GenerateFromSignature );
      fixes.clear();

      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         methodWithJavaDoc, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );

      problemDescription = (String) determineIntroduceDocOptions.invoke( missingMethodJavaDocInspection,
                                                                         toString, fixes );
      assertEquals( null, problemDescription );
      assertEquals( 0, fixes.size() );
    }
    catch( Exception e )
    {
      caughtException = true;
      e.printStackTrace();
    }
    assertFalse( caughtException );
  }

  /**
   * Method testJavaDocSyncPlugin ...
   */
  public void testJavaDocSyncPlugin()
  {
    JavaDocManager javaDocSyncPlugin = new JavaDocManager();
    javaDocSyncPlugin.initComponent();
    assertEquals( "JavaDocManager", javaDocSyncPlugin.getComponentName() );
    assertEquals( 4, javaDocSyncPlugin.getInspectionClasses().length );
    javaDocSyncPlugin.disposeComponent();
  }

  /**
   * Method findMethod
   *
   * @param clazz class to look in
   * @param arguments variable number of Class arguments
   * @param methodName method name to look for
   * @return Method the method or null
   */
  private Method findMethod( Class clazz, String methodName, Class... arguments )
  {
    Method method = null;
    try
    {
      method = clazz.getDeclaredMethod( methodName, arguments );
      method.setAccessible( true );
    }
    catch( NoSuchMethodException e )
    {
      fail( e.getMessage() );
    }
    return method;
  }

  /**
   * Method findPsiMethod
   *
   * @param psiClass class to look in
   * @param methodName method name to look for
   * @return PsiMethod the method or null
   */
  private PsiMethod findPsiMethod( PsiClass psiClass, String methodName )
  {
    assertNotNull( psiClass );
    assertNotNull( methodName );

    PsiMethod psiMethod = null;
    PsiMethod[] methodDefinitions = psiClass.getMethods();
    for( PsiMethod methodDefinition : methodDefinitions )
    {
      if( methodName.equals( methodDefinition.getName() ) )
      {
        psiMethod = methodDefinition;
      }
    }
    return psiMethod;
  }
}
