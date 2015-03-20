/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.maven.plugins.i18n;

import io.wcm.maven.plugins.i18n.readers.I18nReader;
import io.wcm.maven.plugins.i18n.readers.JsonI18nReader;
import io.wcm.maven.plugins.i18n.readers.PropertiesI18nReader;
import io.wcm.maven.plugins.i18n.readers.XmlI18nReader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.sling.commons.json.JSONException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Transform i18n resources in Java Properties, JSON or XML file format to Sling i18n Messages JSON or XML format.
 */
@Mojo(name = "transform", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresProject = true, threadSafe = true)
public class TransformMojo extends AbstractMojo {

  // file extensions
  private static final String FILE_EXTENSION_JSON = "json";
  private static final String FILE_EXTENSION_XML = "xml";
  private static final String FILE_EXTENSION_PROPERTIES = "properties";

  /**
   * Source path containing the i18n source .properties or .xml files.
   */
  @Parameter(defaultValue = "${basedir}/src/main/resources/i18n")
  private String source;

  /**
   * Relative target path for the generated resources.
   */
  @Parameter(defaultValue = "SLING-INF/app-root/i18n")
  private String target;

  /**
   * Output format for i18n: "json" or "xml"
   */
  @Parameter(defaultValue = "json")
  private String outputFormat;

  @Parameter(defaultValue = "generated-i18n-resources")
  private String generatedResourcesFolderPath;

  @Parameter(property = "project", required = true, readonly = true)
  private MavenProject project;

  private File generatedResourcesFolder;
  private List<File> i18nSourceFiles;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    OutputFormat selectedOutputFormat = OutputFormat.valueOf(StringUtils.upperCase(outputFormat));
    try {
      intialize();

      List<File> sourceFiles = getI18nSourceFiles();

      for (File file : sourceFiles) {
        try {
          // transform i18n files
          String languageKey = FileUtils.removeExtension(file.getName());
          I18nReader reader = getI18nReader(file);
          SlingI18nMap i18nMap = new SlingI18nMap(languageKey, reader.read(file));

          // write mappings to target file
          File targetFile = getTargetFile(file, selectedOutputFormat);
          writeTargetI18nFile(i18nMap, targetFile, selectedOutputFormat);

          getLog().info("Transformed " + file.getPath() + " to  " + targetFile.getPath());
        }
        catch (IOException | JSONException ex) {
          throw new MojoFailureException("Unable to transform i18n resource: " + file.getPath(), ex);
        }
      }
    }
    catch (IOException ex) {
      throw new MojoFailureException("Failure to transform i18n resources", ex);
    }
  }

  /**
   * Initialize parameters, which cannot get defaults from annotations. Currently only the root nodes.
   * @throws IOException
   */
  private void intialize() throws IOException {
    getLog().debug("Initializing i18n plugin...");

    // resource
    if (!getI18nSourceFiles().isEmpty()) {
      File myGeneratedResourcesFolder = getGeneratedResourcesFolder();
      addResource(myGeneratedResourcesFolder.getPath(), target);
    }

  }

  private void addResource(String sourceDirectory, String targetPath) {

    // construct resource
    Resource resource = new Resource();
    resource.setDirectory(sourceDirectory);
    resource.setTargetPath(targetPath);

    // add to build
    Build build = this.project.getBuild();
    build.addResource(resource);
    getLog().debug("Added resource: " + resource.getDirectory() + " -> " + resource.getTargetPath());
  }

  /**
   * Fetches i18n source files from source directory.
   * @return a list of XML files
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  private List<File> getI18nSourceFiles() throws IOException {

    if (i18nSourceFiles == null) {
      File sourceDirectory = getSourceDirectory();

      if (!sourceDirectory.isDirectory()) {
        i18nSourceFiles = Collections.emptyList();
      }
      else {
        // get list of xml files
        String includes = "**/*." + FILE_EXTENSION_PROPERTIES + ","
            + "**/*." + FILE_EXTENSION_XML + ","
            + "**/*." + FILE_EXTENSION_JSON;
        String excludes = FileUtils.getDefaultExcludesAsString();

        i18nSourceFiles = FileUtils.getFiles(sourceDirectory, includes, excludes);
      }
    }

    return i18nSourceFiles;
  }

  /**
   * Get directory containing source i18n files.
   * @return directory containing source i18n files.
   * @throws IOException
   */
  private File getSourceDirectory() throws IOException {
    File file = new File(source);
    if (!file.isDirectory()) {
      getLog().debug("Could not find directory at '" + source + "'");
    }
    return file.getCanonicalFile();
  }

  /**
   * Writes mappings to file in Sling compatible JSON format.
   * @param i18nMap mappings
   * @param targetfile target file
   * @param selectedOutputFormat Output format
   * @throws IOException
   * @throws JSONException
   */
  private void writeTargetI18nFile(SlingI18nMap i18nMap, File targetfile, OutputFormat selectedOutputFormat) throws IOException, JSONException {
    if (selectedOutputFormat == OutputFormat.XML) {
      FileUtils.fileWrite(targetfile, CharEncoding.UTF_8, i18nMap.getI18nXmlString());
    }
    else {
      FileUtils.fileWrite(targetfile, CharEncoding.UTF_8, i18nMap.getI18nJsonString());
    }
  }

  /**
   * Get the JSON file for source file.
   * @param sourceFile the source file
   * @param selectedOutputFormat Output format
   * @return File with name and path based on file parameter
   * @throws IOException
   */
  private File getTargetFile(File sourceFile, OutputFormat selectedOutputFormat) throws IOException {

    File sourceDirectory = getSourceDirectory();
    String relativePath = StringUtils.substringAfter(sourceFile.getAbsolutePath(), sourceDirectory.getAbsolutePath());
    String relativeTargetPath = FileUtils.removeExtension(relativePath) + "." + selectedOutputFormat.getFileExtension();

    File jsonFile = new File(getGeneratedResourcesFolder().getPath() + relativeTargetPath);

    jsonFile = jsonFile.getCanonicalFile();

    File parentDirectory = jsonFile.getParentFile();
    if (!parentDirectory.exists()) {
      parentDirectory.mkdirs();
    }

    return jsonFile;
  }

  private File getGeneratedResourcesFolder() {
    if (generatedResourcesFolder == null) {
      String generatedResourcesFolderAbsolutePath = this.project.getBuild().getDirectory() + "/" + generatedResourcesFolderPath;
      generatedResourcesFolder = new File(generatedResourcesFolderAbsolutePath);
      if (!generatedResourcesFolder.exists()) {
        generatedResourcesFolder.mkdirs();
      }
    }
    return generatedResourcesFolder;
  }

  /**
   * Get i18n reader for source file.
   * @param sourceFile Source file
   * @return I18n reader
   * @throws MojoFailureException
   */
  private I18nReader getI18nReader(File sourceFile) throws MojoFailureException {
    String extension = FileUtils.getExtension(sourceFile.getName());
    if (StringUtils.equalsIgnoreCase(extension, FILE_EXTENSION_PROPERTIES)) {
      return new PropertiesI18nReader();
    }
    if (StringUtils.equalsIgnoreCase(extension, FILE_EXTENSION_XML)) {
      return new XmlI18nReader();
    }
    if (StringUtils.equalsIgnoreCase(extension, FILE_EXTENSION_JSON)) {
      return new JsonI18nReader();
    }
    throw new MojoFailureException("Unsupported file extension '" + extension + "': " + sourceFile.getAbsolutePath());
  }

}
