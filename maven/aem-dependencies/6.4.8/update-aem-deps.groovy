/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/***********************************************************************
 * 
 * PARAMETERS
 *
 ***********************************************************************/

LOCAL_AEM_URL = 'http://localhost:45024'
LOCAL_AEM_USER = 'admin'
LOCAL_AEM_PASSWORD = 'admin'

//----------------------------------------------------------------------

@GrabResolver(name='adobe-public-releases', root='https://repo.adobe.com/nexus/content/groups/public')

@Grab('org.slf4j:slf4j-simple:1.7.30')
@Grab('io.github.http-builder-ng:http-builder-ng-core:1.0.4')
@Grab('jaxen:jaxen:1.1.6')
@GrabExclude('jdom:jdom')
@Grab('org.jdom:jdom2:2.0.6')

import org.jdom2.*
import org.jdom2.filter.*
import org.jdom2.input.*
import org.jdom2.xpath.*
import org.jdom2.output.*
import groovyx.net.http.HttpBuilder
import groovy.json.JsonSlurper
import groovy.grape.Grape
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

log = LoggerFactory.getLogger(this.class)

// get bundles version running in local AEM instance
log.info 'Reading bundle versions...'
def bundleVersions = [:]
readBundleVersions(bundleVersions)

// read process pom using JDOM to preserve formatting and whitespaces
POM_NS = Namespace.getNamespace('ns', 'http://maven.apache.org/POM/4.0.0')
Document doc = new SAXBuilder().build(new FileReader('pom.xml'))

// update some well-known properties based on specific bundle versions
log.info 'Update properties...'
pomUpdateProperty(doc, bundleVersions, 'slf4j.version', 'slf4j.api')
pomUpdateProperty(doc, bundleVersions, 'jackrabbit.version', 'org.apache.jackrabbit.jackrabbit-jcr-commons')
pomUpdateProperty(doc, bundleVersions, 'oak.version', 'org.apache.jackrabbit.oak-core')

// update all dependencies matching the bundles in local POM
log.info 'Update dependencies...'
pomUpdateDependencies(doc, bundleVersions)

// validate all dependencies
log.info 'Validate dependencies...'
pomValidateDependencies(doc)

// write back pom content
new XMLOutputter().with {
  format = Format.getRawFormat()
  format.setLineSeparator(LineSeparator.NONE)
  def os = new File('pom.xml').newOutputStream()
  output(doc, os)
  os.close()
}

// --- functions ---

// read URL from locale AEM instance
def readAemUrl(relativeUrl) {
  return HttpBuilder.configure {
    request.uri = LOCAL_AEM_URL + relativeUrl
    request.auth.basic LOCAL_AEM_USER, LOCAL_AEM_PASSWORD
  }.get()
}

// read versions of all maven artifacts from bundle list
def readBundleVersions(bundleVersions) {
  def bundleList = readAemUrl('/system/console/bundles.json')
  bundleList.data.each {
    def version = it.version
    // try to resolve the implementation version from bundle header as it may differ from the bundle version
    def bundleDetails = readAemUrl("/system/console/bundles/${it.id}.json")
    def manifestHeader = bundleDetails.data[0].props.findResult { it.key == 'Manifest Headers' ? it : null }
    if (manifestHeader) {
      def implementationVersion = manifestHeader.value.findResult { it =~ /^Implementation-Version: (.*)$/ ? (it =~ /^Implementation-Version: (.*)$/)[0][1] : null }
      if (implementationVersion) {
        version = implementationVersion
      }
    }
    bundleVersions[it.symbolicName] = version;
  }
}

// update property in POM to match with a specific bundle version
def pomUpdateProperty(doc, bundleVersions, propertyName, bundleName) {
  def props = XPathFactory.instance().compile('/ns:project/ns:properties/ns:' + propertyName, Filters.element(), null, POM_NS).evaluate(doc)
  for (def prop in props) {
    def version = bundleVersions[bundleName]
    assert version != null : 'Version of bundle ' + bundleName + ' not found'
    prop.text = version
  } 
}

// updates all dependencies to their latest versions
def pomUpdateDependencies(doc, bundleVersions) {
  def deps = XPathFactory.instance().compile('/ns:project/ns:dependencyManagement/ns:dependencies/ns:dependency', Filters.element(), null, POM_NS).evaluate(doc)
  for (def dep in deps) {
    def groupId = dep.getChild('groupId', POM_NS).text
    def artifactId = dep.getChild('artifactId', POM_NS).text
    def versionElement = dep.getChild('version', POM_NS)

    // check for update hint comment
    def hint = getDependencyHint(dep)
    if (hint == "ignore") {
      continue
    }

    // check for dervied-from hint
    def derivedFrom = getDerivedFromHint(hint)
    if (derivedFrom) {
      def actualVersion = bundleVersions[derivedFrom.bundleName]
      if (derivedFrom.version != actualVersion) {
        if (actualVersion) {
          log.warn "${groupId}:${artifactId} is derived from ${derivedFrom.bundleName}:${derivedFrom.version}, but that bundle has currently version ${actualVersion}, check manually"
        }
        else {
          log.warn "${groupId}:${artifactId} is derived from ${derivedFrom.bundleName}:${derivedFrom.version}, but that bundle is not present, check manually"
        }
      }
      continue
    }

    // check if the version references a maven property - skip this dependencies, properties are update separately
    def existingVersion = versionElement.text
    if (existingVersion =~ /\$\{.*\}/) {
      continue
    }

    // try to resolve latest version from bundle list
    def version = null
    def bundleName = getBundleNameFromHint(hint)
    if (bundleName) {
      version = bundleVersions[bundleName]
    }
    else {
      // check for bundle = artifactId
      version = bundleVersions[artifactId]
      if (!version) {
        // alternatively check combination for groupId and artifactId
        version = bundleVersions["${groupId}.${artifactId}"]
      }
    }
    if (version) {
      versionElement.text = version
    }
    else {
      log.warn "No matching bundle: ${groupId}:${artifactId}"
    }
  }
}

// validate all dependencies to make sure they can be resolved in public repositories
def pomValidateDependencies(doc) {
  def deps = XPathFactory.instance().compile('/ns:project/ns:dependencyManagement/ns:dependencies/ns:dependency', Filters.element(), null, POM_NS).evaluate(doc)
  for (def dep in deps) {
    def groupId = dep.getChild('groupId', POM_NS).text
    def artifactId = dep.getChild('artifactId', POM_NS).text
    def version = dep.getChild('version', POM_NS).text

    // if version is a property try to resolve it in POM
    def propertyNameMatcher = (version =~ /\$\{(.*)\}/)
    if (propertyNameMatcher) {
      def propertyName = propertyNameMatcher[0][1]
      def props = XPathFactory.instance().compile('/ns:project/ns:properties/ns:' + propertyName, Filters.element(), null, POM_NS).evaluate(doc)
      for (def prop in props) {
        version = prop.text
      } 
    }
    
    // try to resolve dependency
    try {
      Grape.grab(group: groupId, module: artifactId, version: version)
    }
    catch (Exception ex) {
      if (ex.message =~ /download failed: javax.mail#javax.mail-api;1.6.2!javax.mail-api.jar/
          || ex.message =~ /download failed: org.apache.poi#poi-ooxml-schemas;4.0.1!poi-ooxml-schemas.jar/
          || ex.message =~ /download failed: org.apache.xmlgraphics#fop;1.0!fop.jar/) {
        // ignore: dependencies cannot be downloaded by grape/ivy - unsure why
      }
      else {
        log.error ex.message
      }
    }
  }
}

// check for update-aem-deps hint in comment
def getDependencyHint(dep) {
  def hintPattern = /\s*update-aem-deps:([^\s]*)\s*/
  return dep.getContent().findResult { (it instanceof Comment) && it.text =~ hintPattern ? (it.text =~ hintPattern)[0][1] : null }
}

def getBundleNameFromHint(hint) {
  def bundleNamePattern = /bundle=(.*)/;
  def matcher = (hint =~ bundleNamePattern)
  if (matcher.matches()) {
    return matcher.group(1)
  }
  else {
    return null
  }
}

def getDerivedFromHint(hint) {
  def derivedFromPattern = /derived-from=(.*):(.*)/;
  def matcher = (hint =~ derivedFromPattern)
  if (matcher.matches()) {
    return [ bundleName: matcher.group(1), version: matcher.group(2) ]
  }
  else {
    return null
  }
}
