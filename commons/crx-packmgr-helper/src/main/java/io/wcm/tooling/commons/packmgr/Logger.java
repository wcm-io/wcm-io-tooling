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

/**
 * Simple log interface which can be mapped to the logging framework used by the caller.
 */
public interface Logger {

  /**
   * Logs a message with debug log level.
   * @param message log this message
   */
  void debug(CharSequence message);

  /**
   * Logs an error with debug log level.
   * @param message log this message
   * @param t log this cause
   */
  void debug(CharSequence message, Throwable t);

  /**
   * Logs a message with info log level.
   * @param message log this message
   */
  void info(CharSequence message);

  /**
   * Logs an error with info log level.
   * @param message log this message
   * @param t log this cause
   */
  void info(CharSequence message, Throwable t);

  /**
   * Logs a message with warn log level.
   * @param message log this message
   */
  void warn(CharSequence message);

  /**
   * Logs an error with warn log level.
   * @param message log this message
   * @param t log this cause
   */
  void warn(CharSequence message, Throwable t);

  /**
   * Logs a message with error log level.
   * @param message log this message
   */
  void error(CharSequence message);

  /**
   * Logs an error with error log level.
   * @param message log this message
   * @param t log this cause
   */
  void error(CharSequence message, Throwable t);

  /**
   * Is debug logging currently enabled?
   * @return true if debug is enabled in the underlying logger.
   */
  boolean isDebugEnabled();

  /**
   * Is info logging currently enabled?
   * @return true if info is enabled in the underlying logger.
   */
  boolean isInfoEnabled();

  /**
   * Is warn logging currently enabled?
   * @return true if warn is enabled in the underlying logger.
   */
  boolean isWarnEnabled();

  /**
   * Is error logging currently enabled?
   * @return true if error is enabled in the underlying logger.
   */
  boolean isErrorEnabled();

}
