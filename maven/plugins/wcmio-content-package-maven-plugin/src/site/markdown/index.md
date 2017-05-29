About Content Package Maven Plugin
==================================

Build, upload and download content packages.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.maven.plugins/wcmio-content-package-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.maven.plugins/wcmio-content-package-maven-plugin)


### Documentation

* [Usage][usage]
* [Plugin Documentation][plugindocs]
* [Changelog][changelog]


### Overview

This Maven Plugin is an alternative to the [Content Package Maven Plugin][adobe-content-package-maven-plugin] from Adobe.

It supports:

* Building content packages
* Uploading and installing content packages via CRX package manager or Sling Launchpad with [Composum console][composum]
* Downloading and extracting content packages via CRX package manager or Sling Launchpad with [Composum console][composum]

The wcm.io Content Package Maven plugin aims for compatibility in goal and property names and behavior with the Adobe plugin, so for a lot of use cases it can be used as drop-in replacement. However some goals and features are missing. See [this wiki page](https://wcm-io.atlassian.net/wiki/x/-HkIAw) for a comparison.


#### `package` goal

The behavior of the `package` goal is nearly identical to that of the Adobe plugin.


#### `install` goal

Additional features of the `install` goal compared to the Adobe plugin:

* `force` property - if set to false a package is not uploaded or installed if it was already
uploaded before.
* `recursive` property - if set to true nested packages get installed as well.
* `packageFiles` property - allows installing multiple package files referenced from local filesystem
or from maven repository at once.
* `bundleStatusURL` - allows to check for bundle activation status before installing a package to avoid failed package deployments if the previous packages contained OSGi bundles that need some time to get installed properly.

If you only want to manage your `content-package` Maven project to install the content stored in the build
artifact of the current project you can use the Adobe plugin.


#### `download` goal

The Adobe plugin has several limitations defining which package to download via the download goal. Basically
it works only if only one version of the package is installed, and when the path to this package is specified
directly and not via the artifact properties.

The `wcmio-content-package-maven-plugin` takes another strategy to download a package that was uploaded
before. It first starts an upload of the package (without installing it). If it already exists CRX responds
with the path to this package - this path is used to build the package and download it. Thus the latest
package filter options from the local POM are used as well. This strategy is targeted only on
`content-package` Maven projects, not for downloading arbitrary packages.

Additional the `download` goal supports compared to the Adobe plugin:

* Unpacking the content package after download.
* Exclude files, nodes and properties from the unpacked content via pattern lists.


[usage]: usage.html
[plugindocs]: plugin-info.html
[changelog]: changes-report.html
[adobe-content-package-maven-plugin]: https://docs.adobe.com/docs/en/aem/6-3/develop/dev-tools/vlt-mavenplugin.html
[composum]: https://github.com/ist-dresden/composum
