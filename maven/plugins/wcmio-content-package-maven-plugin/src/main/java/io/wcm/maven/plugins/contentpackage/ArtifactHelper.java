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
package io.wcm.maven.plugins.contentpackage;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.repository.RepositorySystem;

class ArtifactHelper {

  private final RepositorySystem repository;
  private final ArtifactRepository localRepository;
  private final java.util.List<ArtifactRepository> remoteRepositories;

  ArtifactHelper(RepositorySystem repository, ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories) {
    this.repository = repository;
    this.localRepository = localRepository;
    this.remoteRepositories = remoteRepositories;
  }

  public File getArtifactFile(final String artifactId, final String groupId, final String version,
      final String packaging, final String classifier, final String artifact) throws MojoFailureException, MojoExecutionException {
    // check if artifact was specified
    if ((StringUtils.isEmpty(artifactId) || StringUtils.isEmpty(groupId) || StringUtils.isEmpty(version))
        && StringUtils.isEmpty(artifact)) {
      return null;
    }

    // split up artifact string
    Artifact artifactObject;
    if (StringUtils.isEmpty(artifactId)) {
      artifactObject = getArtifactFromMavenCoordinates(artifact);
    }
    else {
      artifactObject = createArtifact(artifactId, groupId, version, packaging, classifier);
    }

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

  /**
   * Parse coordinates following definition from https://maven.apache.org/pom.html#Maven_Coordinates
   * @param artifact Artifact coordinates
   * @return Artifact object
   * @throws MojoFailureException if coordinates are semantically invalid
   */
  private Artifact getArtifactFromMavenCoordinates(final String artifact) throws MojoFailureException {

    String[] parts = StringUtils.split(artifact, ":");

    String version;
    String packaging = null;
    String classifier = null;

    switch (parts.length) {
      case 3:
        // groupId:artifactId:version
        version = parts[2];
        break;

      case 4:
        // groupId:artifactId:packaging:version
        packaging = parts[2];
        version = parts[3];
        break;

      case 5:
        // groupId:artifactId:packaging:classifier:version
        packaging = parts[2];
        classifier = parts[3];
        version = parts[4];
        break;

      default:
        throw new MojoFailureException("Invalid artifact: " + artifact);
    }

    String groupId = parts[0];
    String artifactId = parts[1];

    return createArtifact(artifactId, groupId, version, packaging, classifier);
  }

  private Artifact createArtifact(final String artifactId, final String groupId, final String version, final String packaging, String classifier) {
    if (StringUtils.isEmpty(classifier)) {
      return repository.createArtifact(groupId, artifactId, version, packaging);
    }

    return repository.createArtifactWithClassifier(groupId, artifactId, version, packaging, classifier);
  }

}
