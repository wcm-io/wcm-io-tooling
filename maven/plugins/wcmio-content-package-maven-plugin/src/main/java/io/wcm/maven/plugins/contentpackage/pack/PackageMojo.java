/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.maven.plugins.contentpackage.pack;

import static org.apache.jackrabbit.vault.util.Constants.CONFIG_XML;
import static org.apache.jackrabbit.vault.util.Constants.FILTER_XML;
import static org.apache.jackrabbit.vault.util.Constants.META_DIR;
import static org.apache.jackrabbit.vault.util.Constants.PACKAGE_DEFINITION_XML;
import static org.apache.jackrabbit.vault.util.Constants.PROPERTIES_XML;
import static org.apache.jackrabbit.vault.util.Constants.SETTINGS_XML;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.config.ConfigurationException;
import org.apache.jackrabbit.vault.fs.config.DefaultWorkspaceFilter;
import org.apache.jackrabbit.vault.fs.io.AccessControlHandling;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.maven.archiver.ManifestConfiguration;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Creates a JCR Content Package with embedded Bundles and Packages.
 */
@Mojo(
    name = "package",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.COMPILE)
public final class PackageMojo extends AbstractMojo {

  private static final String ETC_PACKAGES = "/etc/packages";

  private static final String JCR_ROOT = "jcr_root/";
  private static final String PACKAGE_TYPE = "zip";
  private static final String PACKAGE_EXT = "." + PACKAGE_TYPE;
  private static final String DEFINITION_FOLDER = "definition";
  private static final String THUMBNAIL_FILE = "thumbnail.png";
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private static final String PROPERTY_GROUP = "group";
  private static final String PROPERTY_NAME = "name";
  private static final String PROPERTY_VERSION = "version";
  private static final String PROPERTY_GROUP_ID = "groupId";
  private static final String PROPERTY_ARTIFACT_ID = "artifactId";
  private static final String PROPERTY_DEPENDENCIES = "dependencies";
  private static final String PROPERTY_CREATED_BY = "createdBy";
  private static final String PROPERTY_CREATED = "created";
  private static final String PROPERTY_REQUIRES_ROOT = "requiresRoot";
  private static final String PROPERTY_ALLOW_INDEX_DEFINITIONS = "allowIndexDefinitions";
  private static final String PROPERTY_PATH = "path";
  private static final String PROPERTY_AC_HANDLING = "acHandling";

  @Component
  private ArtifactHandlerManager artifactHandlerManager;

  /**
   * The Maven project.
   */
  @Parameter(property = "project", readonly = true, required = true)
  private MavenProject project;

  /**
   * The archive configuration to use. See <a
   * href="http://maven.apache.org/shared/maven-archiver/index.html">the
   * documentation for Maven Archiver</a>.
   */
  @Parameter
  private MavenArchiveConfiguration archive;

  /**
   * The directory containing the content to be packaged up into the content
   * package. For now any content here is disregarded. Please copy the content
   * into the workDirectory.
   */
  @Parameter(
      defaultValue = "${project.build.outputDirectory}",
      required = true)
  private File builtContentDirectory;

  /**
   * The name of the generated package ZIP file without the ".zip" file
   * extension.
   */
  @Parameter(
      property = "vault.finalName",
      defaultValue = "${project.build.finalName}",
      required = true)
  private String finalName;

  /**
   * Directory in which the built content package will be output.
   */
  @Parameter(
      defaultValue = "${project.build.directory}",
      required = true)
  private File outputDirectory;

  /**
   * The directory containing the content to be packaged up into the content
   * package.
   */
  @Parameter(
      defaultValue = "${project.build.directory}/vault-work",
      required = true)
  private File workDirectory;

  //TODO: Prefix not supported yet
  //  /**
  //   * Adds a path prefix to all resources useful for shallower source trees.
  //   */
  //  @Parameter(property = "vault.prefix")
  //  private String prefix;

  /**
   * The group of the package (location where the package is installed)
   */
  @Parameter(
      property = "vault.group",
      defaultValue = "${project.groupId}",
      required = true)
  private String group;

  /**
   * The name of the deployed package on the target server
   */
  @Parameter(
      property = "vault.name",
      defaultValue = "${project.artifactId}",
      required = true)
  private String name;

  /**
   * The version of the artifact to install.
   */
  @Parameter(
      property = "vault.version",
      defaultValue = "${project.version}",
      required = true)
  private String version;

  /**
   * Defines whether the package requires root. This will become the
   * <code>requiresRoot</code> property of the properties.xml file.
   */
  @Parameter(
      property = "vault.requiresRoot",
      defaultValue = "false",
      required = true)
  private boolean requiresRoot;

  /**
   * Defines whether the package is allowed to contain index definitions. This will become the
   * <code>allowIndexDefinitions</code> property of the properties.xml file.
   * As of now there is no check if any files contain index definitions if this is set to false
   */
  @Parameter(
      property = "vault.allowIndexDefinitions",
      defaultValue = "false",
      required = true)
  private boolean allowIndexDefinitions;

  /**
   * Defines the AC Handling (see vault properties.xml file). It must be
   * a String representation of the AccessControlHandling enum otherwise
   * the packaging will fail.
   */
  @Parameter(
      property = "vault.acHandling",
      defaultValue = "IGNORE",
      required = false)
  private AccessControlHandling acHandling;

  /**
   * Optional file that specifies the source of the workspace filter. The filters specified in the configuration
   * and injected via embeddeds or subpackages are merged into it.
   */
  @Parameter(defaultValue = "src/main/package-definition/" + FILTER_XML)
  private File filterSource;

  /**
   * Defines the content of the filter.xml file
   */
  @Parameter
  private final Filters filters = new Filters();

  /**
   * Optional reference to PNG image that should be used as thumbnail for the content package.
   */
  @Parameter(defaultValue = "src/main/package-definition/" + THUMBNAIL_FILE)
  private File thumbnailImage;

  /**
   * list of embedded bundles
   */
  @Parameter
  private final List<EmbeddedBundle> embeddeds = new ArrayList<EmbeddedBundle>();

  /**
   * Defines whether to fail the build when an embedded artifact is not
   * found in the project's dependencies
   */
  @Parameter(
      property = "vault.failOnMissingEmbed",
      defaultValue = "false",
      required = true)
  private boolean failOnMissingEmbed;

  /**
   * Defines the list of dependencies
   */
  @Parameter
  private final List<Dependency> dependencies = new ArrayList<Dependency>();

  /**
   * Defines the list of nested packages.
   */
  @Parameter
  private final List<NestedPackage> subPackages = new ArrayList<NestedPackage>();

  /**
   * Specifies additional properties to be set in the properties.xml file.
   * These properties cannot overwrite the following predefined properties:
   * <p>
   * <table>
   * <tr>
   * <td>group</td>
   * <td>Use <i>group</i> parameter to set</td>
   * </tr>
   * <tr>
   * <td>name</td>
   * <td>Use <i>name</i> parameter to set</td>
   * </tr>
   * <tr>
   * <td>version</td>
   * <td>Use <i>version</i> parameter to set</td>
   * </tr>
   * <tr>
   * <td>groupId</td>
   * <td><i>groupId</i> of the Maven project descriptor</td>
   * </tr>
   * <tr>
   * <td>artifactId</td>
   * <td><i>artifactId</i> of the Maven project descriptor</td>
   * </tr>
   * <tr>
   * <td>dependencies</td>
   * <td>Use <i>dependencies</i> parameter to set</td>
   * </tr>
   * <tr>
   * <td>createdBy</td>
   * <td>The value of the <i>user.name</i> system property</td>
   * </tr>
   * <tr>
   * <td>created</td>
   * <td>The current system time</td>
   * </tr>
   * <tr>
   * <td>requiresRoot</td>
   * <td>Use <i>requiresRoot</i> parameter to set</td>
   * </tr>
   * <tr>
   * <td>allowIndexDefinitions</td>
   * <td>Use <i>allowIndexDefinitions</i> parameter to set</td>
   * </tr>
   * <tr>
   * <td>packagePath</td>
   * <td>Automatically generated from the group and package name</td>
   * </tr>
   * <tr>
   * <td>acHandling</td>
   * <td>Use <i>acHandling</i> parameter to set it</td>
   * </tr>
   * </table>
   */
  @Parameter
  private final Properties properties = new Properties();

  /**
   * Defines the path under which the embedded bundles are placed. defaults to '/apps/bundles/install'
   */
  @Parameter(property = "vault.embeddedTarget")
  private String embeddedTarget;

  /**
   * @param embeddedBundle Embedded bundle
   */
  public void addEmbedded(final EmbeddedBundle embeddedBundle) {
    embeddeds.add(embeddedBundle);
  }

  /**
   * @param dependency Dependency
   */
  public void addDependency(final Dependency dependency) {
    dependencies.add(dependency);
  }

  /**
   * @param nestedPackage Nested package
   */
  public void addPackage(final NestedPackage nestedPackage) {
    subPackages.add(nestedPackage);
  }

  /**
   * @param embeddedTarget Embedded target
   */
  public void setEmbeddedTarget(final String embeddedTarget) {
    if (embeddedTarget.endsWith("/")) {
      this.embeddedTarget = embeddedTarget;
    }
    else {
      this.embeddedTarget = embeddedTarget + "/";
    }
  }

  private MavenArchiveConfiguration getMavenArchiveConfiguration() {
    if (archive == null) {
      archive = new MavenArchiveConfiguration();
      archive.setManifest(new ManifestConfiguration());

      archive.setAddMavenDescriptor(true);
      archive.setCompress(true);
      archive.setIndex(false);
      archive.getManifest().setAddDefaultSpecificationEntries(true);
      archive.getManifest().setAddDefaultImplementationEntries(true);
    }

    return archive;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      final File finalFile = new File(outputDirectory, finalName + PACKAGE_EXT);
      final File vaultFolder = new File(workDirectory, META_DIR);
      final File vaultDefinitionFolder = new File(vaultFolder, DEFINITION_FOLDER);
      vaultFolder.mkdirs();
      vaultDefinitionFolder.mkdirs();

      JcrContentPackageArchiver jcrContentPackageArchiver = new JcrContentPackageArchiver();
      Map<String, File> additionalFiles = new HashMap<>();

      // get filter definition from file system
      DefaultWorkspaceFilter filter = null;
      if (filterSource != null && filterSource.exists() && !filterSource.isDirectory()) {
        filter = loadFilter(filterSource);
      }
      else {
        filter = loadFilterInFolder(vaultFolder);
      }

      // merge with filters applied via plugin properties
      if (this.filters != null) {
        this.filters.merge(filter);
      }

      obtainEmbeddedBundles(embeddeds, additionalFiles, filter);
      obtainNestedPackages(subPackages, additionalFiles, filter);

      File filterFile = new File(vaultFolder, FILTER_XML);
      // Reset the Filter to make sure the output is adjusted to any changes
      filter.resetSource();
      FileUtils.fileWrite(filterFile.getAbsolutePath(), filter.getSourceAsString());

      writePropertiesFile(vaultFolder);
      checkAndCopy(vaultFolder, CONFIG_XML);
      checkAndCopy(vaultFolder, SETTINGS_XML);
      checkAndCopy(vaultFolder, PACKAGE_DEFINITION_XML);
      if (thumbnailImage != null && thumbnailImage.exists()) {
        FileUtils.copyFile(thumbnailImage, new File(vaultDefinitionFolder, THUMBNAIL_FILE));
      }
      jcrContentPackageArchiver.addDirectory(workDirectory);

      // add content from builtContentDirectory
      if (builtContentDirectory.exists()) {
        jcrContentPackageArchiver.addDirectory(builtContentDirectory, FileUtils.normalize(JCR_ROOT));
      }

      // ensure that empty directories are included
      jcrContentPackageArchiver.setIncludeEmptyDirs(true);
      // Add additional files
      for (Map.Entry<String, File> entry : additionalFiles.entrySet()) {
        jcrContentPackageArchiver.addFile(entry.getValue(), entry.getKey());
      }

      MavenArchiver mavenArchiver = new MavenArchiver();
      mavenArchiver.setArchiver(jcrContentPackageArchiver);
      mavenArchiver.setOutputFile(finalFile);
      mavenArchiver.createArchive(null, project, getMavenArchiveConfiguration());

      final Artifact projectArtifact = project.getArtifact();
      projectArtifact.setFile(finalFile);
      projectArtifact.setArtifactHandler(artifactHandlerManager.getArtifactHandler(PACKAGE_TYPE));
    }
    /*CHECKSTYLE:OFF*/catch (Exception ex) { /*CHECKSTYLE:ON*/
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }

  private void obtainEmbeddedBundles(List<EmbeddedBundle> embeddedBundleList, Map<String, File> resultList, DefaultWorkspaceFilter filter)
      throws MojoFailureException {
    for (EmbeddedBundle embeddedBundle : embeddedBundleList) {
      final List<Artifact> artifacts = embeddedBundle.getMatchingArtifacts(project);
      if (artifacts.isEmpty()) {
        if (failOnMissingEmbed) {
          throw new MojoFailureException(
              "Embedded artifact: '" + embeddedBundle + "' but no dependency artifact found. Add the missing dependency or adjust the embedded definition.");
        }
        else {
          getLog().warn("No matching artifacts found for '" + embeddedBundle + "'");
          continue;
        }
      }
      if (embeddedBundle.getDestFileName() != null && artifacts.size() > 1) {
        getLog().warn("Destination File Name defined but several artifacts match the '" + embeddedBundle + "'");
      }

      String embeddedTargetPath = embeddedBundle.getTarget();
      if (embeddedTargetPath == null) {
        embeddedTargetPath = embeddedTarget;
        if (embeddedTargetPath == null) {
          embeddedTargetPath = "/apps/bundles/install/";
          getLog().info(
              "No target path set for '" + embeddedBundle + "'. Using default '" + embeddedTargetPath + "'");
        }
      }
      embeddedTargetPath = resolvePath(embeddedTargetPath);

      embeddedTargetPath = JCR_ROOT + embeddedTargetPath;
      embeddedTargetPath = FileUtils.normalize(embeddedTargetPath);

      getLog().info("Embedding Bundle '" + embeddedBundle + "'");
      for (final Artifact artifact : artifacts) {
        final File source = artifact.getFile();
        String destinationFileName = embeddedBundle.getDestFileName();
        if (destinationFileName == null) {
          destinationFileName = source.getName();
        }
        final String targetPathName = embeddedTargetPath + destinationFileName;
        resultList.put(targetPathName, source);
        final String targetNodePathName = targetPathName.substring(JCR_ROOT.length() - 1);
        if (embeddedBundle.isGenerateFilter()) {
          filter.add(new PathFilterSet(targetNodePathName));
        }
      }
    }
  }

  private void obtainNestedPackages(List<NestedPackage> nestedPackageList, Map<String, File> resultList, DefaultWorkspaceFilter filter) throws IOException {
    for (NestedPackage nestedPackage : nestedPackageList) {
      final List<Artifact> artifacts = nestedPackage.getMatchingArtifacts(project);
      if (artifacts.isEmpty()) {
        getLog().warn("No matching artifacts for nested package: '" + nestedPackage + "'");
        continue;
      }

      // get the package path
      getLog().info("Adding Nested Package '" + nestedPackage + "'");
      for (Artifact artifact : artifacts) {
        final File source = artifact.getFile();

        // load properties
        ZipFile zipFile = null;
        InputStream in = null;
        Properties props = new Properties();
        try {
          zipFile = new ZipFile(source, ZipFile.OPEN_READ);
          ZipEntry zipEntry = zipFile.getEntry("META-INF/vault/properties.xml");
          if (zipEntry == null) {
            getLog().error("Package is invalid as it does not contain properties.xml");
            throw new IOException("properties.xml missing in nested package: " + source.getName());
          }
          in = zipFile.getInputStream(zipEntry);
          props.loadFromXML(in);
        }
        finally {
          IOUtils.closeQuietly(in);
          if (zipFile != null) {
            zipFile.close();
          }
        }
        PackageId pid = new PackageId(
            props.getProperty("group"),
            props.getProperty("name"),
            props.getProperty("version"));
        final String targetNodePathName = pid.getInstallationPath() + ".zip";
        final String targetPathName = "jcr_root" + targetNodePathName;

        resultList.put(targetPathName, source);
        getLog().info("Embedding " + artifact.getId() + " -> " + targetPathName);
        if (nestedPackage.isGenerateFilter()) {
          filter.add(new PathFilterSet(targetNodePathName));
        }
      }
    }
  }

  /**
   * Build Package Properties XML file.
   * @param vaultFolder Folder in where the properties.xml file is written to
   * @throws IOException
   */
  private void writePropertiesFile(File vaultFolder)
      throws IOException {
    final Properties vaultProperties = new Properties();

    String description = project.getDescription();
    if (description == null) {
      description = project.getName();
      if (description == null) {
        description = project.getArtifactId();
      }
    }
    vaultProperties.put("description", description);

    // Add User Values first
    for (Object propertyKey : properties.keySet()) {
      if (properties.get(propertyKey) == null) {
        properties.put(propertyKey, "");
      }
    }

    vaultProperties.putAll(properties);

    // Package Descriptions
    vaultProperties.put(PROPERTY_GROUP, group);
    vaultProperties.put(PROPERTY_NAME, name);
    vaultProperties.put(PROPERTY_VERSION, version);

    // Artifact Description
    vaultProperties.put(PROPERTY_GROUP_ID, project.getGroupId());
    vaultProperties.put(PROPERTY_ARTIFACT_ID, project.getArtifactId());

    // Package Dependencies
    if (!dependencies.isEmpty()) {
      vaultProperties.put(PROPERTY_DEPENDENCIES, Dependency.toString(dependencies));
    }

    // User and Timestamp
    if (!vaultProperties.containsKey(PROPERTY_CREATED_BY)) {
      vaultProperties.put(PROPERTY_CREATED_BY, System.getProperty("user.name"));
    }
    vaultProperties.put(PROPERTY_CREATED, DATE_FORMAT.format(new Date()));

    // configurable properties
    vaultProperties.put(PROPERTY_REQUIRES_ROOT, String.valueOf(requiresRoot));
    vaultProperties.put(PROPERTY_ALLOW_INDEX_DEFINITIONS, String.valueOf(allowIndexDefinitions));
    vaultProperties.put(PROPERTY_PATH, ETC_PACKAGES + "/" + group + "/" + name + PACKAGE_EXT);
    vaultProperties.put(PROPERTY_AC_HANDLING, acHandling.toString());

    try (FileOutputStream fos = new FileOutputStream(new File(vaultFolder, PROPERTIES_XML));
        BufferedOutputStream bos = new BufferedOutputStream(fos)) {
      vaultProperties.storeToXML(bos, project.getName());
    }
  }

  private String resolvePath(final String path) {
    String answer = path;
    if (!answer.startsWith("/")) {
      answer = "/" + path;
      getLog().info("Relative path resolved to " + answer);
    }

    return answer;
  }

  private File getFileFromFolder(File folder, final String fileName) {
    File answer = null;
    File[] files = folder.listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String itemName) {
            return itemName.equals(fileName);
          }
        });
    if (files.length > 0) {
      answer = files[0];
    }
    return answer;
  }

  private void checkAndCopy(File vaultFolder, String fileName) throws IOException {
    if (getFileFromFolder(vaultFolder, fileName) == null) {
      InputStream ios = getClass().getResourceAsStream("/vault-file-templates/" + fileName);
      if (ios != null) {
        FileOutputStream fos = new FileOutputStream(new File(vaultFolder, fileName));
        IOUtils.copy(ios, fos);
        IOUtils.closeQuietly(ios);
        IOUtils.closeQuietly(fos);
      }
    }
  }

  private DefaultWorkspaceFilter loadFilterInFolder(File vaultFolder)
      throws IOException, ConfigurationException {
    return loadFilter(
        vaultFolder != null && vaultFolder.exists() && vaultFolder.isDirectory() ? new File(vaultFolder, FILTER_XML) : null);
  }

  private DefaultWorkspaceFilter loadFilter(File filterFile)
      throws IOException, ConfigurationException {
    DefaultWorkspaceFilter answer = null;
    InputStream in = null;
    try {
      if (filterFile != null && filterFile.exists() && !filterFile.isDirectory()) {
        in = new FileInputStream(filterFile);
      }
      if (in != null) {
        answer = new DefaultWorkspaceFilter();
        answer.load(in);
      }
    }
    finally {
      if (in != null) {
        IOUtils.closeQuietly(in);
      }
    }
    if (answer == null) {
      answer = new DefaultWorkspaceFilter();
    }
    return answer;
  }

}
