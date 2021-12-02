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
package io.wcm.maven.plugins.contentpackage.pack;

import java.util.ArrayList;
import java.util.List;

import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.config.ConfigurationException;
import org.apache.jackrabbit.vault.fs.config.DefaultWorkspaceFilter;
import org.apache.jackrabbit.vault.fs.filter.DefaultPathFilter;

/**
 * List of {@link Filter} items.
 */
public final class Filters {

  private final List<Filter> filters = new ArrayList<>();

  /**
   * @param filter Filter definition
   */
  public void addFilter(Filter filter) {
    filters.add(filter);
  }

  /**
   * Merge configured filter paths with existing workspace filter definition.
   * @param workspaceFilter Filter
   * @throws ConfigurationException Configuration exception
   */
  void merge(DefaultWorkspaceFilter workspaceFilter) throws ConfigurationException {
    for (Filter item : filters) {
      PathFilterSet filterSet = toFilterSet(item);
      boolean exists = false;
      for (PathFilterSet existingFilterSet : workspaceFilter.getFilterSets()) {
        if (filterSet.equals(existingFilterSet)) {
          exists = true;
        }
      }
      if (!exists) {
        workspaceFilter.add(filterSet);
      }
    }
  }

  private PathFilterSet toFilterSet(Filter filter) throws ConfigurationException {
    PathFilterSet filterSet = new PathFilterSet(filter.getRoot());
    if (filter.getIncludes() != null) {
      for (String include : filter.getIncludes()) {
        filterSet.addInclude(new DefaultPathFilter(include));
      }
    }
    if (filter.getExcludes() != null) {
      for (String exclude : filter.getExcludes()) {
        filterSet.addExclude(new DefaultPathFilter(exclude));
      }
    }
    return filterSet;
  }

}
