About Node.js Maven Plugin
==========================

Maven Plugin to wrap the Node.js execution.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.maven.plugins/nodejs-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.maven.plugins/nodejs-maven-plugin)


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


### Node.js binary download

The plugin needs to download Node.js binaries which are not available in Maven Central. To support managing the download and caching of these artefacts they are wrapped as Maven Artifacts using the [Maven NodeJS Proxy][maven-nodejs-proxy].

A public version of this proxy is running at [https://maven-nodejs-proxy.pvtool.org/](https://maven-nodejs-proxy.pvtool.org/). You have to include this URL in your Maven Artifact Manager our run your own instance of Maven NodeJS Proxy.


### Acknowledgements

This plugin is derived from https://github.com/ClearboxSystems/NodeJsMaven.


### Alternatives

The most prominent alternative to this plugin is the [frontend-maven-plugin][frontend-maven-plugin]. These are the most notable differences:

* The `frontend-maven-plugin` defines goals for specific tools like grunt, gulp, karma, whereas the `nodejs-maven-plugin` has just one generic `run` goals that lets you execute any Node.js-based tool.
* The `nodejs-maven-plugin` automatically downloads and installs the required NPM version on your local system, using the `frontend-maven-plugin` plugin you need a separate goal for this.
* The `nodejs-maven-plugin` supports downloading the NPM binaries via maven dependencies ([Maven NodeJS Proxy][maven-nodejs-proxy]) - this allows caching the binaries the same way all other maven binaries are cached, and also helps if all internet communication is locked down on your build systems despite the artifact manager.




[usage]: usage.html
[plugindocs]: plugin-info.html
[changelog]: changes-report.html
[maven-nodejs-proxy]: https://github.com/wcm-io-devops/maven-nodejs-proxy
[frontend-maven-plugin]: https://github.com/eirslett/frontend-maven-plugin
