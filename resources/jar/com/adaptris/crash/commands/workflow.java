package com.adaptris.crash.commands;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;
import com.adaptris.crash.commands.actions.WorkflowCommandAction;
import com.adaptris.crash.commands.parameters.ShowJMXDetailsOptions;
import org.crsh.cli.*;
import org.crsh.command.BaseCommand;
import org.crsh.command.Pipe;
import org.crsh.text.Color;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.TableElement;

import javax.management.MBeanServerConnection;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.HashMap;

@Usage("Interlok Workflow Management")
@Man("The workflow commands allowing you to control Interlok worflows (listing, starting, stopping etc).")
public class workflow extends BaseCommand {

  @Retention(RetentionPolicy.RUNTIME)
  @Option(names = {"c", "channel"})
  @Usage("The channel name.")
  private @interface ChannelOption{
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Argument(name = "workflow")
  @Usage("The workflow name.")
  private @interface WorkflowArgument{
  }

  @Usage("List workflows")
  @Man("Lists all available Interlok Workflow MBean info:\n" +
      "% local connection | workflow list\n" +
      "...")
  @Command
  public Pipe<MBeanServerConnection, Object> list(@ShowJMXDetailsOptions final Boolean showJmxDetails) throws Exception {
    return new Pipe<MBeanServerConnection, Object>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          TableElement table = new TableElement().rightCellPadding(1);
          AdapterManagerMBean adapter = InterlokCommandUtils.getAdapter(connection);
          table.add(InterlokCommandUtils.statusRow(adapter, showJmxDetails));
          Collection<ChannelManagerMBean> channels = InterlokCommandUtils.getAllChannels(connection, adapter);
          for (ChannelManagerMBean channel : channels) {
            table.add(InterlokCommandUtils.statusRow(channel, showJmxDetails));
            Collection<WorkflowManagerMBean> workflows = InterlokCommandUtils.getAllWorkflows(connection, channel);
            for (WorkflowManagerMBean workflow : workflows) {
              table.add(InterlokCommandUtils.statusRow(workflow, showJmxDetails));
            }
          }
          context.provide(table);
        } catch (Exception ex) {
          context.provide(new LabelElement(ex.getMessage()).style(Style.style(Color.red)));
        }
      }
    };
  }

  @Usage("Stop an Interlok Workflow")
  @Man("Stops a running Interlok workflow:\n" +
      "% local connection | workflow stop <workflow name>\n" +
      "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> stop(@WorkflowArgument String workflowName, @ChannelOption String channelName) throws Exception {
    return new Pipe<MBeanServerConnection, String>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(WorkflowCommandAction.WORKFLOW_NAME_KEY, workflowName);
        arguments.put(WorkflowCommandAction.CHANNEL_NAME_KEY, channelName);
        context.provide(WorkflowCommandAction.stop.execute(connection, arguments));
      }
    };
  }

  @Usage("Start an Interlok Workflow")
  @Man("Starts a stopped Interlok workflow:\n" +
      "% local connection | workflow start <workflow name>\n" +
      "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> start(@WorkflowArgument String workflowName, @ChannelOption String channelName) throws Exception {
    return new Pipe<MBeanServerConnection, String>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(WorkflowCommandAction.WORKFLOW_NAME_KEY, workflowName);
        arguments.put(WorkflowCommandAction.CHANNEL_NAME_KEY, channelName);
        context.provide(WorkflowCommandAction.start.execute(connection, arguments));
      }
    };
  }

  @Usage("Restarts an Interlok Workflow")
  @Man("Restarts an Interlok workflow:\n" +
      "% local connection | workflow restart <workflow name>\n" +
      "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> restart(@WorkflowArgument String workflowName, @ChannelOption String channelName) throws Exception {
    return new Pipe<MBeanServerConnection, String>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(WorkflowCommandAction.WORKFLOW_NAME_KEY, workflowName);
        arguments.put(WorkflowCommandAction.CHANNEL_NAME_KEY, channelName);
        context.provide(WorkflowCommandAction.restart.execute(connection, arguments));
      }
    };
  }

}