/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.maven.plugins.jsondlgcnv;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

class Rule {

  private final Resource rule;

  Rule(Resource rule) {
    this.rule = rule;
  }

  public String getName() {
    return rule.getName();
  }

  public boolean matches(Resource resource) {
    Resource patterns = rule.getChild("patterns");
    if (patterns == null) {
      return false;
    }
    Iterator<Resource> patternResources = patterns.listChildren();
    while (patternResources.hasNext()) {
      Resource patternResource = patternResources.next();
      if (matchesPattern(resource, patternResource)) {
        return true;
      }
    }
    return false;
  }

  private boolean matchesPattern(Resource resource, Resource pattern) {
    ValueMap resourceProps = resource.getValueMap();
    ValueMap patternProps = pattern.getValueMap();
    if (!StringUtils.equals(resource.getResourceType(), pattern.getResourceType())) {
      return false;
    }
    for (Map.Entry<String, Object> entry : patternProps.entrySet()) {
      if (StringUtils.equals(entry.getKey(), "jcr:primaryType")) {
        continue;
      }
      if (!StringUtils.equals(resourceProps.get(entry.getKey(), String.class), entry.getValue().toString())) {
        return false;
      }
    }
    Iterator<Resource> patternChildren = pattern.listChildren();
    while (patternChildren.hasNext()) {
      Resource patternChild = patternChildren.next();
      Resource child = resource.getChild(patternChild.getName());
      if (child == null) {
        return false;
      }
      if (!matchesPattern(child, patternChild)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return rule.getPath();
  }

}
