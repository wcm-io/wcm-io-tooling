import java.io.File;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

public class MojoTest extends AbstractMojoTestCase {
  /** {@inheritDoc} */
  protected void setUp() throws Exception {
    // required
    super.setUp();

  }

  /** {@inheritDoc} */
  protected void tearDown() throws Exception {
    // required
    super.tearDown();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMojoGoal() throws Exception {
    File pom = getTestFile("src/test/projects/1/pom.xml");
    assertNotNull(pom);
    assertTrue(pom.exists());

    Mojo myMojo = lookupMojo("run", pom);
    assertNotNull(myMojo);
    myMojo.execute();
  }
}