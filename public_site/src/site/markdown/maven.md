## Maven Repositories

### wcm.io Repository

The released wcm.io artifacts are available at Maven Central:

https://search.maven.org/search?q=io.wcm

Snapshots releases are available on the Sonatype snapshot repository - use at your own risk!

https://oss.sonatype.org/content/repositories/snapshots/

The maven artifact coordinates are documented on the index page of each wcm.io module.



### Apache Snapshot Repository

Sometimes snapshot are referenced from the Apache Snapshot repository:

```xml
<repository>
  <id>apache-snapshots</id>
  <url>https://repository.apache.org/snapshots</url>
  <layout>default</layout>
  <releases>
    <enabled>false</enabled>
  </releases>
  <snapshots>
    <enabled>true</enabled>
    <updatePolicy>always</updatePolicy>
  </snapshots>
</repository>
```