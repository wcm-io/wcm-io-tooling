## AEM Dependencies

Defines Maven dependencies for a specific AEM version, including those that are not defined in the AEM API JAR provided by Adobe. Additionally, the POM includes Sling-internal dependencies required for [AEM Mocks][aem-mock] in exactly the versions included in the AEM version.

The first part of the AEM dependencies version number matches with the AEM version, and the version of the AEM API JAR. The last part (4 digits) is the revision number added by the wcm.io project, and is incremented if a fix or update of the POM needs to be published for the same AEM versions.

### AEM Cloud Service

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.maven/io.wcm.maven.aem-cloud-dependencies/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.maven/io.wcm.maven.aem-cloud-dependencies)

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-cloud-dependencies</artifactId>
  <version><!-- latest version, see above --></version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

Source code: [wcm-io-tooling-aem-cloud-dependencies](https://github.com/wcm-io/wcm-io-tooling-aem-cloud-dependencies)


### AEM 6.5 - Latest Version

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.maven/io.wcm.maven.aem-dependencies/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.maven/io.wcm.maven.aem-dependencies)


```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version><!-- latest version, see above --></version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

Source code: [wcm-io-tooling/maven/aem-dependencies](https://github.com/wcm-io/wcm-io-tooling/tree/develop/maven/aem-dependencies)


### Older AEM 6.x versions

#### AEM 6.5.0

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.5.0.0005</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

#### AEM 6.4.8

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.4.8.0006</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

#### AEM 6.4.0

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.4.0.0004</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

#### AEM 6.3.3

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.3.3.0002</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

#### AEM 6.3.0

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.3.0.0003</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

#### AEM 6.2.0

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.2.0.0002</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

#### AEM 6.1.0

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.1.0.0001</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

#### AEM 6.0.0

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.0.0.0001</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

#### CQ 5.5.0

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>5.5.0.0000</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```


[aem-mock]: https://wcm.io/testing/aem-mock/
