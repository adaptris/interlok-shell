package com.adaptris.crash.commands;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.crash.commands.actions.ChannelCommandAction;
import com.adaptris.crash.commands.parameters.ShowJMXDetailsOptions;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
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

@Usage("Interlok Channel Management")
@Man("The channel commands allowing you to control Interlok channels (listing, starting, stopping etc).")
public class channel extends BaseCommand {

  @Retention(RetentionPolicy.RUNTIME)
  @Argument(name = "channel")
  @Usage("The channel name.")
  private @interface ChannelArgument{
  }

  private static final long TIMEOUT = 60000L;

  @Usage("Stop an Interlok Channel")
  @Man("Stops a running Interlok channel:\n" +
       "% local connection | channel stop <channel name>\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> stop(@ChannelArgument String channelName) throws Exception {
    return new Pipe<MBeanServerConnection, String>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(ChannelCommandAction.CHANNEL_NAME_KEY, channelName);
        context.provide(ChannelCommandAction.stop.execute(connection, arguments));
      }
    };
  }

  @Usage("Start an Interlok Channel")
  @Man("Starts a stopped Interlok channel:\n" + 
       "% local connection | channel start <channel name>\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> start(@ChannelArgument String channelName) throws Exception {
    return new Pipe<MBeanServerConnection, String>() {
      @Override
      public void open(InvocationContext<String> consumer) throws Exception {
        super.open(consumer);
      }

      public void provide(MBeanServerConnection connection) throws Exception {
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(ChannelCommandAction.CHANNEL_NAME_KEY, channelName);
        context.provide(ChannelCommandAction.start.execute(connection, arguments));
      }
    };
  }

  @Usage("Restarts an Interlok Channel")
  @Man("Restarts an Interlok channel:\n" +
      "% local connection | channel restart <channel name>\n" +
      "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> restart(@ChannelArgument String channelName) throws Exception {
    return new Pipe<MBeanServerConnection, String>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(ChannelCommandAction.CHANNEL_NAME_KEY, channelName);
        context.provide(ChannelCommandAction.restart.execute(connection, arguments));
      }
    };
  }

  @Usage("List  channels")
  @Man("Lists all available Interlok Channels MBean info:\n" + 
       "% local connection | channel list\n" +
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
          for (ChannelManagerMBean c : channels) {
            table.add(InterlokCommandUtils.statusRow(c, showJmxDetails));
          }
          context.provide(table);
        } catch (Exception ex) {
          context.provide(new LabelElement(ex.getMessage()).style(Style.style(Color.red)));
        }
      }
    };
  }

}
