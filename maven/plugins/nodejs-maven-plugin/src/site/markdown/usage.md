Node.js Maven Plugin Usage
==========================

Wraps the [Node.js](http://nodejs.org/) executables (Node.js and NPM) in a maven plugin. Provides following configuration options:

- installation directory for Node.js and NPM (`<java.tmp.dir>/nodejs/<nodejsVersion>` per default)
- version for Node.js
- version for NPM (optional, if not given the NPM version bundles with Node.js is used)

### Usage

The plugin provides two different task types:

* `<npmInstallTask>` - executes the 'npm install' in the specified directory.
* `<nodeJsTask>` - executes a specific nodejs module with optional parameters.

#### Example 1

```xml
<plugin>
  <groupId>io.wcm.maven.plugins</groupId>
  <artifactId>nodejs-maven-plugin</artifactId>
  <executions>
    <execution>
      <phase>compile</phase>
      <goals>
        <goal>run</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <nodeJsVersion>10.15.3</nodeJsVersion>
    <tasks>
      <npmInstallTask>
        <workingDirectory>${project.basedir}</workingDirectory>
      </npmInstallTask>
      <nodeJsTask>
        <workingDirectory>${project.basedir}</workingDirectory>
        <moduleName>npm</moduleName>
        <executableName>npm-cli</executableName>
        <arguments>
          <argument>run</argument>
          <argument>test</argument>
        </arguments>
      </nodeJsTask>
    </tasks>
  </configuration>
</plugin>
```

#### Example 2

```xml
<plugin>
  <groupId>io.wcm.maven.plugins</groupId>
  <artifactId>nodejs-maven-plugin</artifactId>
  <executions>
    <execution>
      <phase>compile</phase>
      <goals>
        <goal>run</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <npmVersion>10.15.3</npmVersion>
    <nodeJsVersion>6.4.1</nodeJsVersion>
    <nodeJsDirectory>${project.basedir}/customNodeJsDir</nodeJsDirectory>
    <tasks>
      <npmInstallTask>
        <workingDirectory>${frontend.dir}</workingDirectory>
      </npmInstallTask>
      <nodeJsTask>
        <workingDirectory>${frontend.dir}</workingDirectory>
        <moduleName>grunt-cli</moduleName>
        <executableName>grunt</executableName>
        <arguments>
          <argument>build</argument>
        </arguments>
      </nodeJsTask>
    </tasks>
  </configuration>
</plugin>
```
