package com.adaptris.crash.commands;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.crash.commands.parameters.ShowJMXDetailsOptions;
import org.crsh.cli.*;
import org.crsh.command.InvocationContext;
import org.crsh.command.Pipe;
import org.crsh.text.Color;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.TableElement;

import javax.management.MBeanServerConnection;
import java.util.Collection;

@Usage("Interlok Channel Management")
@Man("The channel commands allowing you to control Interlok channels (listing, starting, stopping etc).")
public class channel extends AdapterBaseCommand {

  private static final long TIMEOUT = 60000L;

  @Usage("Stop an Interlok Channel")
  @Man("Stops a running Interlok channel:\n" +
       "% channel stop <channel name>\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> stop(@Usage("The channel name to stop.") @Argument String channelName) throws Exception {
    return new Pipe<MBeanServerConnection, String>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          getChannel(connection, channelName).requestClose(TIMEOUT);
          context.provide("Channel (" + channelName + ") stopped/closed");
        } catch (Exception e) {
          context.provide("Could not stop the channel: " + e.getMessage());
        }
      }
    };
  }

  @Usage("Start an Interlok Channel")
  @Man("Starts a stopped Interlok channel:\n" + 
       "% channel start <channel name>\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> start(@Usage("The channel name to start.") @Argument String channelName) throws Exception {
    return new Pipe<MBeanServerConnection, String>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          if (!isStarted(getAdapter(connection))) {
            context.provide("Can't start any channels while the adapter is stopped");
          }
          getChannel(connection, channelName).requestStart(TIMEOUT);
          context.provide("Channel (" + channelName + ") started");
        } catch (Exception e) {
          context.provide("Could not start the channel: " + e.getMessage());
        }
      }
    };
  }

  @Usage("List  channels")
  @Man("Lists all available Interlok Channels MBean info:\n" + 
       "% channel list\n" +
       "...")
  @Command
  public Pipe<MBeanServerConnection, Object> list(@ShowJMXDetailsOptions final Boolean showJmxDetails) throws Exception {
    return new Pipe<MBeanServerConnection, Object>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          TableElement table = new TableElement().rightCellPadding(1);
          AdapterManagerMBean adapter = getAdapter(connection);
          table.add(listRow(adapter, showJmxDetails));
          Collection<ChannelManagerMBean> channels = getAllChannels(connection, adapter);
          for (ChannelManagerMBean c : channels) {
            table.add(listRow(c, showJmxDetails));
          }
          context.provide(table);
        } catch (Exception ex) {
          context.provide(new LabelElement(ex.getMessage()).style(Style.style(Color.red)));
        }
      }
    };
  }

}
