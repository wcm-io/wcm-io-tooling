## Content Package Builder

Java Library for building AEM Content Packages with content pages and binary files.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.tooling.commons/io.wcm.tooling.commons.content-package-builder/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.tooling.commons/io.wcm.tooling.commons.content-package-builder)

### Documentation

* [Usage](usage.html)
* [API documentation](apidocs/)
* [Changelog](changes-report.html)


### Overview

This library is a barebone implementation to create AEM content packages.

AEM Pages can be created from a Map containing the page content. Nested maps make up a hiearchy of nodes.

Additionally binary files can be added.

The result is a ZIP package that can be imported via CRX Package Manager in a AEM instance.
