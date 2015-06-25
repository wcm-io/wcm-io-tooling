## wcm.io Content Package Builder - Usage


### Create a Content Package

To create a content package with content pages and one binary files:

```java
File zipFile = new File("myZipFile.zip");

ContentPackageBuilder builder = new ContentPackageBuilder()
    .name("myName")
    .group("myGroup")
    .rootPath("/apps/myapp/config");
try (ContentPackage contentPackage = builder.build(zipFile)) {

  // add two content pages
  contentPackage.addPage("/content/page1", ImmutableMap.<String, Object>of("var1", "v1"));
  contentPackage.addPage("/content/page2", ImmutableMap.<String, Object>of("var2", "v2"));

  // add a binary file
  contentPackage.addFile("/content/file1.txt", myBinaryFiles);

}
```

The required metadata files at 'META-INF/vault' are created automatically.


### Use package filters

In this example we define more complex package filters and use a free content structure:

```java
File zipFile = new File("myZipFile.zip");

ContentPackageBuilder builder = new ContentPackageBuilder()
    .name("myName")
    .group("myGroup")
    .filter(new PackageFilter("/etc/map/http")
        .addExcludeRule("/etc/map/http")
        .addIncludeRule("/etc/map/http/.*")
        .addExcludeRule("/etc/map/http/AppMeasurementBridge"));

try (ContentPackage contentPackage = builder.build(zipFile)) {

  // add some JCR nodes
  contentPackage.addContent("/etc/map/http", ImmutableMap.<String, Object>builder()
      .put("jcr:primaryType", "sling:Folder")
      .put("mysite.com", ImmutableMap.of("jcr:primaryType", "sling:Mapping",
          "sling:internalRedirect", ImmutableList.of("/content/mysite", "/")))
      .build());

}
```
