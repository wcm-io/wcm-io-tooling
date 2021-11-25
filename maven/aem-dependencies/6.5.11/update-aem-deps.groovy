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

LOCAL_AEM_URL = 'http://localhost:45025'
LOCAL_AEM_USER = 'admin'
LOCAL_AEM_PASSWORD = 'admin'

//----------------------------------------------------------------------

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

HINT_PATTERN = /\s*update-aem-deps:([^\s]*)\s*/

log = LoggerFactory.getLogger(this.class)

// get bundles version running in local AEM instance
log.info 'Reading bundle versions...'
def bundleVersions = [:]
def bundlePackageVersions = [:]
readBundleVersions(bundleVersions, bundlePackageVersions)

// read process pom using JDOM to preserve formatting and whitespaces
POM_NS = Namespace.getNamespace('ns', 'http://maven.apache.org/POM/4.0.0')
Document doc = new SAXBuilder().build(new FileReader('pom.xml'))

// update some well-known properties based on specific bundle versions
log.info 'Update properties...'
pomUpdateProperties(doc, bundleVersions)

// update all dependencies matching the bundles in local POM
log.info 'Update dependencies...'
pomUpdateDependencies(doc, bundleVersions, bundlePackageVersions)

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
  def url = LOCAL_AEM_URL + relativeUrl
  try {
    return HttpBuilder.configure {
      request.uri = url
      request.auth.basic LOCAL_AEM_USER, LOCAL_AEM_PASSWORD
    }.get()
  }
  catch (Exception ex) {
    throw new RuntimeException("Unable to access " + url, ex)
  }
}

// read versions of all maven artifacts from bundle list
def readBundleVersions(bundleVersions, bundlePackageVersions) {
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

    // read exported package versions
    def exportedPackages = bundleDetails.data[0].props.findResult { it.key == 'Exported Packages' ? it : null }
    def packageVersions = [:]
    if (exportedPackages && exportedPackages.value) {
      exportedPackages.value.each {
        def exportedPackage = "${it}";
        def packageVersionPattern = /(.*),version=(.*)/;
        def matcher = (exportedPackage =~ packageVersionPattern)
        if (matcher.matches()) {
          packageVersions[matcher.group(1)] = matcher.group(2)
        }
      }
    }
    bundlePackageVersions[it.symbolicName] = packageVersions

  }
}

// set pom version
def pomSetAemSdkVersion(doc, aemSdkVersion) {
  def versions = XPathFactory.instance().compile('/ns:project/ns:version', Filters.element(), null, POM_NS).evaluate(doc)
  for (def version in versions) {
    version.text = "${aemSdkVersion}.0000-SNAPSHOT"
  } 
}

// update property in POM to match with a specific bundle version
def pomUpdateProperties(doc, bundleVersions) {
  def props = XPathFactory.instance().compile('/ns:project/ns:properties/*', Filters.element(), null, POM_NS).evaluate(doc)
  for (def prop in props) {
    // check if previous sibling is a comment not with a dependency hint
    def elementIndex = prop.parent.getContent().indexOf(prop)
    def previousSibling = null
    while (elementIndex > 0) {
      previousSibling = prop.parent.getContent().get(--elementIndex)
      if (previousSibling instanceof Element || previousSibling instanceof Comment) {
        break
      }
      else {
        previousSibling = null
      }
    }
    if (previousSibling instanceof Comment && previousSibling.text =~ HINT_PATTERN) {
      def hint = (previousSibling.text =~ HINT_PATTERN)[0][1]
      // check for dervied-from hint
      def derivedFrom = getDerivedFromHint(hint)
      if (derivedFrom) {
        def actualVersion = bundleVersions[derivedFrom.bundleName]
        if (derivedFrom.version != actualVersion) {
          if (actualVersion) {
            log.warn "property ${prop.name} is derived from ${derivedFrom.bundleName}:${derivedFrom.version}, but that bundle has currently version ${actualVersion}, check manually"
          }
          else {
            log.warn "property ${prop.name} is derived from ${derivedFrom.bundleName}:${derivedFrom.version}, but that bundle is not present, check manually"
          }
        }
        continue
      }
      def bundleName = getBundleNameFromHint(hint)
      if (bundleName) {
        def version = bundleVersions[bundleName]
        assert version != null : 'Version of bundle ' + bundleName + ' not found'
        prop.text = version
      }
    }
  }
}

// updates all dependencies to their latest versions
def pomUpdateDependencies(doc, bundleVersions, bundlePackageVersions) {
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
      def bundlePackage = getBundlePackageFromHint(hint)
      if (bundlePackage) {
        version = bundlePackageVersions[bundlePackage.bundleName][bundlePackage.packageName]
      }
      else {
        // check for bundle = artifactId
        version = bundleVersions[artifactId]
        if (!version) {
          // alternatively check combination for groupId and artifactId
          version = bundleVersions["${groupId}.${artifactId}"]
        }
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
  return dep.getContent().findResult { (it instanceof Comment) && it.text =~ HINT_PATTERN ? (it.text =~ HINT_PATTERN)[0][1] : null }
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

def getBundlePackageFromHint(hint) {
  def bundlePackagePattern = /bundle-package=(.*):(.*)/;
  def matcher = (hint =~ bundlePackagePattern)
  if (matcher.matches()) {
    return [ bundleName: matcher.group(1), packageName: matcher.group(2) ]
  }
  else {
    return null
  }
}
