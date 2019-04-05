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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.config.DefaultWorkspaceFilter;
import org.apache.jackrabbit.vault.fs.filter.DefaultPathFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

class FiltersTest {

  private Filters underTest;

  @BeforeEach
  void setUp() {
    underTest = new Filters();
  }

  @Test
  void testMergeWithEmptyFilter() {
    underTest.addFilter(newFilter("/content/test1"));
    underTest.addFilter(newFilter("/content/test2", new String[] { "include1" }, new String[] { "exclude1" }));

    DefaultWorkspaceFilter filter = new DefaultWorkspaceFilter();
    underTest.merge(filter);
    assertEquals(ImmutableList.of(
        newFilterSet("/content/test1"),
        newFilterSet("/content/test2", new String[] { "include1" }, new String[] { "exclude1" })),
        filter.getFilterSets());
  }

  @Test
  void testMerge() {
    underTest.addFilter(newFilter("/content/test1"));
    underTest.addFilter(newFilter("/content/test2", new String[] { "include1" }, new String[] { "exclude1" }));

    DefaultWorkspaceFilter filter = new DefaultWorkspaceFilter();
    filter.add(newFilterSet("/content/test3"));
    underTest.merge(filter);
    assertEquals(ImmutableList.of(
        newFilterSet("/content/test3"),
        newFilterSet("/content/test1"),
        newFilterSet("/content/test2", new String[] { "include1" }, new String[] { "exclude1" })),
        filter.getFilterSets());
  }

  @Test
  void testWithOverlap() {
    underTest.addFilter(newFilter("/content/test1"));
    underTest.addFilter(newFilter("/content/test2", new String[] { "include1" }, new String[] { "exclude1" }));

    DefaultWorkspaceFilter filter = new DefaultWorkspaceFilter();
    filter.add(newFilterSet("/content/test1"));
    underTest.merge(filter);
    assertEquals(ImmutableList.of(
        newFilterSet("/content/test1"),
        newFilterSet("/content/test2", new String[] { "include1" }, new String[] { "exclude1" })),
        filter.getFilterSets());
  }

  private Filter newFilter(String path) {
    return newFilter(path, null, null);
  }

  private Filter newFilter(String path, String[] includes, String[] excludes) {
    Filter item = new Filter();
    item.setRoot(path);
    item.setIncludes(includes);
    item.setExcludes(excludes);
    return item;
  }

  private PathFilterSet newFilterSet(String path) {
    return newFilterSet(path, null, null);
  }

  private PathFilterSet newFilterSet(String path, String[] includes, String[] excludes) {
    PathFilterSet item = new PathFilterSet(path);
    if (includes != null) {
      for (String include : includes) {
        item.addInclude(new DefaultPathFilter(include));
      }
    }
    if (excludes != null) {
      for (String exclude : excludes) {
        item.addExclude(new DefaultPathFilter(exclude));
      }
    }
    return item;
  }

}
