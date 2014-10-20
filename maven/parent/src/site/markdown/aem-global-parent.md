## AEM Global Parent

Global parent for Maven artifact hierarchy for AEM projects. Defines AEM-specific plugins and general Maven settings.


### Maven Dependency

```xml
<dependency>
  <groupId>io.wcm</groupId>
  <artifactId>io.wcm.maven.aem-global-parent</artifactId>
  <version>1.0.2-SNAPSHOT</version>
</dependency>
```

### Overview

The settings in this AEM global parent POM cover:

* Dependencies for SCR and OSGi annotations
* Resource includes for src/main/webapp folder
* Default configurations for maven-bundle-plugin, maven-sling-plugin, content-package-maven-plugin
* Include wcm.io maven plugins [wcmio-content-package-maven-plugin](plugins/wcmio-content-package-maven-plugin/),
  [cq-maven-plugin](plugins/cq-maven-plugin/), [nodejs-maven-plugin](plugins/nodejs-maven-plugin/)
* Define default exclusions for m2e lifecycle
