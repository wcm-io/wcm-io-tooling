//$URL: $
//$Id: $
package io.wcm.tooling.netbeans.sightly;

import java.util.ArrayList;
import java.util.List;

/**
 * class used for testings
 */
public class TestClass {

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
