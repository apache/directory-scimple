package org.apache.directory.scim.example.memory;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

public class Deployments {

  public static WebArchive projectWar() {

    // look fir first .war file in project's target dir
    Optional<File> warFile = Arrays.stream(new File("target/").listFiles((dir, name) -> name.endsWith(".war"))).findFirst();

    // fail if no war
    if (!warFile.isPresent()) {
      Assert.fail("Failed to locate war file in project's target directory");
    }

    // Use the ROOT context
    return ShrinkWrap.create(ZipImporter.class, "ROOT.war")
        .importFrom(warFile.get())
        .as(WebArchive.class);
  }
}
