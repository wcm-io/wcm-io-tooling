## wcm.io Content Package Builder - Usage


### Create a Content Package

To create a content package with content pages and one binary files:

```java
File zipFile = new File("myZipFile.zip");

ContentPackageBuilder builder = new ContentPackageBuilder()
    .name("myName")
    .group("myGroup")
    .rootPath("/apps/myapp/config");
try (ContentPackage contentPackage = new builder.build(zipFile)) {

  // add two content pages
  contentPackage.addPage("/content/page1", ImmutableMap.<String, Object>of("var1", "v1"));
  contentPackage.addPage("/content/page2", ImmutableMap.<String, Object>of("var2", "v2"));

  // add a binary file
  contentPackage.addFile("/content/file1.txt", myBinaryFiles);

}
```

The required metadata files at 'META-INF/vault' are created automatically.
