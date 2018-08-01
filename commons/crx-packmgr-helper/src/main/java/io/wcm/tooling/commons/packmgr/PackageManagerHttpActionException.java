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
package io.wcm.tooling.commons.packmgr;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;

/**
 * Exception during package manager HTTP actions.
 */
public final class PackageManagerHttpActionException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * @param message Message
   * @param cause Cause
   */
  public PackageManagerHttpActionException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message Message
   */
  public PackageManagerHttpActionException(String message) {
    super(message);
  }

  /**
   * Create exception instance for I/O exception.
   * @param url HTTP url called
   * @param ex I/O exception
   * @return Exception instance
   */
  public static PackageManagerHttpActionException forIOException(String url, IOException ex) {
    String message = "HTTP call to " + url + " failed: "
        + StringUtils.defaultString(ex.getMessage(), ex.getClass().getSimpleName());
    if (ex instanceof SocketTimeoutException) {
      message += " (consider to increase the socket timeout using -Dvault.httpSocketTimeoutSec)";
    }
    return new PackageManagerHttpActionException(message, ex);
  }

  /**
   * Create exception instance for I/O exception.
   * @param url HTTP url called
   * @param statusLine HTTP status line
   * @param responseString Response string or null
   * @return Exception instance
   */
  public static PackageManagerHttpActionException forHttpError(String url, StatusLine statusLine,
      String responseString) {
    String message = "HTTP call to " + url + " failed with status " + statusLine.getStatusCode()
        + " " + statusLine.getReasonPhrase()
        + (responseString != null ? "\n" + responseString : "");
    return new PackageManagerHttpActionException(message);
  }

}
