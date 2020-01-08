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
package io.wcm.maven.plugins.jsondlgcnv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.repository.RepositorySystem;

/**
 * Converts dialog definitions in JSON format using the cq-dialog-conversion tool.
 */
@Mojo(name = "convert", requiresProject = true, threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE)
public class ConversionMojo extends AbstractMojo {

  /**
   * Source path containing Sling-Initial-Content JSON files.
   */
  @Parameter(defaultValue = "${project.basedir}/src/main/webapp/app-root", property = "convert.source")
  private File source;

  /**
   * Temporary work directory
   */
  @Parameter(defaultValue = "${project.build.directory}/json-dialog-conversion-work")
  private File workDir;

  /**
   * Ruleset for transformation
   */
  @Parameter(defaultValue = "/libs/cq/dialogconversion/rules/coral2", property = "convert.rules")
  private String rules;

  /**
   * If set to true, the affected JSON files are only reformatted, but not transformed.
   */
  @Parameter(defaultValue = "false", property = "convert.formatOnly")
  private boolean formatOnly;

  /**
   * Version of artifact com.adobe.cq:cq-dialog-conversion-content:zip
   */
  @Parameter(defaultValue = "2.0.0", property = "convert.toolVersion")
  private String dialogConversionToolVersion;

  @Component
  private RepositorySystem repository;
  @Parameter(property = "localRepository", required = true, readonly = true)
  private ArtifactRepository localRepository;
  @Parameter(property = "project.remoteArtifactRepositories", required = true, readonly = true)
  private java.util.List<ArtifactRepository> remoteRepositories;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      initialize();

      File dialogConversionContent = getDialogConversionContentDir();

      SlingMockWrapper wrapper = new SlingMockWrapper(dialogConversionContent, source);
      wrapper.execute(context -> {
        DialogConverter converter = new DialogConverter(context, rules, getLog());
        if (formatOnly) {
          converter.format();
        }
        else {
          converter.convert();
        }
      });
    }
    catch (IOException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }

  private void initialize() throws IOException {
    // cleanup work dir
    if (workDir.exists()) {
      FileUtils.deleteDirectory(workDir);
    }
    workDir.mkdirs();
  }

  private File getDialogConversionContentDir() throws MojoExecutionException, IOException {
    File targetDir = new File(workDir, "cq-dialog-conversion-content");
    targetDir.mkdir();

    // get cq-dialog-conversion-content ZIP from maven artifact
    File file = getArtifactFile("com.adobe.cq", "cq-dialog-conversion-content", "zip", dialogConversionToolVersion);

    // unzip file to target dir
    try (ZipFile zipFile = new ZipFile(file)) {
      Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
      while (entries.hasMoreElements()) {
        ZipArchiveEntry entry = entries.nextElement();
        File outFile = new File(targetDir, entry.getName());
        if (entry.isDirectory()) {
          if (!outFile.exists()) {
            outFile.mkdirs();
          }
          continue;
        }
        if (outFile.exists()) {
          continue;
        }
        try (InputStream entryStream = zipFile.getInputStream(entry)) {
          FileUtils.copyInputStreamToFile(entryStream, outFile);
        }
      }
    }

    return targetDir;
  }

  private File getArtifactFile(String groupId, String artifactId, String packaging, String version) throws MojoExecutionException {
    Artifact artifactObject = repository.createArtifact(groupId, artifactId, version, packaging);

    // resolve artifact
    ArtifactResolutionRequest request = new ArtifactResolutionRequest();
    request.setArtifact(artifactObject);
    request.setLocalRepository(localRepository);
    request.setRemoteRepositories(remoteRepositories);
    ArtifactResolutionResult result = repository.resolve(request);
    if (result.isSuccess()) {
      return artifactObject.getFile();
    }
    else {
      throw new MojoExecutionException("Unable to download artifact: " + artifactObject.toString());
    }
  }

}
