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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ElementUtilities;

/**
 * resolves all request-attributes of a given class
 */
public class RequestAttributeResolver extends AbstractSourceResolver {

  private static final Logger LOGGER = Logger.getLogger(RequestAttributeResolver.class.getName());

  private static final String REQUEST_ATTRIBUTE_CLASSNAME = "org.apache.sling.models.annotations.injectorspecific.RequestAttribute";

  private final String text;

  public RequestAttributeResolver(String text, ClassPath classPath) {
    super(classPath);
    this.text = text;
  }

  public Set<String> resolve(String filter) {
    Set<String> ret = new LinkedHashSet<>();
    // only display results if filter contains @
    if (!StringUtils.contains(text, "@") || !StringUtils.contains(text, "data-sly-use")) {
      return ret;
    }
    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
      @Override
      public boolean accept(Element e, TypeMirror type) {
        // we are looking for the annotations
        for (AnnotationMirror mirror : e.getAnnotationMirrors()) {
          if (mirror.getAnnotationType() != null && mirror.getAnnotationType().asElement() != null
                  && StringUtils.equalsIgnoreCase(REQUEST_ATTRIBUTE_CLASSNAME, mirror.getAnnotationType().asElement().toString())) {
            return true;
          }
        }
        return false;
      }
    };
    String clazz = StringUtils.substringBetween(text, "'");

    Set<Element> elems = getMembersFromJavaSource(clazz, acceptor);
    for (Element elem : elems) {
      if (StringUtils.startsWithIgnoreCase(elem.getSimpleName().toString(), filter)
              && !StringUtils.contains(text, elem.getSimpleName().toString() + " ")
              && !StringUtils.contains(text, elem.getSimpleName().toString() + "=")) {
        ret.add(elem.getSimpleName().toString());
      }
    }
    if (ret.isEmpty()) {
      for (String att : getAttributesFromClassLoader(clazz)) {
        if (StringUtils.startsWithIgnoreCase(att, filter)
                && !StringUtils.contains(text, att + " ")
                && !StringUtils.contains(text, att + "=")) {
          ret.add(att);
        }
      }
    }
    return ret;
  }

  private Set<String> getAttributesFromClassLoader(String clazzname) {
    final Set<String> ret = new LinkedHashSet<>();
    try {
      Class clazz = classPath.getClassLoader(true).loadClass(clazzname);
      for (Method method : clazz.getDeclaredMethods()) {
        for (Annotation annotation : method.getAnnotations()) {
          if (annotation.annotationType().getName().equals(REQUEST_ATTRIBUTE_CLASSNAME)) {
            ret.add(method.getName());
          }
        }
      }
      for (Field field : clazz.getDeclaredFields()) {
        for (Annotation annotation : field.getAnnotations()) {
          if (annotation.annotationType().getName().equals(REQUEST_ATTRIBUTE_CLASSNAME)) {
            ret.add(field.getName());
          }
        }
      }
    }
    catch (ClassNotFoundException cnfe) {
      LOGGER.log(Level.FINE, "Could not resolve class " + clazzname, cnfe);
    }
    return ret;
  }

}
