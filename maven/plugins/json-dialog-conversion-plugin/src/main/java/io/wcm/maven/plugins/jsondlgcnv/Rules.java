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
import java.util.Set;
import java.util.TreeSet;

import org.apache.sling.api.resource.Resource;

/**
 * Access to all node-based rules.
 */
class Rules {

  private final Set<Rule> rules = new TreeSet<>();

  Rules(Resource rulesRoot) {
    Iterator<Resource> ruleResouces = rulesRoot.listChildren();
    while (ruleResouces.hasNext()) {
      rules.add(new Rule(ruleResouces.next()));
    }
  }

  /**
   * Get rule matching for the given GraniteUI resource.
   * @param resource GraniteUIR resource
   * @return matching rule or null
   */
  public Rule getRule(Resource resource) {
    for (Rule rule : rules) {
      if (rule.matches(resource)) {
        return rule;
      }
    }
    return null;
  }

}
