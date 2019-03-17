## AEM Dependencies

Defines Maven dependencies for a specific AEM version, including those that are not defined in the aem-api "Uber" JAR provided by Adobe. Additionally, the POM includes Sling-internal dependencies required for [AEM Mocks][aem-mock] in exactly the versions included in the AEM version.

Source code: [maven/aem-dependencies](https://github.com/wcm-io/wcm-io-tooling/tree/develop/maven/aem-dependencies)

### AEM 6.4 SP3

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.4.3.0000</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

### AEM 6.4

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.4.0.0000</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

### AEM 6.3 SP3

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.3.3.0000</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

### AEM 6.3

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.3.0.0002</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

### AEM 6.2

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.2.0.0002</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

### AEM 6.1

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.1.0.0001</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

### AEM 6.0

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>6.0.0.0001</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

### CQ 5.5

```xml
<dependency>
  <groupId>io.wcm.maven</groupId>
  <artifactId>io.wcm.maven.aem-dependencies</artifactId>
  <version>5.5.0.0000</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```


[aem-mock]: http://wcm.io/testing/aem-mock/
