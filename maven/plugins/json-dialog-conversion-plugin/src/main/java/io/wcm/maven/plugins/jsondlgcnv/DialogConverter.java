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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.fsprovider.internal.mapper.ContentFile;
import org.apache.sling.testing.mock.sling.junit.SlingContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

//CHECKSTYLE:OFF
/**
 * This converter logic is heavily based on
 * <a href=
 * "https://github.com/Adobe-Marketing-Cloud/aem-dialog-conversion/blob/master/bundles/cq-dialog-conversion/src/main/java/com/adobe/cq/dialogconversion/impl/rules/NodeBasedRewriteRule.java">NodeBasedRewriteRule</a>
 * and uses exactly the same logic - but operations on local JSON files as output.
 */
//CHECKSTYLE:ON
class DialogConverter {

  // pattern that matches the regex for mapped properties: ${<path>}
  private static final Pattern MAPPED_PATTERN = Pattern.compile("^(\\!{0,1})\\$\\{(\'.*?\'|.*?)(:(.+))?\\}$");

  // special properties
  private static final String PROPERTY_MAP_CHILDREN = "cq:rewriteMapChildren";
  private static final String PROPERTY_IS_FINAL = "cq:rewriteFinal";
  private static final String PROPERTY_COMMON_ATTRS = "cq:rewriteCommonAttrs";
  private static final String PROPERTY_RENDER_CONDITION = "cq:rewriteRenderCondition";

  // special nodes
  private static final String NN_CQ_REWRITE_PROPERTIES = "cq:rewriteProperties";

  // node names
  private static final String NN_RENDER_CONDITION = "rendercondition";
  private static final String NN_GRANITE_RENDER_CONDITION = "granite:rendercondition";
  private static final String NN_GRANITE_DATA = "granite:data";

  // Granite
  private static final String[] GRANITE_COMMON_ATTR_PROPERTIES = { "id", "rel", "class", "title", "hidden", "itemscope", "itemtype", "itemprop" };
  private static final String RENDER_CONDITION_CORAL2_RESOURCE_TYPE_PREFIX = "granite/ui/components/foundation/renderconditions";
  private static final String RENDER_CONDITION_CORAL3_RESOURCE_TYPE_PREFIX = "granite/ui/components/coral/foundation/renderconditions";
  private static final String DATA_PREFIX = "data-";

  private final Rules rules;
  private final Resource sourceRoot;
  private final Log log;

  DialogConverter(SlingContext context, String rulesPath, Log log) {
    this.rules = new Rules(context.resourceResolver().getResource(rulesPath));
    this.sourceRoot = context.resourceResolver().getResource("/source");
    this.log = log;
  }

  /**
   * Convert and format all JSON files with dialog definitions.
   */
  public void convert() {
    convertDialogs(sourceRoot, true);
  }

  /**
   * Format (but not convert) all JSON files with dialog definitions.
   */
  public void format() {
    convertDialogs(sourceRoot, false);
  }

  private void convertDialogs(Resource resource, boolean convert) {
    if (StringUtils.equals(resource.getName(), "cq:dialog")
        || StringUtils.equals(resource.getName(), "cq:design_dialog")) {
      convertDialogResource(resource, convert);
    }
    else {
      Iterator<Resource> children = resource.listChildren();
      while (children.hasNext()) {
        convertDialogs(children.next(), convert);
      }
    }
  }

  private void convertDialogResource(Resource resource, boolean convert) {
    Rule rule = rules.getRule(resource);
    if (rule != null) {
      log.info("Convert " + StringUtils.removeStart(resource.getPath(), "/source/") + " with rule '" + rule.getName() + "'.");

      ContentFile contentFile = resource.adaptTo(ContentFile.class);
      try {
        JSONObject jsonContent = new JSONObject(FileUtils.readFileToString(contentFile.getFile()));
        JsonElement wrapper = getJsonElement(jsonContent, contentFile.getSubPath());

        if (convert) {
          applyRule(wrapper, rule);
        }

        // format json string with gson pretty print
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject gsonJson = gson.fromJson(jsonContent.toString(), JsonObject.class);

        FileUtils.write(contentFile.getFile(), gson.toJson(gsonJson));
      }
      catch (JSONException | IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    Iterator<Resource> children = resource.listChildren();
    while (children.hasNext()) {
      convertDialogResource(children.next(), convert);
    }
  }

  private JsonElement getJsonElement(JSONObject json, String path) throws JSONException {
    if (StringUtils.isEmpty(path)) {
      return new JsonElement(json, null, null);
    }
    if (StringUtils.contains(path, "/")) {
      String name = StringUtils.substringBefore(path, "/");
      String remainder = StringUtils.substringAfter(path, "/");
      JSONObject child = json.getJSONObject(name);
      return getJsonElement(child, remainder);
    }
    else {
      return new JsonElement(json.getJSONObject(path), path, json);
    }
  }

  private void applyRule(JsonElement wrapper, Rule rule) throws JSONException {
    // check if the 'replacement' node exists
    Resource replacement = rule.getReplacement();
    if (replacement == null) {
      throw new RuntimeException("The rule " + rule + " does not define a 'replacement' section.");
    }
    ValueMap replacementProps = replacement.getValueMap();

    // if the replacement node has no children, we replace the tree by the empty tree,
    // i.e. we remove the original tree
    if (!replacement.hasChildren()) {
      wrapper.parent.remove(wrapper.key);
      return;
    }
    JSONObject root = cloneJson(wrapper.element);

    // true if the replacement tree is final and all its nodes are excluded from
    // further processing by the algorithm
    boolean treeIsFinal = replacementProps.get(PROPERTY_IS_FINAL, false);

    // copy replacement to original tree under original name
    Resource replacementNext = replacement.listChildren().next();
    JSONObject copy = wrapper.element;
    clearJson(copy);
    copyToJson(copy, replacementNext);

    // common attribute mapping
    if (replacementProps.containsKey(PROPERTY_COMMON_ATTRS)) {
      addCommonAttrMappings(root, copy);
    }

    // render condition mapping
    if (replacementProps.containsKey(PROPERTY_RENDER_CONDITION)) {
      if (root.has(NN_GRANITE_RENDER_CONDITION) || root.has(NN_RENDER_CONDITION)) {
        JSONObject renderConditionRoot = root.has(NN_GRANITE_RENDER_CONDITION) ? root.getJSONObject(NN_GRANITE_RENDER_CONDITION)
            : root.getJSONObject(NN_RENDER_CONDITION);
        JSONObject renderConditionCopy = copy.put(NN_GRANITE_RENDER_CONDITION, renderConditionRoot);

        // convert render condition resource types recursively
        Iterator<JSONObject> renderConditionIterator = collectTree(renderConditionCopy).iterator();

        while (renderConditionIterator.hasNext()) {
          JSONObject renderConditionNode = renderConditionIterator.next();
          String resourceType = renderConditionNode.getString(ResourceResolver.PROPERTY_RESOURCE_TYPE);
          if (resourceType.startsWith(RENDER_CONDITION_CORAL2_RESOURCE_TYPE_PREFIX)) {
            resourceType = resourceType.replace(RENDER_CONDITION_CORAL2_RESOURCE_TYPE_PREFIX, RENDER_CONDITION_CORAL3_RESOURCE_TYPE_PREFIX);
            renderConditionNode.put(ResourceResolver.PROPERTY_RESOURCE_TYPE, resourceType);
          }
        }
      }
    }

    // collect mappings: (node in original tree) -> (node in replacement tree)
    Map<String, JSONObject> mappings = new LinkedHashMap<>();
    // traverse nodes of newly copied replacement tree
    Iterator<JSONObject> nodeIterator = collectTree(copy).iterator();
    while (nodeIterator.hasNext()) {
      JSONObject node = nodeIterator.next();
      // iterate over all properties
      Map<String, Object> replacementProperties = getProperties(node);
      Iterator<Map.Entry<String, Object>> propertyIterator = replacementProperties.entrySet().iterator();
      JSONObject rewritePropertiesNode = null;

      if (node.has(NN_CQ_REWRITE_PROPERTIES)) {
        rewritePropertiesNode = node.getJSONObject(NN_CQ_REWRITE_PROPERTIES);
      }

      while (propertyIterator.hasNext()) {
        Map.Entry<String, Object> property = propertyIterator.next();
        // add mapping to collection
        if (PROPERTY_MAP_CHILDREN.equals(property.getKey())) {
          mappings.put(cleanup((String)property.getValue()), node);
          // remove property, as we don't want it to be part of the result
          node.remove(cleanup(property.getKey()));
          continue;
        }
        // add single node to final nodes
        if (PROPERTY_IS_FINAL.equals(property.getKey())) {
          if (!treeIsFinal) {
            // TODO: required?
            //finalNodes.add(node);
          }
          node.remove(cleanup(property.getKey()));
          continue;
        }
        // set value from original tree in case this is a mapped property
        boolean mappedProperty = mapProperty(root, node, property.getKey(), (String)property.getValue());

        if (mappedProperty && rewritePropertiesNode != null) {
          if (rewritePropertiesNode.has(cleanup(property.getKey()))) {
            rewriteProperty(node, cleanup(property.getKey()), rewritePropertiesNode.getJSONArray(cleanup(property.getKey())));
          }
        }
      }

      // remove <cq:rewriteProperties> node post-mapping
      if (rewritePropertiesNode != null) {
        node.remove(NN_CQ_REWRITE_PROPERTIES);
      }

      // reorder children and properties
      reorderChildrenProperties(node, root);
    }

    // copy children from original tree to replacement tree according to the mappings found
    for (Map.Entry<String, JSONObject> mapping : mappings.entrySet()) {
      String key = cleanup(mapping.getKey());
      if (!root.has(cleanup(key))) {
        // the node specified in the mapping does not exist in the original tree
        continue;
      }
      JSONObject source = root.getJSONObject(cleanup(key));
      JSONObject destination = mapping.getValue();
      Iterator<Map.Entry<String,JSONObject>> iterator = getChildren(source).entrySet().iterator();
      // copy over the source's children to the destination
      while (iterator.hasNext()) {
        Map.Entry<String,JSONObject> child = iterator.next();
        destination.put(cleanup(child.getKey()), child.getValue());
      }
    }

    // TODO: not required?
    /*
    // we add the complete subtree to the final nodes
    if (treeIsFinal) {
      nodeIterator = collectTree(copy).iterator();
      while (nodeIterator.hasNext()) {
        finalNodes.add(nodeIterator.next());
      }
    }
    */
  }

  /**
   * Replaces the value of a mapped property with a value from the original tree.
   * @param root the root node of the original tree
   * @param node the replacement tree object
   * @param key property name of the (potentially) mapped property in the replacement copy tree
   * @return true if there was a successful mapping, false otherwise
   * @throws JSONException
   */
  private boolean mapProperty(JSONObject root, JSONObject node, String key, String... mapping) throws JSONException {
    boolean deleteProperty = false;
    for (String value : mapping) {
      Matcher matcher = MAPPED_PATTERN.matcher(value);
      if (matcher.matches()) {
        // this is a mapped property, we will delete it if the mapped destination
        // property doesn't exist
        deleteProperty = true;
        String path = matcher.group(2);
        // unwrap quoted property paths
        path = StringUtils.removeStart(StringUtils.stripEnd(path, "\'"), "\'");
        if (root.has(cleanup(path))) {
          // replace property by mapped value in the original tree
          Object originalValue = root.get(cleanup(path));
          node.put(cleanup(key), originalValue);

          // negate boolean properties if negation character has been set
          String negate = matcher.group(1);
          if ("!".equals(negate) && (originalValue instanceof Boolean)) {
            node.put(cleanup(key), !((Boolean)originalValue));
          }

          // the mapping was successful
          deleteProperty = false;
          break;
        }
        else {
          String defaultValue = matcher.group(4);
          if (defaultValue != null) {
            node.put(cleanup(key), defaultValue);
            deleteProperty = false;
            break;
          }
        }
      }
    }
    if (deleteProperty) {
      // mapped destination does not exist, we don't include the property in replacement tree
      node.remove(key);
      return false;
    }

    return true;
  }

  /**
   * Applies a string rewrite to a property.
   * @param node Node
   * @param key the property name to rewrite
   * @param rewriteProperty the property that defines the string rewrite
   * @throws JSONException
   */
  private void rewriteProperty(JSONObject node, String key, JSONArray rewriteProperty) throws JSONException {
    if (node.get(cleanup(key)) instanceof String) {
      if (rewriteProperty.length() == 2) {
        if (rewriteProperty.get(0) instanceof String && rewriteProperty.get(1) instanceof String) {
          String pattern = rewriteProperty.getString(0);
          String replacement = rewriteProperty.getString(1);

          Pattern compiledPattern = Pattern.compile(pattern);
          Matcher matcher = compiledPattern.matcher(node.getString(cleanup(key)));
          node.put(cleanup(key), matcher.replaceAll(replacement));
        }
      }
    }
  }

  /**
   * Adds property mappings on a replacement node for Granite common attributes.
   * @param root the root node
   * @param node the replacement node
   * @throws JSONException
   */
  private void addCommonAttrMappings(JSONObject root, JSONObject node) throws JSONException {
    for (String property : GRANITE_COMMON_ATTR_PROPERTIES) {
      String[] mapping = { "${./" + property + "}", "${\'./granite:" + property + "\'}" };
      mapProperty(root, node, "granite:" + property, mapping);
    }

    if (root.has(NN_GRANITE_DATA)) {
      // the root has granite:data defined, copy it before applying data-* properties
      node.put(NN_GRANITE_DATA, root.get(NN_GRANITE_DATA));
    }

    // map data-* prefixed properties to granite:data child
    for (Map.Entry<String, Object> entry : getProperties(root).entrySet()) {
      if (!StringUtils.startsWith(entry.getKey(), DATA_PREFIX)) {
        continue;
      }

      // add the granite:data child if necessary
      JSONObject dataNode;
      if (!node.has(NN_GRANITE_DATA)) {
        dataNode = new JSONObject();
        node.put(NN_GRANITE_DATA, dataNode);
      }
      else {
        dataNode = node.getJSONObject(NN_GRANITE_DATA);
      }

      // set up the property mapping
      String nameWithoutPrefix = entry.getKey().substring(DATA_PREFIX.length());
      mapProperty(root, dataNode, nameWithoutPrefix, "${./" + entry.getKey() + "}");
    }
  }

  private JSONObject cloneJson(JSONObject item) throws JSONException {
    JSONObject newItem = new JSONObject();

    Set<Map.Entry<String, Object>> props = getProperties(item).entrySet();
    for (Map.Entry<String, Object> prop : props) {
      newItem.put(prop.getKey(), prop.getValue());
    }

    Set<Map.Entry<String, JSONObject>> children = getChildren(item).entrySet();
    for (Map.Entry<String, JSONObject> child : children) {
      newItem.put(child.getKey(), cloneJson(child.getValue()));
    }

    return newItem;
  }

  private void clearJson(JSONObject item) throws JSONException {
    Set<Map.Entry<String, Object>> props = getProperties(item).entrySet();
    Set<Map.Entry<String, JSONObject>> children = getChildren(item).entrySet();
    for (Map.Entry<String, Object> prop : props) {
      item.remove(prop.getKey());
    }
    for (Map.Entry<String, JSONObject> child : children) {
      item.remove(child.getKey());
    }
  }

  private void copyToJson(JSONObject dest, Resource resource) throws JSONException {
    for (Map.Entry<String, Object> entry : resource.getValueMap().entrySet()) {
      if (StringUtils.equals(cleanup(entry.getKey()), "jcr:primaryType")) {
        continue;
      }
      dest.put(cleanup(entry.getKey()), entry.getValue());
    }

    Iterator<Resource> children = resource.listChildren();
    while (children.hasNext()) {
      Resource child = children.next();
      JSONObject childObject = new JSONObject();
      copyToJson(childObject, child);
      dest.put(child.getName(), childObject);
    }
  }

  private List<JSONObject> collectTree(JSONObject item) throws JSONException {
    List<JSONObject> items = new ArrayList<>();
    items.add(item);
    for (JSONObject child : getChildren(item).values()) {
      items.addAll(collectTree(child));
    }
    return items;
  }

  private Map<String, Object> getProperties(JSONObject item) throws JSONException {
    Map<String, Object> props = new LinkedHashMap<>();
    JSONArray names = item.names();
    if (names != null) {
      for (int i = 0; i < names.length(); i++) {
        String name = names.getString(i);
        Object value = item.get(name);
        if (!(value instanceof JSONObject)) {
          props.put(name, value);
        }
      }
    }
    return props;
  }

  private Map<String, JSONObject> getChildren(JSONObject item) throws JSONException {
    Map<String, JSONObject> children = new LinkedHashMap<>();
    JSONArray names = item.names();
    if (names != null) {
      for (int i = 0; i < names.length(); i++) {
        String name = names.getString(i);
        Object value = item.get(name);
        if (value instanceof JSONObject) {
          children.put(name, (JSONObject)value);
        }
      }
    }
    return children;
  }

  private String cleanup(String name) {
    return StringUtils.removeStart(name, "./");
  }

  private void reorderChildrenProperties(JSONObject item, JSONObject orginal) throws JSONException {
    Map<String, Object> props = new TreeMap<String, Object>(getProperties(item));
    Map<String, JSONObject> children = getChildren(item);
    for (String key : props.keySet()) {
      item.remove(key);
    }
    for (String key : children.keySet()) {
      item.remove(key);
    }
    // put all properties and items that where already present in original node back in same order
    for (String key : getProperties(orginal).keySet()) {
      if (props.containsKey(key)) {
        item.put(key, props.get(key));
        props.remove(key);
      }
    }
    // put all other props in alphabetical order
    for (Map.Entry<String, Object> entry : props.entrySet()) {
      item.put(entry.getKey(), entry.getValue());
    }
    // put all children that where already present in original node back in same order
    for (String key : getProperties(orginal).keySet()) {
      if (children.containsKey(key)) {
        item.put(key, children.get(key));
        children.remove(key);
      }
    }
    // put all other children in alphabetical order
    for (Map.Entry<String, JSONObject> entry : children.entrySet()) {
      item.put(entry.getKey(), entry.getValue());
    }
  }


  private static class JsonElement {

    private final JSONObject element;
    private final String key;
    private final JSONObject parent;

    JsonElement(JSONObject element, String key, JSONObject parent) {
      this.element = element;
      this.key = key;
      this.parent = parent;
    }

  }

}
