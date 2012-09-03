package fr.insalyon.citi.golo.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class Metadata {

  public static final String VERSION;
  public static final String TIMESTAMP;

  static {
    Properties props = new Properties();
    InputStream inputStream = Metadata.class.getResourceAsStream("/metadata.properties");
    try {
      props.load(inputStream);
      VERSION = props.getProperty("version");
      TIMESTAMP = props.getProperty("timestamp");
    } catch (IOException e) {
      throw new AssertionError("Could not load metadata.properties from the current classpath.");
    }
  }
}
