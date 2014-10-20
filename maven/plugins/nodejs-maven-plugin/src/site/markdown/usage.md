Node.js Maven Plugin Usage
==========================

Wraps the [Node.js](http://nodejs.org/) executables (nodejs and npm) in a maven plugin. Provides following configuration options:

- installation directory for nodes and npm (java.tmp.dir per default)
- version for nodejs and npm

Following configuration will install nodejs and npm in the /tmp/nodejs folder in the specified version.

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
    <npmVersion>1.4.9</npmVersion>
    <nodeJsVersion>0.10.32</nodeJsVersion>
    <nodeJsDirectory>/tmp/nodejs</nodeJsDirectory>
    <tasks>
      <nodeJsTask>
        <workingDirectory>${project.basedir}/src/test/javascript/</workingDirectory>
        <moduleName>karma</moduleName>
        <moduleVersion>1.0.0</moduleVersion>
        <arguments>
          <argument>start</argument>
        </arguments>
      </nodeJsTask>
    </tasks>
  </configuration>
</plugin>
```

See [plugin documentation](run-mojo.html) for a full documentation of the configuration parameters.


### Usage

Plugin provides two different task types:

* `<npmInstallTask>` - executes the 'npm install' in the specified directory.
* `<nodeJsTask>` - executes a specific nodejs module with optional parameters.

Following configuration executes the npm install task in the ${project.basedir}/target folder:

```xml
<npmInstallTask>
  <workingDirectory>${project.basedir}/target</workingDirectory>
</npmInstallTask>
```

It is also possible to specify multiple arguments:

```xml
<arguments>
  <argument>-g</argument>
</arguments>
```

Following configuration executes the karma moduel with argument "start":

```xml
<nodeJsTask>
  <workingDirectory>${project.basedir}/src/test/javascript/</workingDirectory>
  <moduleName>karma</moduleName>
  <moduleVersion>1.0.0</moduleVersion>
  <arguments>
    <argument>start</argument>
  </arguments>
</nodeJsTask>
```

The task will first check, if the karma module is installed. If not, the module will be installed automatically, before it is executed. If the executable name is different from the module name, it can be specified in the <executbaleName></executbaleName> configuration option.

Below is a coplete configuration for the execution of the npm install taks in a directory with package.json and execution of the grunt:

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
    <tasks>
      <npmInstallTask>
        <workingDirectory>${project.basedir}/_src</workingDirectory>
      </npmInstallTask>
      <nodeJsTask>
        <workingDirectory>${project.basedir}/_src</workingDirectory>
        <moduleName>grunt-cli</moduleName>
        <executableName>grunt</executableName>
      </nodeJsTask>
    </tasks>
  </configuration>
</plugin>
```
