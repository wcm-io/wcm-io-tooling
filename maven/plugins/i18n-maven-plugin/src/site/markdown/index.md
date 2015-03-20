About i18n Maven Plugin
=======================

Transforms and validates i18n resources for usage in Sling/AEM applications.


### Maven Dependency

```xml
<plugin>
  <groupId>io.wcm.maven.plugins</groupId>
  <artifactId>i18n-maven-plugin</artifactId>
  <version>1.0.0</version>
</plugin>
```

### Documentation

* [Usage][usage]
* [Plugin Documentation][plugindocs]
* [Changelog][changelog]


### Overview

It is very tedious to write sling i18n resource manually in this very verbose format<br/>
http://sling.apache.org/site/internationalization-support.html#InternationalizationSupport-SampleResources

This plugin allows to write the i18n resources in a very simple and compact format like:

* Java Properties file
* JSON file (allows nesting for building key hierarchies)
* XML file (allows nesting for building key hierarchies)

All those input files are transformed during maven build to the sling destination format in either JCR content JSON or XML format.

See [Usage][usage] for details.


[usage]: usage.html
[plugindocs]: plugin-info.html
[changelog]: changes-report.html
