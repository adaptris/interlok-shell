/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.adaptris.crash.ssh.dev;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.util.SecurityUtils;
import org.crsh.auth.AuthenticationPlugin;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.ssh.term.URLKeyPairProvider;
import org.crsh.vfs.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Copied from Crash#SSHPlugin and allows us to run w/o a username / password using UserAuthNone
//
public class NoAuthSSH extends CRaSHPlugin<NoAuthSSH> {
  private final Logger log = LoggerFactory.getLogger(NoAuthSSH.class.getName());

  /** The SSH port. */
  public static final PropertyDescriptor<Integer> DEV_SSH_PORT = PropertyDescriptor.create("sshdev.port", 0, "The SSH port");
  public static final PropertyDescriptor<String> DEV_SSH_ENABLED =
      PropertyDescriptor.create("sshdev.enabled", "false", "Enable SSH dev mode");


  private ServerWrapper lifeCycle;

  @Override
  public NoAuthSSH getImplementation() {
    return this;
  }

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Arrays.<PropertyDescriptor<?>>asList(DEV_SSH_PORT, DEV_SSH_ENABLED, AuthenticationPlugin.AUTH);
  }

  @Override
  public void init() {

    if (!Boolean.valueOf(getContext().getProperty(DEV_SSH_ENABLED))) {
      log.trace("sshdev mode not enabled; nothing to to");
      return;
    }
    SecurityUtils.setRegisterBouncyCastle(true);
    Integer port = getContext().getProperty(DEV_SSH_PORT);
    if (port == null) {
      port = 0;
    }

    Resource serverKey = null;
    KeyPairProvider keyPairProvider = null;
    // Get embedded default key
    URL serverKeyURL = NoAuthSSH.class.getResource("/crash/hostkey.pem");
    if (serverKeyURL != null) {
      try {
        log.trace("Found embedded key url {}", serverKeyURL);
        serverKey = new Resource("hostkey.pem", serverKeyURL);
      } catch (IOException e) {
        log.trace("Could not load ssh key from url " + serverKeyURL, e);
      }
    }
    if (serverKeyURL == null) {
      log.trace("Could not boot SSHD due to missing server key");
      return;
    }
    if (keyPairProvider == null) {
      keyPairProvider = new URLKeyPairProvider(serverKey);
    }
    //
    log.trace("Booting devmode SSHD");
    ServerWrapper lifeCycle = new ServerWrapper(getContext(), port, keyPairProvider);
    lifeCycle.init();
    this.lifeCycle = lifeCycle;
  }

  @Override
  public void destroy() {
    if (lifeCycle != null) {
      log.trace("Shutdown devmode SSHD");
      lifeCycle.destroy();
      lifeCycle = null;
    }
  }
}
