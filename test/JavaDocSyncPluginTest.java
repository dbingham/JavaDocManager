import com.intellij.ide.startup.impl.StartupManagerImpl;
import com.intellij.lang.properties.PropertiesReferenceManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.impl.ModuleManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.IdeaTestCase;
import com.intellij.testFramework.PsiTestUtil;
import com.perniciouspenguins.ideaz.javadoc.util.PsiUtils;

import java.io.IOException;

/**
 * Add
 * -Didea.plugins.load=false -Xbootclasspath/p:$IDEA_HOME/lib/boot.jar
 * to VM parameters
 *
 */
public class JavaDocSyncPluginTest extends IdeaTestCase
{
  protected void setUpProject() throws IOException
  {
    final String root = "D:\\home\\raymond\\Development\\Java\\IntelliJPlugins\\JavaDocManager\\1.0.2";
    myProject = createProjectFrom( root );
    setUpJdk();
    myModule = ModuleManager.getInstance( myProject ).getModules()[0];
  }

  static Project createProjectFrom( final String root )
  {
    VirtualFile tempProjectRootDir = prepareProjectDirectory( root );
    Project project = loadProjectComponents( tempProjectRootDir );

    ((ModuleManagerImpl) ModuleManager.getInstance( project )).projectOpened();

    PropertiesReferenceManager.getInstance( project ).projectOpened();
    ((StartupManagerImpl) StartupManager.getInstance( project )).runStartupActivities();
    ((StartupManagerImpl) StartupManager.getInstance( project )).runPostStartupActivities();
    return project;
  }

  private static Project loadProjectComponents( VirtualFile tempProjectRootDir )
  {
    Project project = null;
    try
    {
      VirtualFile[] children = tempProjectRootDir.getChildren();
      for( VirtualFile virtualFile : children )
      {
        if( FileTypeManager.getInstance().getFileTypeByFile( virtualFile ) == StdFileTypes.IDEA_PROJECT )
        {
          project = ProjectManagerEx.getInstanceEx().loadProject( virtualFile.getPath() );
          break;
        }
      }
    }
    catch( Exception e )
    {
      LOG.error( e );
    }
    return project;
  }

  private static VirtualFile prepareProjectDirectory( final String root )
  {
    return ApplicationManager.getApplication().runWriteAction( new Computable<VirtualFile>()
    {
      public VirtualFile compute()
      {
        try
        {
          return PsiTestUtil
              .createTestProjectStructure( null, FileUtil.toSystemIndependentName( root ), myFilesToDelete, false );
        }
        catch( Exception e )
        {
          LOG.error( e );
          return null;
        }
      }
    } );
  }

  public void testGetSuperDoesNotGoDepthFirst() throws Throwable
  {
    //PsiManager psiManager = PsiManager.getInstance(myProject);
    Module module = ModuleManager.getInstance( myProject ).findModuleByName( "JavaDocManager" );

    assertNotNull( module );
    PsiClass classImplementingInterfacePsiClass = getJavaFacade().findClass( "p1.p2.ClassImplementingInterface",
                                                                             GlobalSearchScope.moduleWithDependenciesScope(
                                                                                 module ) );
    assertNotNull( classImplementingInterfacePsiClass );

    PsiMethod[] psiMethods = classImplementingInterfacePsiClass.getAllMethods();
    PsiMethod psiMethod = null;
    for( int i = 0; psiMethod == null && i < psiMethods.length; i++ )
    {
      if( "overrideMe".equals( psiMethods[i].getName() ) )
      {
        psiMethod = psiMethods[i];
      }
    }
    assertNotNull( psiMethod );

    PsiMethod superMethod = PsiUtils.getSuperMethod( psiMethod );
    PsiClass superClass = superMethod.getContainingClass();
    assert superClass != null;
    assertEquals( "ExtendingSomeInterface", superClass.getName() );
  }
}
