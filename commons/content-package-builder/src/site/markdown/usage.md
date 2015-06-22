## wcm.io Content Package Builder - Usage


### Create a Content Package

To create a content package with content pages and one binary files:

```java
File zipFile = new File("myZipFile.zip");

ContentPackage contentPackage = new ContentPackageBuilder().name("myName").group("myGroup").build(zipFile);

// add two content pages
contentPackage.addPage("/content/page1", ImmutableMap.<String, Object>of("var1", "v1"));
contentPackage.addPage("/content/page2", ImmutableMap.<String, Object>of("var2", "v2"));

// add a binary file
contentPackage.addFile("/content/file1.txt", myBinaryFiles);

// close package
contentPackage.close();
```

The required metadata files at 'META-INF/vault' are created automatically.
