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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Deploy CQ5/CRX package build by this project to CRX instance via CRX Package Manager HTTP interface
 * Documentation of CRX CLI interface:
 * http://dev.day.com/docs/en/crx/current/how_to/package_manager.html#Managing%20Packages%20on%20the%20Command%20Line
 * @goal deploy-package
 * @phase install
 * @requiresProject
 * @requiresDependencyResolution runtime
 * @threadSafe
 */
public class DeployPackageMojo extends AbstractContentPackageMojo {

  /**
   * The URL of the CRX package manager
   * @parameter expression="${crx.packagemanager.url}" default-value="http://localhost:4502/crx/packmgr/service"
   * @required
   */
  private String crxPackageManagerUrl;

  /**
   * The user name to authenticate at the running CRX instance.
   * @parameter expression="${crx.user}" default-value="admin"
   * @required
   */
  private String crxUser;

  /**
   * The password to authenticate at the running CRX instance.
   * @parameter expression="${crx.password}" default-value="admin"
   * @required
   */
  private String crxPassword;

  /**
   * Install package after deployment
   * @parameter expression="${crx.install}" default-value="false"
   * @required
   */
  private boolean crxInstallPackage;

  /**
   * Force upload of CQ package (default: true).
   * If set to false a package is not uploaded if it was already uploaded before.
   * @parameter expression="${crx.forceupload}" default-value="true"
   * @required
   */
  private boolean crxForceUpload;

  /**
   * Whether to skip this step even though it has been configured in the
   * project to be executed. This property may be set by the <code>crx.deploy.skip</code> comparable to the <code>maven.test.skip</code> property to prevent
   * running the unit tests.
   * @parameter expression="${crx.deploy.skip}" default-value="false"
   * @required
   */
  private boolean crxDeployskip;

  /**
   * Number of times to retry deployment via CRX HTTP interface if it fails.
   * @parameter expression="${crx.deploy.retrycount}" default-value="0"
   * @required
   */
  private int crxDeployRetryCount;

  /**
   * Number of seconds between retries attempts.
   * @parameter expression="${crx.deploy.retrydelay}" default-value="0"
   * @required
   */
  private int crxDeployRetryDelay;

  /**
   * Generates the ZIP.
   */
  @Override
  public void execute() throws MojoExecutionException {
    deployFile(getZipFile());
  }

  protected String getCrxPackageManagerUrl() {
    // convert "legacy interface URL" with service.jsp to new CRX interface (since CRX 2.1)
    return StringUtils.replace(this.crxPackageManagerUrl, "/crx/packmgr/service.jsp", "/crx/packmgr/service");
  }

  /**
   * Deploy file via package manager
   */
  protected void deployFile(File file) throws MojoExecutionException {

    if (this.crxDeployskip) {
      return;
    }

    try {
      if (this.crxInstallPackage) {
        getLog().info("Upload and install " + file.getName() + " to " + getCrxPackageManagerUrl());
      }
      else {
        getLog().info("Upload " + file.getName() + " to " + getCrxPackageManagerUrl());
      }

      // setup http client with credentials
      HttpClient httpClient = getCrxPackageManagerHttpClient();

      // prepare post method
      PostMethod post = new PostMethod(getCrxPackageManagerUrl() + "/.json?cmd=upload");
      List<Part> parts = new ArrayList<Part>();
      parts.add(new FilePart("package", file));
      if (this.crxForceUpload) {
        parts.add(new StringPart("force", "true"));
      }
      post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), post.getParams()));

      // execute post
      JSONObject response = executePackageManagerMethodJson(httpClient, post, 0);
      boolean success = response.optBoolean("success", false);
      String msg = response.optString("msg", null);
      String path = response.optString("path", null);

      if (success) {

        if (this.crxInstallPackage) {
          getLog().info("Package uploaded, now installing...");

          post = new PostMethod(getCrxPackageManagerUrl() + "/console.html" + path + "?cmd=install");

          // execute post
          executePackageManagerMethodHtml(httpClient, post, 0);
        }
        else {
          getLog().info("Package uploaded successfully (without installing).");
        }

      }
      else if (StringUtils.startsWith(msg, "Package already exists") && !this.crxForceUpload) {
        getLog().info("Package skipped because it was already uploaded.");
      }
      else {
        throw new MojoExecutionException("Package upload failed: " + msg);
      }

    }
    catch (FileNotFoundException ex) {
      throw new MojoExecutionException("File not found: " + file.getAbsolutePath(), ex);
    }
  }

  /**
   * Set up http client with credentials
   * @return Http client
   * @throws MojoExecutionException
   */
  protected HttpClient getCrxPackageManagerHttpClient() throws MojoExecutionException {
    try {
      URI crxUri = new URI(getCrxPackageManagerUrl());
      HttpClient httpClient = new HttpClient();
      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials credentials = new UsernamePasswordCredentials(this.crxUser, this.crxPassword);
      httpClient.getState().setCredentials(new AuthScope(crxUri.getHost(), crxUri.getPort(), AuthScope.ANY_REALM), credentials);
      return httpClient;
    }
    catch (URISyntaxException ex) {
      throw new MojoExecutionException("Invalid url: " + getCrxPackageManagerUrl(), ex);
    }
  }

  /**
   * Execute CRX HTTP Package manager method and parse/output xml response.
   * @param httpClient Http client
   * @param method Get or Post method
   * @throws MojoExecutionException
   */
  protected JSONObject executePackageManagerMethodJson(HttpClient httpClient, HttpMethodBase method,
      int retryCount) throws MojoExecutionException {

    try {

      String responseString = null;
      try {
        JSONObject response = null;

        // execute method
        int httpStatus = httpClient.executeMethod(method);
        responseString = getResponseBodyAsString(method);
        if (httpStatus == HttpStatus.SC_OK) {

          // get response JSON
          if (responseString != null) {
            response = new JSONObject(responseString);
          }
          if (response == null) {
            response = new JSONObject();
            response.put("success", false);
            response.put("msg", "Invalid response (null).");
          }

        }
        else {
          response = new JSONObject();
          response.put("success", false);
          response.put("msg", responseString);
        }

        return response;
      }
      catch (HttpException ex) {
        throw new MojoExecutionException("Http method failed.", ex);
      }
      catch (IOException ex) {
        throw new MojoExecutionException("Http method failed.", ex);
      }
      catch (JSONException ex) {
        throw new MojoExecutionException("JSON operation failed:\n" + responseString, ex);
      }
      finally {
        // cleanup
        method.releaseConnection();
      }

    }
    catch (MojoExecutionException ex) {
      // retry again if configured so...
      if (retryCount < this.crxDeployRetryCount) {
        getLog().info("ERROR: " + ex.getMessage());
        getLog().debug("Package manager method execution failed.", ex);
        getLog().info("---------------");

        String msg = "Package manager method failed, try again (" + (retryCount + 1) + "/" + this.crxDeployRetryCount + ")";
        if (this.crxDeployRetryDelay > 0) {
          msg += " after " + this.crxDeployRetryDelay + " second(s)";
        }
        msg += "...";
        getLog().info(msg);
        if (this.crxDeployRetryDelay > 0) {
          try {
            Thread.sleep(this.crxDeployRetryDelay * DateUtils.MILLIS_PER_SECOND);
          }
          catch (InterruptedException ex1) {
            // ignore
          }
        }
        return executePackageManagerMethodJson(httpClient, method, retryCount + 1);
      }
      else {
        throw ex;
      }
    }
  }

  /**
   * Execute CRX HTTP Package manager method and parse/output xml response.
   * @param httpClient Http client
   * @param method Get or Post method
   * @throws MojoExecutionException
   */
  protected void executePackageManagerMethodHtml(HttpClient httpClient, HttpMethodBase method,
      int retryCount) throws MojoExecutionException {

    try {

      try {

        // execute method
        int httpStatus = httpClient.executeMethod(method);
        if (httpStatus == HttpStatus.SC_OK) {

          // get response xml
          String response = getResponseBodyAsString(method);

          // debug output whole xml
          if (getLog().isDebugEnabled()) {
            getLog().debug("CRX Package Manager Response:\n" + response);
          }

          // remove all HTML tags and special conctent
          final Pattern HTML_STYLE = Pattern.compile("<style[^<>]*>[^<>]*</style>", Pattern.MULTILINE | Pattern.DOTALL);
          final Pattern HTML_JAVASCRIPT = Pattern.compile("<script[^<>]*>[^<>]*</script>", Pattern.MULTILINE | Pattern.DOTALL);
          final Pattern HTML_ANYTAG = Pattern.compile("<[^<>]*>");

          response = HTML_STYLE.matcher(response).replaceAll("");
          response = HTML_JAVASCRIPT.matcher(response).replaceAll("");
          response = HTML_ANYTAG.matcher(response).replaceAll("");
          response = StringUtils.replace(response, "&nbsp;", " ");

          getLog().info(response);
        }
        else {
          throw new MojoExecutionException("Failure:\n" + getResponseBodyAsString(method));
        }

      }
      catch (HttpException ex) {
        throw new MojoExecutionException("Http method failed.", ex);
      }
      catch (IOException ex) {
        throw new MojoExecutionException("Http method failed.", ex);
      }
      finally {
        // cleanup
        method.releaseConnection();
      }

    }
    catch (MojoExecutionException ex) {
      // retry again if configured so...
      if (retryCount < this.crxDeployRetryCount) {
        getLog().info("ERROR: " + ex.getMessage());
        getLog().debug("Package manager method execution failed.", ex);
        getLog().info("---------------");

        String msg = "Package manager method failed, try again (" + (retryCount + 1) + "/" + this.crxDeployRetryCount + ")";
        if (this.crxDeployRetryDelay > 0) {
          msg += " after " + this.crxDeployRetryDelay + " second(s)";
        }
        msg += "...";
        getLog().info(msg);
        if (this.crxDeployRetryDelay > 0) {
          try {
            Thread.sleep(this.crxDeployRetryDelay * DateUtils.MILLIS_PER_SECOND);
          }
          catch (InterruptedException ex1) {
            // ignore
          }
        }
        executePackageManagerMethodHtml(httpClient, method, retryCount + 1);
      }
      else {
        throw ex;
      }
    }
  }

  private String getResponseBodyAsString(HttpMethodBase method) throws MojoExecutionException {
    InputStream is = null;
    try {
      is = method.getResponseBodyAsStream();
      return IOUtils.toString(is, CharEncoding.UTF_8);
    }
    catch (IOException ex) {
      throw new MojoExecutionException("Getting method response failed.", ex);
    }
    finally {
      try {
        if (is != null) {
          is.close();
        }
      }
      catch (IOException ex) {
        // ignore
      }
    }
  }

}
