package dev.ancaghenade.shipmentpicturelambdavalidator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesProvider {

  InputStream inputStream;

  public Properties values() throws IOException {
    try {
      Properties properties = new java.util.Properties();
      inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
      if (inputStream != null) {
        properties.load(inputStream);
      } else {
        throw new FileNotFoundException("Property file not found in the classpath.");
      }
      return properties;
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    } finally {
      inputStream.close();
    }
    return null;
  }

  public String getProperty(String key) throws IOException {
    return values().getProperty(key);
  }
}