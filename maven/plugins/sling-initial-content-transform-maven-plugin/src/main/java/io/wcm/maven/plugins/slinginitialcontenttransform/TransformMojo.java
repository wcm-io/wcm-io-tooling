/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.maven.plugins.slinginitialcontenttransform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.packaging.PackageType;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import io.wcm.maven.plugins.slinginitialcontenttransform.contentparser.JsonContentLoader;
import io.wcm.maven.plugins.slinginitialcontenttransform.contentparser.XmlContentLoader;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackage;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder;
import io.wcm.tooling.commons.contentpackagebuilder.PackageFilter;
import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;

/**
 * Extracts Sling-Initial-Content form an OSGi bundle and attaches two artifacts with classifiers instead:
 * <ul>
 * <li>bundle: OSGi bundle without the Sling-Initial-Content</li>
 * <li>content: Content packages with the Sling-Initial-Content transformed to FileVault</li>
 * </ul>
 */
@Mojo(name = "transform", requiresProject = true, threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class TransformMojo extends AbstractMojo {

  private static final String CLASSIFIER_CONTENT = "content";
  private static final String CLASSIFIER_BUNDLE = "bundle";
  private static final String MANIFEST_FILE = "META-INF/MANIFEST.MF";

  /**
   * Allows to skip the plugin execution.
   */
  @Parameter(property = "slinginitialcontenttransform.skip", defaultValue = "false")
  private boolean skip;

  /**
   * The name of the OSGi bundle file to process.
   */
  @Parameter(property = "slinginitialcontenttransform.file", defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
  private File file;

  /**
   * The group of the package.
   */
  @Parameter(
      property = "slinginitialcontenttransform.group",
      defaultValue = "${project.groupId}",
      required = true)
  private String group;

  @Parameter(property = "project", required = true, readonly = true)
  private MavenProject project;
  @Component
  private MavenProjectHelper projectHelper;

  private final JsonContentLoader jsonContentLoader = new JsonContentLoader();
  private final XmlContentLoader xmlContentLoader = new XmlContentLoader();

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      getLog().debug("Skipping execution.");
      return;
    }
    if (!StringUtils.equals(project.getPackaging(), "jar")) {
      getLog().debug("Not a jar project: " + project.getPackaging());
      return;
    }
    if (!file.exists()) {
      getLog().warn("File does not exist: " + file.getPath());
      return;
    }

    try (OsgiBundleFile osgiBundle = new OsgiBundleFile(file)) {
      if (!osgiBundle.hasContent()) {
        getLog().debug("Bundle does not contain Sling-Initial-Content.");
        return;
      }
      transformBundle(osgiBundle);
    }
    catch (IOException ex) {
      throw new MojoExecutionException("Unable to transform bundle.", ex);
    }
  }

  /**
   * Transform OSGi bundle with Sling-Initial-Content two two separate artifacts with classifier "content" and "bundle".
   * @throws IOException I/O exception
   */
  private void transformBundle(OsgiBundleFile osgiBundle) throws IOException {
    File contentPackageFile = createContentPackage(osgiBundle);
    projectHelper.attachArtifact(project, "zip", CLASSIFIER_CONTENT, contentPackageFile);

    File bundleFile = createBundleWithoutContent(osgiBundle);
    projectHelper.attachArtifact(project, "jar", CLASSIFIER_BUNDLE, bundleFile);
  }

  /**
   * Extract Sling-Initial-Content to a content package.
   * @param osgiBundle OSGi bundle
   * @return Content package file
   * @throws IOException I/O exception
   */
  private File createContentPackage(OsgiBundleFile osgiBundle) throws IOException {
    String contentPackageName = project.getBuild().getFinalName() + "-" + CLASSIFIER_CONTENT + ".zip";
    File contentPackageFile = new File(project.getBuild().getDirectory(), contentPackageName);
    if (contentPackageFile.exists()) {
      Files.delete(contentPackageFile.toPath());
    }

    ContentPackageBuilder contentPackageBuilder = new ContentPackageBuilder()
        .group(this.group)
        .name(project.getArtifactId() + "-" + CLASSIFIER_CONTENT)
        .version(project.getVersion())
        .packageType(PackageType.APPLICATION.name().toLowerCase());
    for (ContentMapping mapping : osgiBundle.getContentMappings()) {
      contentPackageBuilder.filter(new PackageFilter(mapping.getContentPath()));
    }
    for (Map.Entry<String, String> namespace : osgiBundle.getNamespaces().entrySet()) {
      contentPackageBuilder.xmlNamespace(namespace.getKey(), namespace.getValue());
    }
    try (ContentPackage contentPackage = contentPackageBuilder.build(contentPackageFile)) {
      for (ContentMapping mapping : osgiBundle.getContentMappings()) {
        List<BundleEntry> entries = osgiBundle.getEntries(mapping).collect(Collectors.toList());
        for (BundleEntry entry : entries) {
          addContent(contentPackage, entry, mapping);
        }
      }
    }

    getLog().info("Extracted Sling-Initial-Content: " + contentPackageFile.getName());
    return contentPackageFile;
  }

  private void addContent(ContentPackage contentPackage, BundleEntry entry, ContentMapping mapping) throws IOException {
    String extension = FilenameUtils.getExtension(entry.getPath());
    if (mapping.isJson() && StringUtils.equals(extension, "json")) {
      addJsonContent(contentPackage, entry);
    }
    if (mapping.isXml() && StringUtils.equals(extension, "xml")) {
      addXmlContent(contentPackage, entry);
    }
    else {
      addBinaryContent(contentPackage, entry);
    }
  }

  private void addJsonContent(ContentPackage contentPackage, BundleEntry entry) throws IOException {
    try (InputStream is = entry.getInputStream()) {
      ContentElement contentElement = jsonContentLoader.load(is);
      String path = StringUtils.substringBeforeLast(entry.getPath(), ".json");
      contentPackage.addContent(path, contentElement);
    }
  }

  private void addXmlContent(ContentPackage contentPackage, BundleEntry entry) throws IOException {
    try (InputStream is = entry.getInputStream()) {
      ContentElement contentElement = xmlContentLoader.load(is);
      String path = StringUtils.substringBeforeLast(entry.getPath(), ".xml");
      contentPackage.addContent(path, contentElement);
    }
  }

  private void addBinaryContent(ContentPackage contentPackage, BundleEntry entry) throws IOException {
    try (InputStream is = entry.getInputStream()) {
      contentPackage.addFile(entry.getPath(), is);
    }
  }

  /**
   * Create OSGi bundle JAR file without Sling-Initial-Content.
   * @param osgiBundle OSGi bundle
   * @return OSGi bundle file
   * @throws IOException I/O exception
   */
  private File createBundleWithoutContent(OsgiBundleFile osgiBundle) throws IOException {
    String bundleFileName = project.getBuild().getFinalName() + "-" + CLASSIFIER_BUNDLE + ".jar";
    File bundleFile = new File(project.getBuild().getDirectory(), bundleFileName);
    if (bundleFile.exists()) {
      Files.delete(bundleFile.toPath());
    }

    try (FileOutputStream fos = new FileOutputStream(bundleFile);
        ZipOutputStream zos = new ZipOutputStream(fos)) {
      List<BundleEntry> entries = osgiBundle.getNonContentEntries().collect(Collectors.toList());
      for (BundleEntry entry : entries) {
        zos.putNextEntry(new ZipEntry(entry.getPath()));
        if (!entry.isDirectory()) {
          try (InputStream is = entry.getInputStream()) {
            if (StringUtils.equals(entry.getPath(), MANIFEST_FILE)) {
              Manifest transformedManifest = getManifestWithoutSlingInitialContentHeader(is);
              transformedManifest.write(zos);
            }
            else {
              IOUtils.copy(is, zos);
            }
          }
        }
      }
    }

    getLog().info("Created bundle without content: " + bundleFile.getName());
    return bundleFile;
  }

  private Manifest getManifestWithoutSlingInitialContentHeader(InputStream is) throws IOException {
    Manifest manifest = new Manifest(is);
    manifest.getMainAttributes().remove(new Attributes.Name(OsgiBundleFile.HEADER_INITIAL_CONTENT));
    return manifest;
  }

}
