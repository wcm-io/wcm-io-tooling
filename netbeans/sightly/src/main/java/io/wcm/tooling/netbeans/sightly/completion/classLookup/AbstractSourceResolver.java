/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.tooling.netbeans.sightly.completion.classLookup;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * abstract class with helpful methods to resolve source
 */
public abstract class AbstractSourceResolver {

  protected final ClassPath classPath;

  public AbstractSourceResolver(ClassPath classPath) {
    this.classPath = classPath;
  }

  /**
   * Resolves the clazzname to a fileobject of the java-file
   *
   * @param clazzname
   * @return null or the fileobject
   */
  protected JavaSource getJavaSourceForClass(String clazzname) {
    String resource = clazzname.replaceAll("\\.", "/") + ".java";
    FileObject fileObject = classPath.findResource(resource);
    if (fileObject == null) {
      return null;
    }
    Project project = FileOwnerQuery.getOwner(fileObject);
    if (project == null) {
      return null;
    }
    SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups("java");
    for (SourceGroup sourceGroup : sourceGroups) {
      return JavaSource.create(ClasspathInfo.create(sourceGroup.getRootFolder()));
    }
    return null;
  }

  /**
   * tries to load all members for the given clazzname from javasource files.
   *
   * @param clazzname
   * @param acceptor
   * @return set with methods or empty set if class could not be loaded
   */
  protected Set<Element> getMembersFromJavaSource(final String clazzname, final ElementUtilities.ElementAcceptor acceptor) {
    final Set<Element> ret = new LinkedHashSet<>();
    JavaSource javaSource = getJavaSourceForClass(clazzname);
    if (javaSource != null) {
      try {
        javaSource.runUserActionTask(new Task<CompilationController>() {
          @Override
          public void run(CompilationController controller) throws IOException {
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TypeElement classElem = controller.getElements().getTypeElement(clazzname);
            if (classElem == null) {
              return;
            }
            ElementUtilities eu = controller.getElementUtilities();

            Iterable<? extends Element> members = eu.getMembers(classElem.asType(), acceptor);
            for (Element e : members) {
              ret.add(e);
            }
          }
        }, false);
      }
      catch (IOException ioe) {
        Exceptions.printStackTrace(ioe);
      }
    }
    return ret;
  }
}
