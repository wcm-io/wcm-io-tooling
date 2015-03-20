About Node.js Maven Plugin
==========================

Maven Plugin to wrap the Node.js execution.


### Maven Dependency

```xml
<plugin>
  <groupId>io.wcm.maven.plugins</groupId>
  <artifactId>nodejs-maven-plugin</artifactId>
  <version>1.0.6</version>
</plugin>
```

### Documentation

* [Usage][usage]
* [Plugin Documentation][plugindocs]
* [Changelog][changelog]


### Supported Platforms

The plugin currently supports following platforms:

* Windows (32 and 64 bit)
* Mac OS (32 and 64 bit)
* Linux (i386 and amd64)

The windows distribution of nodejs does not contain the npm executables. Therefore npm is installed seperateley by the plugin on windows.


### Acknowledgements

This plugin is derived from https://github.com/ClearboxSystems/NodeJsMaven.



[usage]: usage.html
[plugindocs]: plugin-info.html
[changelog]: changes-report.html
