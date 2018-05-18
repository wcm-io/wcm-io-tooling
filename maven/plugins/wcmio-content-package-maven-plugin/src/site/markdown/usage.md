Content Package Maven Plugin Usage
==================================

Examples for using the plugin.

### Building content packages

Required steps:

* Create a separate maven project for the content package and set the packaging type to `content-package`.
* Define the package filters either via the `filters` plugin property or a predefined filter.xml references via `filterSource` property.
* Set `name` and `group` properties for the content package
* Make sure the content of the jcr_root folder for the package is copied to the `target/classes` folder, or you specify an alternative `builtContentDirectory`.

Examples from the wcm.io Sample application:

* [Content package with sample content](https://github.com/wcm-io/wcm-io-samples/tree/develop/sample-content)
* [Content package for application deployment](https://github.com/wcm-io/wcm-io-samples/tree/develop/complete)


### Install single content package from filesystem

```xml
<plugin>
  <groupId>io.wcm.maven.plugins</groupId>
  <artifactId>wcmio-content-package-maven-plugin</artifactId>
  <configuration>
    <serviceURL>http://localhost:4502/crx/packmgr/service</serviceURL>
    <userId>admin</userId>
    <password>admin</password>
    <packageFile>/path/to/content-package.zip</packageFile>
  </configuration>
</plugin>
```

Command line:

```
mvn wcmio-content-package:install
```


### Install multiple content packages from maven repository

This usecase is useful to install hotfix and service pack packages to an AEM instance.
The hotfixes have to be uploaded to an internal maven repository before, and can than deployed to any instance
via maven using their artifact coordinates. Setting force to false ensures that the packages are installed
only once, and not on every run.

```xml
<plugin>
  <groupId>io.wcm.maven.plugins</groupId>
  <artifactId>wcmio-content-package-maven-plugin</artifactId>
  <configuration>
    <serviceURL>http://localhost:4502/crx/packmgr/service</serviceURL>
    <userId>admin</userId>
    <password>admin</password>
    <!-- Skip upload if hotfix package was already uploaded before -->
    <force>false</force>
    <packageFiles>
      <packageFile>
        <groupId>groupId</groupId>
        <artifactId>artifactId-1</artifactId>
        <version>1.0.0</version>
        <type>zip</type>
      </packageFile>
      <packageFile>
        <groupId>groupId</groupId>
        <artifactId>artifactId-2</artifactId>
        <version>1.0.0</version>
        <type>zip</type>
      </packageFile>
    </packageFiles>
  </configuration>
</plugin>
```

Command line:

```
mvn wcmio-content-package:install
```


### Download content package with unpack and excludes

This downloads a content package back to the project from where it was uploaded from (using the Adobe
`content-package-maven-plugin`). After download it is unpacked automatically, some files are excluded (not unpacked)
and some properties are removed from the unpacked `.content.xml` files (to avoid unwanted changes in SCM commits
when versioning the unpacked package contents).

```xml
<plugin>
  <groupId>io.wcm.maven.plugins</groupId>
  <artifactId>wcmio-content-package-maven-plugin</artifactId>
  <configuration>
    <serviceURL>http://localhost:4502/crx/packmgr/service</serviceURL>
    <userId>admin</userId>
    <password>admin</password>
    <excludeFiles>
      <exclude>^META-INF/.*</exclude>
      <!-- exclude renditions that are automatically generated -->
      <exclude>.*/cq5dam\.thumbnail\..*</exclude>
    </excludeFiles>
    <excludeProperties>
      <exclude>jcr\:created</exclude>
      <exclude>jcr\:createdBy</exclude>
      <exclude>jcr\:lastModified</exclude>
      <exclude>jcr\:lastModifiedBy</exclude>
      <exclude>cq\:lastModified</exclude>
      <exclude>cq\:lastModifiedBy</exclude>
    </excludeProperties>
  </configuration>
</plugin>
```

Command line:

```
mvn -Dvault.unpack=true wcmio-content-package:download
```


### Run from command line without pom.xml context

You can execute the `install` and `download` goals also directly from the command line without a pom.xml context. You have to pass all parameters as Java System parameters then as well.

Example for directly installing a package without a pom:

```
mvn io.wcm.maven.plugins:wcmio-content-package-maven-plugin:1.6.10:install \
    -Dvault.file=mypackage.zip \
    -Dvault.serviceURL=http://localhost:4502/crx/packmgr/service
```

The full list of available parameters (user property names) can be found in the [plugin documentation][plugindocs].


[plugindocs]: plugin-info.html
