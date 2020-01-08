/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package edu.umd.cs.findbugs.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Suppress SpotBugs warnings.
 */
@Retention(RetentionPolicy.CLASS)
public @interface SuppressFBWarnings {

  /**
   * @return List of SpotBugs warnings that should be suppressed for the annotated element.
   *         Possible values: category, kind or pattern.
   */
  String[] value() default {};

  /**
   * @return Optional: Document the reason why the warning is suppressed.
   */
  String justification() default "";

}
