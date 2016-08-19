package com.adaptris.crash.commands;

import com.adaptris.core.util.JmxHelper;
import org.crsh.cli.Command;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;

import javax.management.MBeanServerConnection;

public class local extends BaseCommand implements AdapterConnectionCommand  {

  private transient MBeanServerConnection server;

  public local() {
    server = JmxHelper.findMBeanServer();
  }

  @Override
  @Usage("returns MBeanServerConnection connection")
  @Command
  public MBeanServerConnection connection() {
    return server;
  }
}
