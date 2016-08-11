package com.adaptris.crash;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.crsh.standalone.Bootstrap;
import org.crsh.vfs.FS;
import org.crsh.vfs.spi.FSDriver;
import org.crsh.vfs.spi.file.FileDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.ManagementComponent;
import com.adaptris.util.URLString;

public class CrashServerComponent implements ManagementComponent {
  
  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  private static final String CRASH_COMMAND_DIR_PROP = "crash.command.dir";
  
  private static final String CRASH_CONFIG_DIR_PROP = "crash.config.dir";

  private transient boolean allowedToStart;
  
  private transient Properties bootstrapProperties;
  
  private transient Bootstrap bootstrap;
  
  @Override
  public void init(Properties config) throws Exception {
    if((config.containsKey(CRASH_COMMAND_DIR_PROP)) && (config.containsKey(CRASH_CONFIG_DIR_PROP))) {
      allowedToStart = true;
      bootstrapProperties = config;
    } else {
      log.warn("Bootstrap properties does not contain the required CRaSH properties (" + CRASH_COMMAND_DIR_PROP + ", " + CRASH_CONFIG_DIR_PROP + ") therefore not starting CRaSH");
      allowedToStart = false;
      return;
    }  
  }

  @Override
  public void start() throws Exception {
    if(allowedToStart) {
      FS confFs = new FS();
      confFs.mount(loadDirFromConfig(bootstrapProperties, CRASH_CONFIG_DIR_PROP));
      
      FS commandFs = new FS();
      commandFs.mount(loadDirFromConfig(bootstrapProperties, CRASH_COMMAND_DIR_PROP));
      
      
      bootstrap = new Bootstrap(this.getClass().getClassLoader(), confFs, commandFs);
      bootstrap.bootstrap();
    }
  }

  private FSDriver<?> loadDirFromConfig(Properties bsProperties, String dirValue) throws NullPointerException, IOException {
    FileDriver fileDriver = new FileDriver(connectToUrl(new URLString(bsProperties.getProperty(dirValue))));
    return fileDriver;
  }

  @Override
  public void stop() throws Exception {
    bootstrap.stop();
  }

  @Override
  public void destroy() throws Exception {
    bootstrap.shutdown();
  }
  
  private File connectToUrl(URLString loc) throws IOException {
    if (loc.getProtocol() == null || "file".equals(loc.getProtocol())) {
      return connectToFile(loc.getFile());
    } else
      throw new IOException("Invalid ");
  }

  private File connectToFile(String localFile) throws IOException {
    File f = new File(localFile);
    if (f.exists()) {
      return f;
    }
    else {
      ClassLoader c = this.getClass().getClassLoader();
      URL u = c.getResource(localFile);
      if (u != null) {
        try {
          return new File(u.toURI());
        } catch (URISyntaxException e) {
          throw new IOException("Invalid file syntax.", e);
        }
      }
    }
    throw new IOException("Resource not found; " + localFile);
  }

}
