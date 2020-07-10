/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package io.wcm.tooling.commons.contentpackagebuilder;

import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.JcrConstants;

import com.google.common.collect.ImmutableSet;

/**
 * Node type helper methods.
 */
final class NodeTypes {

  /**
   * Nodes with this node types should be put into an own folder in the filevault filesystem structure.
   */
  private static final Set<String> FOLDER_NODETYPES = ImmutableSet.of(
      "nt:folder",
      "sling:Folder",
      "sling:OrderedFolder",
      "cq:Page");

  private NodeTypes() {
    // static methods only
  }

  /**
   * Check if a dedicated folder should be created for the given node type.
   * @param nodeType Node type
   * @return true if a folder should be created for this node type
   */
  public static boolean isFolderNodeType(String nodeType) {
    return FOLDER_NODETYPES.contains(nodeType);
  }

  /**
   * Check if a dedicated folder should be created for the given node type.
   * @param map Node definition as map
   * @return true if a folder should be created for this node type
   */
  public static boolean hasFolderNodeType(Map<String, Object> map) {
    Object nodeType = map.get(JcrConstants.JCR_PRIMARYTYPE);
    if (nodeType instanceof String) {
      return isFolderNodeType((String)nodeType);
    }
    return false;
  }

}
