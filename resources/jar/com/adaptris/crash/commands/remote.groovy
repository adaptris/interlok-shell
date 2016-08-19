package com.adaptris.crash.commands

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Usage

import javax.management.MBeanServerConnection
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

class remote implements AdapterConnectionCommand {

  @Usage("connect to JMX with a JMXServiceURL")
  @Command
  public String connect(
      @Usage("The JMX service URL") @Argument String jmxServiceUrl) {
    if (adapterManagerMBean != null) {
      throw new ScriptException("Already connected");
    }
    if (jmxServiceUrl == null) {
      return "Connection string is mandatory"
    }
    adapterManagerMBean = JMXConnectorFactory.connect(new JMXServiceURL(jmxServiceUrl), null).getMBeanServerConnection();
    return "Connected to : $jmxServiceUrl"
  }


  @Usage("close the current connection")
  @Command
  public String close() {
    if (adapterManagerMBean != null) {
      adapterManagerMBean = null;
    }
    return "Connection closed"
  }

  @Override
  @Usage("returns MBeanServerConnection connection")
  @Command
  MBeanServerConnection connection() {
    if (adapterManagerMBean == null){
      throw new ScriptException("Not connected; See [remote --help].");
    }
    return adapterManagerMBean;
  }
}