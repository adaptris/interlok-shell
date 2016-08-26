package com.adaptris.crash.commands

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Man
import org.crsh.cli.Usage

import javax.management.MBeanServerConnection
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

@Usage("Interlok (remote) JMX Connection Management")
@Man("Provides a connection to the remote JMX MBeanServerConnection")
class remote implements AdapterConnectionCommand {

  @Usage("Connect to JMX with a JMXServiceURL")
  @Man("Connect to JMX with a JMXServiceURL :\n% remote connect service:jmx:jmxmp://remote.server.com:5555\n...\n")
  @Command
  public String connect(
      @Usage("The JMX service URL") @Argument String jmxServiceUrl) {
    if (jmxConnector != null) {
      throw new ScriptException("Already connected");
    }
    if (jmxServiceUrl == null) {
      return "Connection string is mandatory\n"
    }
    jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(jmxServiceUrl), null)
    return "Connected to : $jmxServiceUrl\n"
  }


  @Usage("close the current connection")
  @Command
  public String close() {
    if (jmxConnector != null) {
      jmxConnector.close();
      jmxConnector = null;
    }
    return "Connection closed\n"
  }

  @Override
  @Usage("returns MBeanServerConnection connection")
  @Command
  MBeanServerConnection connection() {
    if (jmxConnector == null){
      throw new ScriptException("Not connected; See [remote --help].");
    }
    return jmxConnector.getMBeanServerConnection();;
  }
}