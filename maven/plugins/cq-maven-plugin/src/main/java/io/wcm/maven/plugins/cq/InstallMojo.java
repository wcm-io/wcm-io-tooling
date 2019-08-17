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

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.utils.cli.CommandLineException;
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
  @Parameter(property = "sling.plugin.version", required = true, defaultValue = "2.4.2")
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


  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;
  @Parameter(defaultValue = "${settings}", readonly = true)
  private Settings settings;
  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;

  @Component(role = MavenPluginManager.class)
  private MavenPluginManager pluginManager;
  @Component(role = BuildPluginManager.class)
  private BuildPluginManager buildPluginManager;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String packaging = project.getPackaging();

    // detect goal to deploy current project based on packaging
    if (StringUtils.equals(packaging, "bundle")) {
      executeSlingPluginDirectly();
    }
    else if (StringUtils.equals(packaging, "content-package")) {
      getLog().info("Install content package to instance...");
      executeWithMavenInvoker("wcmio-content-package:install");
    }
    else {
      // no supported packaging - skip processing
      getLog().info("No bundle or content-package project, skip deployment.");
    }
  }

  /**
   * Executes the sling-maven-plugin directly from the current project.
   * @throws MojoExecutionException
   */
  private void executeSlingPluginDirectly() throws MojoExecutionException {

    Plugin plugin = new Plugin();
    plugin.setGroupId("org.apache.sling");
    plugin.setArtifactId("sling-maven-plugin");
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
    } /*CHECKSTYLE:OFF*/
    catch (Throwable ex) { /*CHECKSTYLE_ON*/
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

  /**
   * Invoke maven for the current project with all it's setting and the given goal.
   * @param goal Goal
   * @throws MojoExecutionException
   */
  private void executeWithMavenInvoker(String goal) throws MojoExecutionException {
    InvocationRequest invocationRequest = new DefaultInvocationRequest();
    invocationRequest.setPomFile(project.getFile());
    invocationRequest.setGoals(Arrays.asList(goal));
    invocationRequest.setBatchMode(true);

    // take over all systems properties and profile settings from current maven execution
    invocationRequest.setShellEnvironmentInherited(true);
    invocationRequest.setLocalRepositoryDirectory(new File(settings.getLocalRepository()));
    invocationRequest.setProperties(session.getUserProperties());
    invocationRequest.setProfiles(settings.getActiveProfiles());

    Invoker invoker = new DefaultInvoker();
    setupInvokerLogger(invoker);

    try {
      InvocationResult invocationResult = invoker.execute(invocationRequest);
      if (invocationResult.getExitCode() != 0) {
        String msg = "Execution of cq:install failed, see above.";
        if (invocationResult.getExecutionException() != null) {
          msg = invocationResult.getExecutionException().getMessage();
        }
        throw new CommandLineException(msg);
      }
    }
    catch (MavenInvocationException | CommandLineException ex) {
      throw new MojoExecutionException("Failed to execute goals", ex);
    }
  }

  /**
   * Mirror maven execution log output to current maven logger.
   * @param invoker Invoker
   */
  private void setupInvokerLogger(Invoker invoker) {
    Log log = getLog();
    invoker.setOutputHandler(new InvocationOutputHandler() {
      @Override
      public void consumeLine(String line) {
        if (StringUtils.startsWith(line, "[ERROR] ")) {
          log.error(StringUtils.substringAfter(line, "[ERROR] "));
        }
        else if (StringUtils.startsWith(line, "[WARNING] ")) {
          log.warn(StringUtils.substringAfter(line, "[WARNING] "));
        }
        else if (StringUtils.startsWith(line, "[INFO] ")) {
          log.info(StringUtils.substringAfter(line, "[INFO] "));
        }
        else if (StringUtils.startsWith(line, "[DEBUG] ")) {
          log.debug(StringUtils.substringAfter(line, "[DEBUG] "));
        }
        else {
          log.info(line);
        }
      }
    });
  }

}
