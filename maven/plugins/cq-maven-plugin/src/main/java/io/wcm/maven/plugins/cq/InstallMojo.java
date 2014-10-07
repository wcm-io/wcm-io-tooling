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
package io.wcm.maven.plugins.cq;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Executes install phase and installs an OSGi bundle jar to a running Sling instance
 * (combines goals "install" and "sling:install").
 */
@Mojo(name = "install",
requiresDependencyResolution = ResolutionScope.COMPILE,
requiresProject = true,
threadSafe = true)
@Execute(phase = LifecyclePhase.INSTALL)
public class InstallMojo extends AbstractMojo {

  /**
   * Version of sling plugin
   */
  @Parameter(property = "sling.plugin.version", required = true, defaultValue = "2.1.0")
  private String slingPluginVersion;

  /**
   * The URL of osgi console
   */
  @Parameter(property = "sling.console.url", required = true, defaultValue = "http://localhost:8080/system/console")
  private String slingConsoleUrl;

  /**
   * The user name to authenticate at osgi console
   */
  @Parameter(property = "sling.console.user", required = true, defaultValue = "admin")
  private String slingConsoleUser;

  /**
   * The password to authenticate at osgi console
   */
  @Parameter(property = "sling.console.password", required = true, defaultValue = "admin")
  private String slingConsolePassword;

  /**
   * The Maven project.
   */
  @Parameter(property = "project", required = true, readonly = true)
  private MavenProject project;

  /**
   * The Maven session.
   */
  @Parameter(property = "session", required = true, readonly = true)
  private MavenSession session;

  /**
   * The Maven plugin manager.
   */
  @Component(role = MavenPluginManager.class)
  private MavenPluginManager pluginManager;

  /**
   * The Maven build plugin manager.
   */
  @Component(role = BuildPluginManager.class)
  private BuildPluginManager buildPluginManager;


  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    Plugin plugin = new Plugin();
    plugin.setGroupId("org.apache.sling");
    plugin.setArtifactId("maven-sling-plugin");
    plugin.setVersion(this.slingPluginVersion);

    try {
      PluginDescriptor pluginDescriptor = pluginManager.getPluginDescriptor(plugin,
          project.getRemotePluginRepositories(), session.getRepositorySession());
      MojoDescriptor mojoDescriptor = pluginDescriptor.getMojo("install");
      MojoExecution mojoExecution = new MojoExecution(pluginDescriptor.getMojo("install"));

      Xpp3Dom config = convertConfiguration(mojoDescriptor.getMojoConfiguration());
      config.getChild("slingUrl").setValue(this.slingConsoleUrl);
      config.getChild("user").setValue(this.slingConsoleUser);
      config.getChild("password").setValue(this.slingConsolePassword);
      config.getChild("mountByFS").setValue("false");
      mojoExecution.setConfiguration(config);

      buildPluginManager.executeMojo(session, mojoExecution);
    }
    catch (Throwable ex) {
      throw new MojoExecutionException("Faild to execute plugin: " + plugin, ex);
    }

  }

  private Xpp3Dom convertConfiguration(PlexusConfiguration plexusConfig) throws PlexusConfigurationException {
    Xpp3Dom config = new Xpp3Dom(plexusConfig.getName());
    config.setValue(plexusConfig.getValue());
    for (String attribute : plexusConfig.getAttributeNames()) {
      config.setAttribute(attribute, plexusConfig.getAttribute(attribute));
    }
    for (PlexusConfiguration child : plexusConfig.getChildren()) {
      config.addChild(convertConfiguration(child));
    }
    return config;
  }

}
