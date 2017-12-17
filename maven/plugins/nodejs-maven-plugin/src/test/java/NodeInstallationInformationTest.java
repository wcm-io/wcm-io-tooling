import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import io.wcm.maven.plugins.nodejs.installation.NodeInstallationInformation;

public class NodeInstallationInformationTest {
  private static final String LINUX = "Linux";

  @Test
  public void testGetArchitecture() throws IOException {
    String linux = System.getProperty("os.name");
    if (linux.equalsIgnoreCase(linux)) {
      String architecture = NodeInstallationInformation.getArchitecture(linux);
      Assert.assertEquals(LINUX, architecture);
    }
  }
}
