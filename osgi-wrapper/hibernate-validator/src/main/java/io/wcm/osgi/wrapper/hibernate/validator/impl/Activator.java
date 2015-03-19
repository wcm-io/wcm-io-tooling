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
package io.wcm.osgi.wrapper.hibernate.validator.impl;

import java.util.Locale;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Solve ClassLoder Issues when initializing Hibernate Validator and its dependencies.
 */
public class Activator implements BundleActivator {

  @Override
  public void start(BundleContext context) throws Exception {
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    try {
      // instantiate Interpolation to initialze static dependency to ExpressionFactory in correct ClassLoader context
      new InterpolationTerm("${true}", Locale.US);
    }
    finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }
  }

  @Override
  public void stop(BundleContext context) {
    // nothing to do
  }

}
