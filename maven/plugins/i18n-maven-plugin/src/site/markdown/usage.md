i18n Maven Plugin Usage
=======================

Examples for using the plugin.


### Maintaining your i18n resources in the maven project

Create a folder `src/main/resources/i18n` and create the properties, json or xml files with the raw i18n message data.

To exclude those files from the resulting JAR file add to the POM:

```xml
<build>
  <resources>
    <resource>
      <directory>src/main/resources</directory>
      <filtering>false</filtering>
      <excludes>
        <!-- those resources are processed by the i18n-maven-plugin -->
        <exclude>i18n/**</exclude>
      </excludes>
    </resource>
...
```

To enable the i18n transformation step put this plugin definition to you POM:

```xml
<plugin>
  <groupId>io.wcm.maven.plugins</groupId>
  <artifactId>i18n-maven-plugin</artifactId>
  <executions>
    <execution>
      <goals>
        <goal>transform</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```


### Input format i18n Properties File

If you want to maintain your i18n resources in a Java Properties format you can use a file like this.

```
key1=value1
key21.key22.key23=value 2
```


### Input format i18n JSON File

If you want to maintain your i18n resources in JSON format you can use a file like this. You can use the hierarchy of the JSON format to build key hierarchies separated by ".".

```json
{
  "key1": "value1",
  "key21": {
    "key22": {
      "key23": "value 2"
    }
  }
}
```


### Input format i18n XML File

If you want to maintain your i18n resources in XML format you can use a file like this. You can use the hierarchy of the JSON format to build key hierarchies separated by ".".

```xml
<?xml version="1.0" encoding="UTF-8"?>
<i18n>
  <key1>value1</key1>
  <key21>
    <key22>
      <key23>value 2</key23>
    </key22>
  </key21>
</i18n>
```


### Output format i18n JSON File (Sling i18n Message Format)

Any of the above listed input formats will result in the following output format after translation (when outputFormat is set to 'json'):

```json
{
  "jcr:primaryType": "nt:folder",
  "jcr:mixinTypes": ["mix:language"],
  "jcr:language": "en",
  "key1": {
    "jcr:primaryType": "nt:folder",
    "jcr:mixinTypes": ["sling:Message"],
    "sling:message": "value1"
  },
  "key21.key22.key23": {
    "jcr:primaryType": "nt:folder",
    "jcr:mixinTypes": ["sling:Message"],
    "sling:message": "value 2"
  }
}
```


### Output format i18n XML File (Sling i18n Message Format)

Any of the above listed input formats will result in the following output format after translation (when outputFormat is set to 'xml'):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0"
    xmlns:mix="http://www.jcp.org/jcr/mix/1.0" xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
    jcr:primaryType="nt:folder"
    jcr:mixinTypes="[mix:language]"
    jcr:language="en">
  <key1
      jcr:primaryType="nt:folder"
      jcr:mixinTypes="[sling:Message]"
      sling:message="value1"/>
  <key21.key22.key23
      jcr:primaryType="nt:folder"
      jcr:mixinTypes="[sling:Message]"
      sling:message="value 2"/>
</jcr:root>
```
