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
package io.wcm.maven.plugins.nodejs.installation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Wrapper around the commons compress library to decompress the zip archives
 */
public class ZipUnArchiver {

  private File archive;

  /**
   * @param archive Archive
   */
  public ZipUnArchiver(File archive) {
    this.archive = archive;
  }

  /**
   * Unarchives the archive into the base dir
   * @param baseDir Base dir
   * @throws MojoExecutionException Mojo execution exception
   */
  public void unarchive(String baseDir) throws MojoExecutionException {
    try (FileInputStream fis = new FileInputStream(archive);
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(fis)) {
      ZipArchiveEntry zipEnry = zipIn.getNextZipEntry();
      while (zipEnry != null) {
        // Create a file for this tarEntry
        final File destPath = new File(baseDir + File.separator + zipEnry.getName());
        if (zipEnry.isDirectory()) {
          destPath.mkdirs();
        }
        else {
          destPath.createNewFile();
          try (BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destPath))) {
            IOUtils.copy(zipIn, bout);
          }
        }
        zipEnry = zipIn.getNextZipEntry();
      }
    }
    catch (IOException ex) {
      throw new MojoExecutionException("Could not extract archive: " + archive.getAbsolutePath(), ex);
    }

    // delete archive after extraction
    archive.delete();
  }

}
