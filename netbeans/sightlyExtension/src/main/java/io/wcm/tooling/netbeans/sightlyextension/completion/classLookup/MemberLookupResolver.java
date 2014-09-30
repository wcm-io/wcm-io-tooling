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
package io.wcm.tooling.netbeans.sightlyextension.completion.classLookup;

import static io.wcm.tooling.netbeans.sightlyextension.completion.classLookup.MemberLookupCompleter.GETTER_PATTERN;
import static io.wcm.tooling.netbeans.sightlyextension.completion.classLookup.ParsedStatement.PATTERN;
import io.wcm.tooling.netbeans.sightlyextension.completion.dataSly.DataSlyCommands;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang.StringUtils;
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
 * This class is used to find the methods for a given variable. This included recursive lookup if the variable itself cannot be resolved.
 *
 * data-sly-use.foo="${my.Class}
 *
 * ${foo. will be resolved to all getter methods and public fields of "my.Class"
 *
 *
 * data-sly-list.bar=${foo.entries}
 *
 * ${bar. will be resolved to all getter methods and public fields of the return-type of foo.entries
 *
 */
public class MemberLookupResolver {

  private final String text;
  private final ClassPath classPath;
  private static final Logger LOGGER = Logger.getLogger(MemberLookupResolver.class.getName());

  /**
   *
   * @param text Text to use as base for resolving
   * @param classPath
   */
  public MemberLookupResolver(String text, ClassPath classPath) {
    this.text = text;
    this.classPath = classPath;
  }

  /**
   * The actual lookup
   *
   * @return set of all elements which match the lookup
   */
  public Set<MemberLookupResult> performMemberLookup(String variable) {
    Set<MemberLookupResult> ret = new LinkedHashSet<>();
    // check, if the current variable resolves to a data-sly-use command
    ParsedStatement statement = getParsedStatement(variable);
    if (statement == null) {
      return ret;
    }
    if (StringUtils.equals(statement.getCommand(), DataSlyCommands.DATA_SLY_USE.getCommand())) {
      // this ends the search and we can perform the actual lookup
      ret.addAll(getResultsForClass(statement.getValue(), variable));
    }
    else {
      Set<MemberLookupResult> subResults = performMemberLookup(StringUtils.substringBefore(statement.getValue(), "."));
      for (MemberLookupResult result : subResults) {
        if (result.matches(StringUtils.substringAfter(statement.getValue(), "."))) {
          ret.addAll(getResultsForClass(result.getReturnType(), variable));
        }
      }
    }
    return ret;
  }

  /**
   *
   * @param clazzname
   * @return set with all methods for the given clazzname
   */
  private Set<MemberLookupResult> getResultsForClass(String clazzname, String variable) {
    Set<MemberLookupResult> ret = new LinkedHashSet<>();
    ret.addAll(getMethodsFromJavaSource(clazzname, variable));
    if (ret.isEmpty()) {
      ret.addAll(getMethodsFromClassLoader(clazzname, variable));
    }
    return ret;
  }

  /**
   * tries to load all methods for the given clazzname from javasource files.
    *
   * @param clazzname
   * @param variable
   * @return set with methods or empty set if class could not be loaded
   */
  private Set<MemberLookupResult> getMethodsFromJavaSource(final String clazzname, final String variable) {
    final Set<MemberLookupResult> ret = new LinkedHashSet<>();
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
            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
              @Override
              public boolean accept(Element e, TypeMirror type) {
                // we accept only public stuff
                if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                  return false;
                }
                if (e.getKind() == ElementKind.METHOD) {
                  ExecutableElement method = (ExecutableElement)e;
                  if (method.getReturnType().getKind() != TypeKind.VOID) {
                    return GETTER_PATTERN.matcher(e.getSimpleName().toString()).matches();
                  }
                }
                return e.getKind() == ElementKind.FIELD;
              }
            };
            Iterable<? extends Element> methods = eu.getMembers(classElem.asType(), acceptor);
            for (Element e : methods) {
              if (e.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement)e;
                MemberLookupResult result = new MemberLookupResult(variable, e.getSimpleName().toString(), method.getReturnType().toString());
                ret.add(result);
              }
              else if (e.getKind() == ElementKind.FIELD) {
                MemberLookupResult result = new MemberLookupResult(variable, e.getSimpleName().toString(), e.asType().toString());
                ret.add(result);
              }
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

  /**
   * Fallback used to load the methods from classloader
   *
   * @param clazzname
   * @param variable
   * @return set with all methods, can be empty
   */
  private Set<MemberLookupResult> getMethodsFromClassLoader(String clazzname, String variable) {
    final Set<MemberLookupResult> ret = new LinkedHashSet<>();
    try {
      Class clazz = classPath.getClassLoader(true).loadClass(clazzname);
      for (Method method : clazz.getMethods()) {
        if (method.getReturnType() != Void.TYPE && GETTER_PATTERN.matcher(method.getName()).matches()) {
          ret.add(new MemberLookupResult(variable, method.getName(), method.getReturnType().getName()));
        }
      }
      for (Field field : clazz.getFields()) {
        ret.add(new MemberLookupResult(variable, field.getName(), field.getType().getName()));
      }
    }
    catch (ClassNotFoundException cnfe) {
      LOGGER.log(Level.FINE, "Could not resolve class " + clazzname + "defined for variable " + variable, cnfe);
    }
    return ret;
  }

  /**
   * Resolves the clazzname to a fileobject of the java-file
   *
   * @param clazzname
   * @return null or the fileobject
   */
  private JavaSource getJavaSourceForClass(String clazzname) {
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
   *
   * @param variable
   * @return the statement for the given variablename
   */
  private ParsedStatement getParsedStatement(String variable) {
    Matcher m = PATTERN.matcher(text);
    while (m.find()) {
      ParsedStatement statement = ParsedStatement.fromMatcher(m);
      // we only want the first match
      if (statement != null && statement.getVariable().startsWith(variable)) {
        return statement;
      }
    }
    return null;
  }
}
