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

import io.wcm.tooling.netbeans.sightly.completion.dataSly.DataSlyCommands;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.java.classpath.ClassPath;

import static io.wcm.tooling.netbeans.sightly.completion.classLookup.MemberLookupCompleter.GETTER_PATTERN;
import static io.wcm.tooling.netbeans.sightly.completion.classLookup.ParsedStatement.PATTERN;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementUtilities;

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
public class MemberLookupResolver extends AbstractSourceResolver {

  private final String[] commands;
  private static final Logger LOGGER = Logger.getLogger(MemberLookupResolver.class.getName());

  /**
   *
   * @param text Text to use as base for resolving
   * @param classPath
   */
  public MemberLookupResolver(String text, ClassPath classPath) {
    super(classPath);
    this.commands = StringUtils.splitByWholeSeparator(text, "data-sly-");
  }

  /**
   * The actual lookup
   *
   * @return set of all elements which match the lookup
   */
  public Set<MemberLookupResult> performMemberLookup(String variable) {
    // if there is more than one "." we need to do some magic and resolve the definition fragmented
    if (variable.contains(".")) {
      return performNestedLookup(variable);
    }

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
   * performs a nested lookup. E.g for foo.bar it will resolve the type of bar and then get it's methods
   *
   * @param variable e.g. foo.bar
   * @return set with matching results
   */
  private Set<MemberLookupResult> performNestedLookup(String variable) {
    Set<MemberLookupResult> ret = new LinkedHashSet<>();
    // start with the first part
    String[] parts = StringUtils.split(variable, ".");
    if (parts.length > 2) {
      Set<MemberLookupResult> subResult = performNestedLookup(StringUtils.substringBeforeLast(variable, "."));
      for (MemberLookupResult result : subResult) {
        if (result.matches(parts[parts.length - 1])) {
          ret.addAll(getResultsForClass(result.getReturnType(), variable));
        }
      }
    }
    else {
      Set<MemberLookupResult> subResults = performMemberLookup(parts[0]);
      for (MemberLookupResult result : subResults) {
        if (result.matches(parts[1])) {
          // we found a method which has the correct name, now we can resolv this
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

  private Set<MemberLookupResult> getMethodsFromJavaSource(String clazzname, String variable) {
    Set<MemberLookupResult> ret = new LinkedHashSet<>();
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

    Set<Element> elems = getMembersFromJavaSource(clazzname, acceptor);
    for (Element e : elems) {
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
   *
   * @param variable
   * @return the statement for the given variablename
   */
  private ParsedStatement getParsedStatement(String variable) {
    for (String command : commands) {
      String candidate = command.replaceAll("\n", "").replaceAll("\r", "");
      Matcher m = PATTERN.matcher("data-sly-" + candidate);
      while (m.find()) {
        ParsedStatement statement = ParsedStatement.fromMatcher(m);
        // we only want the first match
        if (statement != null && statement.getVariable().startsWith(variable)) {
          return statement;
        }
      }
    }
    return null;
  }
}
