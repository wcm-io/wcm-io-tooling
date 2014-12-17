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
package io.wcm.tooling.netbeans.sightly;

import java.util.ArrayList;
import java.util.List;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;

/**
 * class used for testings
 */
public class TestClass {

  @RequestAttribute(optional = true)
  private String requestAttribute1;

  @RequestAttribute(optional = false)
  private String requestAttribute2;

  @RequestAttribute(optional = false)
  private boolean anotherRequestAttribute3;

  private String somethingPrivate;
  public String somethingPublic;

  public AnotherTestClass anotherTestClass;

  public void getVoid() {
  }

  public String getString() {
    return "";
  }

  public String someString() {
    return "";
  }

  public boolean isFoo() {
    return true;
  }

  public boolean hasFoo() {
    return false;
  }

  public AnotherTestClass getAnotherTestClass() {
    return anotherTestClass;
  }

  private String getPrivate() {
    return "";
  }

  public List getEntries() {
    return new ArrayList();
  }

  public List<AnotherTestClass> getTypedEntries() {
    return new ArrayList<>();
  }

}
