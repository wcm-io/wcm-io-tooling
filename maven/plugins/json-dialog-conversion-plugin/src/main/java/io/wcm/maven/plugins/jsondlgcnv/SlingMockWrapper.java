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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.sling.fsprovider.internal.FsMode;
import org.apache.sling.fsprovider.internal.FsResourceProvider;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.runners.model.Statement;

class SlingMockWrapper {

  private final File dialogConversionContentDir;
  private final File sourceDir;
  private final SlingContext context;

  SlingMockWrapper(File dialogConversionContentDir, File sourceDir) {
    this.dialogConversionContentDir = dialogConversionContentDir;
    this.sourceDir = sourceDir;
    context = new SlingContext(ResourceResolverType.JCR_MOCK);
  }

  void execute(Consumer<SlingContext> consumer) throws MojoExecutionException {
    try {
      context.apply(new Statement() {
        @Override
        public void evaluate() throws Throwable {
          mountDialogConversionContent();
          mountSourceDir();
          consumer.accept(context);
        }
      }, null).evaluate();
    }
    catch (Throwable ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }

  private void mountDialogConversionContent() {
    Map<String, Object> config = new HashMap<>();
    config.put("provider.fs.mode", FsMode.FILEVAULT_XML.name());
    config.put("provider.file", new File(dialogConversionContentDir, "jcr_root").getPath());
    config.put("provider.filevault.filterxml.path", new File(dialogConversionContentDir, "META-INF/vault/filter.xml").getPath());
    config.put("provider.root", "/libs/cq/dialogconversion");
    config.put("provider.checkinterval", 0);
    context.registerInjectActivateService(new FsResourceProvider(), config);
  }

  private void mountSourceDir() {
    Map<String, Object> config = new HashMap<>();
    config.put("provider.fs.mode", FsMode.INITIAL_CONTENT.name());
    config.put("provider.initial.content.import.options", "overwrite:=true;ignoreImportProviders:=xml");
    config.put("provider.file", sourceDir.getPath());
    config.put("provider.root", "/source");
    config.put("provider.checkinterval", 0);
    context.registerInjectActivateService(new FsResourceProvider(), config);
  }

}
