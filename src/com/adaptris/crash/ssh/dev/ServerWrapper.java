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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthNone;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.ShellFactory;
import org.crsh.ssh.term.CRaSHCommandFactory;
import org.crsh.ssh.term.scp.SCPCommandFactory;
import org.crsh.ssh.term.subsystem.SubsystemFactoryPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServerWrapper {

  private final Logger log = LoggerFactory.getLogger(NoAuthSSH.class.getName());
  private final PluginContext context;

  private final int port;

  private final Charset encoding = Charset.forName("UTF-8");
  private final KeyPairProvider keyPairProvider;

  private SshServer server;

  private Integer localPort;

  public ServerWrapper(PluginContext context, int port, KeyPairProvider keyPairProvider) {
    this.context = context;
    this.port = port;
    this.keyPairProvider = keyPairProvider;
  }

  /**
   * Returns the local part after the ssh server has been succesfully bound or null. This is useful when
   * the port is chosen at random by the system.
   *
   * @return the local port
   */
  public Integer getLocalPort() {
    return localPort;
  }

  public KeyPairProvider getKeyPairProvider() {
    return keyPairProvider;
  }

  public void init() {
    try {
      ShellFactory factory = context.getPlugin(ShellFactory.class);
      SshServer server = SshServer.setUpDefaultServer();
      server.setPort(port);
      server.setShellFactory(new CRaSHCommandFactory(factory, encoding));
      server.setCommandFactory(new SCPCommandFactory(context));
      server.setKeyPairProvider(keyPairProvider);
      ArrayList<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>(0);
      for (SubsystemFactoryPlugin plugin : context.getPlugins(SubsystemFactoryPlugin.class)) {
        namedFactoryList.add(plugin.getFactory());
      }
      server.setSubsystemFactories(namedFactoryList);
      server.setUserAuthFactories(new ArrayList<NamedFactory<UserAuth>>(Arrays.asList(new UserAuthNone.Factory())));
      log.trace("About to start SSHD (devmode)");
      server.start();
      localPort = server.getPort();
      log.info("SSHD started on port {}", localPort);
      this.server = server;
    } catch (Throwable e) {
      log.error("Could not start CRaSSHD", e);
    }
  }

  public void destroy() {
    if (server != null) {
      try {
        server.stop();
      } catch (InterruptedException e) {
      }
    }
  }
}
