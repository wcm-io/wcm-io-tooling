package io.wcm.maven.plugins.nodejs.mojo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;

/**
 * Logs the output of the nodejs process
 */
final class NodejsOutputStreamHandler extends Thread {

  private static final Pattern ERROR_LOG_PATTERN = Pattern.compile(".*(ERROR|FAILED|ERR|npm error).*");
  private static final Pattern WARNING_LOG_PATTERN = Pattern.compile(".*(warn).*", Pattern.CASE_INSENSITIVE);

  private final InputStream inputStream;
  private final Log logger;

  public NodejsOutputStreamHandler(InputStream inputStream, Log logger) {
    this.inputStream = inputStream;
    this.logger = logger;
  }

  @Override
  public void run(){
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    try {
      while((line = reader.readLine()) != null) {
        if (ERROR_LOG_PATTERN.matcher(line).matches()) {
          logger.error(line);
        }
        else if (WARNING_LOG_PATTERN.matcher(line).matches()) {
          logger.warn(line);
        }
        else {
          logger.info(line);
        }
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }
}
