/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.tooling.commons.packmgr.install.crx;

/**
 * Installation status of a package.
 */
enum PackageInstalledStatus {

  /**
   * Package is found in package list.
   */
  NOT_FOUND,

  /**
   * Package with the correct version is installed, no other version of the same package was installed more recently.
   */
  INSTALLED,

  /**
   * Package is uploaded, but was never installed.
   */
  UPLOADED,

  /**
   * Package is uploaded and was installed, but another version if the same packaged was installed more recently.
   */
  INSTALLED_OTHER_VERSION

}
