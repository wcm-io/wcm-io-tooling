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
package io.wcm.maven.plugins.nodejs.mojo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;

/**
 * Logs the output of the nodejs process
 */
final class NodejsOutputStreamHandler extends Thread {

  private static final Pattern ERROR_LOG_PATTERN = Pattern.compile(".*(ERROR|FAILED|ERR|npm error).*");
  private static final Pattern WARNING_LOG_PATTERN = Pattern.compile(".*(warn).*", Pattern.CASE_INSENSITIVE);

  private final InputStream inputStream;
  private final Log logger;

  public NodejsOutputStreamHandler(InputStream inputStream, Log logger) {
    this.inputStream = inputStream;
    this.logger = logger;
  }

  @Override
  public void run() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        if (ERROR_LOG_PATTERN.matcher(line).matches()) {
          logger.error(line);
        }
        else if (WARNING_LOG_PATTERN.matcher(line).matches()) {
          logger.warn(line);
        }
        else {
          logger.info(line);
        }
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }
}
