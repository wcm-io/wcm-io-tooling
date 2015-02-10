About Content Package Maven Plugin
==================================

Install and download content packages via CRX package manager.


### Maven Dependency

```xml
<plugin>
  <groupId>io.wcm.maven.plugins</groupId>
  <artifactId>wcmio-content-package-maven-plugin</artifactId>
  <version>1.0.0</version>
</plugin>
```

### Documentation

* [Usage][usage]
* [Plugin Documentation][plugindocs]
* [Changelog][changelog]


### Comparison to Adobe Content Package Maven Plugin

Adobe has published it's own [Content Package Maven Plugin][adobe-content-package-maven-plugin]. It supports
more goals and has more features than the `wcmio-content-package-maven-plugin`.

But there are some features missing, and this is where `wcmio-content-package-maven-plugin` steps in.
To make it easy switching between both plugins most properties have the same names and default values.
Both plugins can co-exist nicely.


#### `install` goal

Additional features of the `install` goal:

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

Additional the `download` goal supports:

* Unpacking the content package after download.
* Exclude files, nodes and properties from the unpacked content via pattern lists.


[usage]: usage.html
[plugindocs]: plugin-info.html
[changelog]: changes-report.html
[adobe-content-package-maven-plugin]: http://repo.adobe.com/nexus/content/repositories/releases/com/day/jcr/vault/content-package-maven-plugin/
