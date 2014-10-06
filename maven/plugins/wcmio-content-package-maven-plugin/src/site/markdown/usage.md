Content Package Maven Plugin Usage
==================================

Examples for using the plugin.


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

Command line: `mvn wcmio-content-package:install`


### Install multiple content packages from maven repository 

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

Command line: `mvn wcmio-content-package:install`
