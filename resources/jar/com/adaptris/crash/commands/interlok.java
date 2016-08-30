package com.adaptris.crash.commands;

import com.adaptris.core.util.JmxHelper;
import com.adaptris.crash.commands.actions.AdapterCommandAction;
import com.adaptris.crash.commands.completion.ChannelCompletion;
import com.adaptris.crash.commands.parameters.LocalConnectionOption;
import com.adaptris.crash.commands.parameters.ShowJMXDetailsOptions;
import groovy.util.ScriptException;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Usage;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

public class interlok extends BaseCommand implements Completer{

  private static String JMX_CONNECTOR_KEY = "jmxConnector";
  private static String M_BEAN_SERVER_CONNECTION_KEY = "mBeanServerConnection";

  @Retention(RetentionPolicy.RUNTIME)
  @Argument(name = "channel", completer = interlok.class)
  @Usage("The channel name.")
  private @interface ChannelArgument{
  }

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

    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put("showJMXDetails", showJmxDetails);
    return command.execute(invocationContext, getMBeanServerConnection(), arguments);
  }


  @Override
  public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
    if (parameter.getAnnotation() instanceof ChannelArgument) {
      return new ChannelCompletion(getMBeanServerConnection()).complete(parameter, prefix);
    } else {
      return Completion.create();
    }
  }

  private MBeanServerConnection getMBeanServerConnection() throws ScriptException {
    Map<String, Object> session = context.getSession();
    if (!session.containsKey(M_BEAN_SERVER_CONNECTION_KEY)) {
      throw new ScriptException("Not connected");
    }
   return (MBeanServerConnection)session.get(M_BEAN_SERVER_CONNECTION_KEY);
  }

}
