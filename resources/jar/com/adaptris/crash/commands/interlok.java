package com.adaptris.crash.commands;

import com.adaptris.core.util.JmxHelper;
import com.adaptris.crash.commands.actions.AdapterCommandAction;
import com.adaptris.crash.commands.parameters.LocalConnectionOption;
import com.adaptris.crash.commands.parameters.ShowJMXDetailsOptions;
import groovy.util.ScriptException;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class interlok extends BaseCommand {

  private static String JMX_CONNECTOR_KEY = "jmxConnector";
  private static String M_BEAN_SERVER_CONNECTION_KEY = "mBeanServerConnection";

  @Usage("Connect to JMX with a JMXServiceURL or use local connection")
  @Man("Connect to JMX with a JMXServiceURL:\n" +
      "% interlok connect service:jmx:jmxmp://remote.server.com:5555\n" +
      "...\n" +
      "Connect locally:\n" +
      "% interlok connect --locally\n" +
      "...\n")
  @Command
  public String connect(
      @Usage("The JMX service URL") @Argument String jmxServiceUrl, @LocalConnectionOption Boolean localConnection) throws IOException, ScriptException {
    Map<String, Object> session = context.getSession();
    if (session.containsKey(M_BEAN_SERVER_CONNECTION_KEY)) {
      throw new ScriptException("Already connected");
    }
    if (Boolean.TRUE.equals(localConnection)){
      session.put(M_BEAN_SERVER_CONNECTION_KEY, JmxHelper.findMBeanServer());
      return "Connected locally\n";
    }
    if (jmxServiceUrl == null) {
      throw new ScriptException("Connection string is mandatory");
    }
    JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(jmxServiceUrl), null);
    session.put(JMX_CONNECTOR_KEY, jmxConnector);
    session.put(M_BEAN_SERVER_CONNECTION_KEY, jmxConnector.getMBeanServerConnection());
    return "Connected to : "+ jmxServiceUrl +"\n";
  }

  @Usage("close the current connection")
  @Command
  public String close() throws IOException {
    Map<String, Object> session = context.getSession();
    session.remove(M_BEAN_SERVER_CONNECTION_KEY);
    if (session.containsKey(JMX_CONNECTOR_KEY)) {
      JMXConnector jmxConnector = (JMXConnector)session.get(JMX_CONNECTOR_KEY);
      jmxConnector.close();
      session.remove(JMX_CONNECTOR_KEY);
    }
    return "Connection closed\n";
  }

  @Command
  public String adapter(InvocationContext<Object> invocationContext, @Argument AdapterCommandAction.Commands command, @ShowJMXDetailsOptions final Boolean showJmxDetails) throws ScriptException, IOException {
    Map<String, Object> session = context.getSession();
    if (!session.containsKey(M_BEAN_SERVER_CONNECTION_KEY)) {
      throw new ScriptException("Not connected");
    }
    MBeanServerConnection mBeanServerConnection = (MBeanServerConnection)session.get(M_BEAN_SERVER_CONNECTION_KEY);
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put("showJMXDetails", showJmxDetails);
    return command.execute(invocationContext, mBeanServerConnection, arguments);

  }

}
