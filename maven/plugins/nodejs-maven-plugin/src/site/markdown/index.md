About Node.js Maven Plugin
==========================

Maven Plugin to wrap the Node.js execution.


### Maven Dependency

```xml
<plugin>
  <groupId>io.wcm.maven.plugins</groupId>
  <artifactId>nodejs-maven-plugin</artifactId>
  <version>2.0.0</version>
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


### NodeJS binary download

The plugin needs to download NodeJS binaries which are not available in Maven Central. To support managing the download and caching of these artefacts they are wrapped as Maven Artifacts using the [Maven NodeJS Proxy][maven-nodejs-proxy].

A public version of this proxy is running at [https://maven-nodejs-proxy.pvtool.org/](https://maven-nodejs-proxy.pvtool.org/). You have to include this URL in your Maven Artifact Manager our run your own instance of Maven NodeJS Proxy.


### Acknowledgements

This plugin is derived from https://github.com/ClearboxSystems/NodeJsMaven.



[usage]: usage.html
[plugindocs]: plugin-info.html
[changelog]: changes-report.html
[maven-nodejs-proxy]: https://github.com/wcm-io-devops/maven-nodejs-proxy
