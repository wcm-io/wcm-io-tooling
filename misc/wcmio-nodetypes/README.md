# wcm.io Nodetypes

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.tooling.nodetypes/io.wcm.tooling.nodetypes.wcmio/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.tooling.nodetypes/io.wcm.tooling.nodetypes.wcmio)

# Overview
Provides a [CND file][1] with all Node types and Namespaces defined by wcm.io Modules. It can be used for the [FileVault Validation Module][2] and its `jackrabbit-nodetype` validator as addition to the [AEM nodetypes][4].

# Usage with Maven
You can use this module with the [FileVault Package Maven Plugin][3] in version 1.1.4 or higher like this:

```
<plugin>
  <groupId>org.apache.jackrabbit</groupId>
  <artifactId>filevault-package-maven-plugin</artifactId>
  <configuration>
    <validatorsSettings>
      <jackrabbit-nodetypes>
        <options>
          <!-- use the nodetypes and namespaces from the aem-nodetypes.jar provided in the plugin dependencies -->
          <cnds>tccl:aem.cnd,tccl:wcmio.cnd</cnds>
        </options>
      </jackrabbit-nodetypes>
    </validatorsSettings>
  </configuration>
  <dependencies>
    <dependency>
      <groupId>biz.netcentric.aem</groupId>
      <artifactId>aem-nodetypes</artifactId>
      <version><!-- pick AEM version --></version>
    </dependency>
    <dependency>
      <groupId>io.wcm.tooling.nodetypes</groupId>
      <artifactId>io.wcm.tooling.nodetypes.wcmio</artifactId>
      <version>1.0.0</version>
    </dependency>
  </dependencies>
</plugin>
```


[1]: https://jackrabbit.apache.org/jcr/node-type-notation.html
[2]: https://jackrabbit.apache.org/filevault/validation.html
[3]: https://jackrabbit.apache.org/filevault-package-maven-plugin/index.html
[4]: https://github.com/Netcentric/aem-nodetypes
