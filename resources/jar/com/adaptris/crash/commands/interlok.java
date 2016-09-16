package com.adaptris.crash.commands;

import com.adaptris.core.util.JmxHelper;
import com.adaptris.crash.commands.actions.AdapterCommandAction;
import com.adaptris.crash.commands.actions.ChannelCommandAction;
import com.adaptris.crash.commands.actions.MessageInjectionCommandAction;
import com.adaptris.crash.commands.actions.WorkflowCommandAction;
import com.adaptris.crash.commands.completion.ChannelCompletion;
import com.adaptris.crash.commands.completion.WorkflowCompletion;
import com.adaptris.crash.commands.parameters.PasswordOption;
import com.adaptris.crash.commands.parameters.ShowJMXDetailsOptions;
import com.adaptris.crash.commands.parameters.UsernameOption;
import groovy.util.ScriptException;
import org.apache.commons.lang.StringUtils;
import org.crsh.cli.*;
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

@Usage("Interlok Management Command")
@Man("The interlok management command is monolithic style command allowing control of adapter, channels and workflows.")
public class interlok extends BaseCommand implements Completer{

  private static String JMX_CONNECTOR_KEY = "jmxConnector";
  private static String M_BEAN_SERVER_CONNECTION_KEY = "mBeanServerConnection";

  @Retention(RetentionPolicy.RUNTIME)
  @Usage("connects to the local JMX MBeanServerConnection")
  @Man("Connects to the local JMX MBeanServerConnection")
  @Option(names = {"l", "local"})
  private @interface LocalConnectionOption {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Argument(name = "command")
  @Usage("command action - start/stop/restart")
  @Required
  private @interface CommandArgument{
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Argument(name = "channel", completer = interlok.class)
  @Usage("channel name")
  @Required
  private @interface ChannelArgument{
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Option(names = {"c", "channel"}, completer = interlok.class)
  @Usage("channel name")
  @Required
  private @interface ChannelOption{
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Argument(name = "workflow", completer = interlok.class)
  @Usage("workflow name")
  @Required
  private @interface WorkflowArgument{
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Option(names = {"w", "workflow"}, completer = interlok.class)
  @Usage("workflow name")
  @Required
  private @interface WorkflowOption{
  }

  @Usage("connect to a JMX connection")
  @Man("Connect to JMX with a JMXServiceURL:\n" +
      "% interlok connect --username <username> --password <password> service:jmx:jmxmp://remote.server.com:5555\n" +
      "...\n" +
      "Connect to the Local JMX MBeanServerConnection:\n" +
      "% interlok connect --locally\n" +
      "...\n")
  @Command
  public String connect(
      @Usage("JMX service URL") @Argument(name = "jmxServiceUrl") String jmxServiceUrl, @LocalConnectionOption Boolean localConnection,
      @UsernameOption String username, @PasswordOption String password) throws IOException, ScriptException {
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
    Map<String, Object> env = new HashMap<String, Object>();
    if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)){
      env.put("jmx.remote.profiles", "SASL/PLAIN");
      env.put(JMXConnector.CREDENTIALS, new String[] {username, password});
    }
    JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(jmxServiceUrl), env);
    session.put(JMX_CONNECTOR_KEY, jmxConnector);
    session.put(M_BEAN_SERVER_CONNECTION_KEY, jmxConnector.getMBeanServerConnection());
    return "Connected to : "+ jmxServiceUrl +"\n";
  }

  @Usage("close the current JMX connection")
  @Man("Closes current JMX connection:\n" +
      "% interlok close")
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

  @Usage("Interlok Adapter Management")
  @Man("Stops a running Interlok adapter:\n" +
      "% interlok adapter stop \n" +
      "...\n" +
      "Starts a stopped Interlok adapter:\n" +
      "% interlok adapter start \n" +
      "...\n" +
      "Restarts an Interlok adapter:\n" +
      "% interlok adapter restart \n" +
      "...\n" +
      "Reload the adapter from configuration:\n" +
      "% interlok adapter reload \n" +
      "...\n" +
      "Reload the adapter from configuration after a VCS update:\n" +
      "% interlok adapter reloadVCS \n" +
      "...\n")
  @Command
  public String adapter(InvocationContext<Object> invocationContext, @CommandArgument @Usage("command action - start/stop/restart/reload/reloadVCS") AdapterCommandAction command, @ShowJMXDetailsOptions final Boolean showJmxDetails) throws ScriptException, IOException {
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put(AdapterCommandAction.SHOW_JMX_DETAILS_KEY, showJmxDetails);
    return command.execute(invocationContext, getMBeanServerConnection(), arguments);
  }

  @Usage("Interlok Channel Management")
  @Man("Stops a running Interlok channel:\n" +
      "% interlok channel stop <channel name>\n" +
      "...\n" +
      "Starts a stopped Interlok channel:\n" +
      "% interlok channel start <channel name>\n" +
      "...\n" +
      "Restarts an Interlok channel:\n" +
      "% interlok channel restart <channel name>\n" +
      "...\n")
  @Command
  public String channel(InvocationContext<Object> invocationContext, @CommandArgument ChannelCommandAction command,
                        @ChannelArgument String channelName) throws ScriptException, IOException {
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put(ChannelCommandAction.CHANNEL_NAME_KEY, channelName);
    return command.execute(invocationContext, getMBeanServerConnection(), arguments);
  }

  @Usage("Interlok Workflow Management")
  @Man("Stops a running Interlok workflow:\n" +
      "% interlok workflow --channel <channel name> stop <workflow name>\n" +
      "...\n" +
      "Starts a stopped Interlok workflow:\n" +
      "% interlok workflow --channel <channel name> start <workflow name>\n" +
      "...\n" +
      "Restarts an Interlok workflow:\n" +
      "% interlok workflow --channel <channel name> restart <workflow name>\n" +
      "...\n")
  @Command
  public String workflow(InvocationContext<Object> invocationContext, @CommandArgument WorkflowCommandAction command,
                         @WorkflowArgument String workflowName,  @ChannelOption String channelName) throws ScriptException {
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put(WorkflowCommandAction.CHANNEL_NAME_KEY, channelName);
    arguments.put(WorkflowCommandAction.WORKFLOW_NAME_KEY, workflowName);
    return command.execute(invocationContext, getMBeanServerConnection(), arguments);
  }

  @Usage("Interlok Workflow Message Injection")
  @Man("Send message to Interlok workflow:\n" +
      "% interlok inject-message --channel <channel name> --workflow <workflow name> send\n" +
      "...\n")
  @Command
  @Named("inject-message")
  public String injectMessage(InvocationContext<Object> invocationContext, @CommandArgument MessageInjectionCommandAction command,
                         @WorkflowOption String workflowName,  @ChannelOption String channelName) throws ScriptException {
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put(MessageInjectionCommandAction.CHANNEL_NAME_KEY, channelName);
    arguments.put(MessageInjectionCommandAction.WORKFLOW_NAME_KEY, workflowName);
    return command.execute(invocationContext, getMBeanServerConnection(), arguments);
  }


  @Override
  public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
    if (parameter.getAnnotation() instanceof ChannelArgument || parameter.getAnnotation() instanceof ChannelOption) {
      return new ChannelCompletion(getMBeanServerConnection()).complete(parameter, prefix);
    } else if (parameter.getAnnotation() instanceof WorkflowArgument || parameter.getAnnotation() instanceof WorkflowOption){
      return new WorkflowCompletion(getMBeanServerConnection()).complete(parameter,prefix);
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
