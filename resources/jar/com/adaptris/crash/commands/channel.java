package com.adaptris.crash.commands;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.crash.commands.parameters.ShowJMXDetailsOptions;
import org.crsh.cli.*;
import org.crsh.command.InvocationContext;
import org.crsh.text.Color;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.TableElement;

import java.util.Collection;

@Usage("Interlok Channel Management")
@Man("The channel commands allowing you to control Interlok channels (listing, starting, stopping etc).")
public class channel extends AdapterBaseCommand {

  @Usage("Stop an Interlok Channel")
  @Man("Stops a running Interlok channel:\n" +
       "% channel stop <channel name>\n" + 
       "...\n")
  @Command
  public String stop(@Usage("The channel name to stop.") @Argument String channelName) throws Exception {
    try {
      getChannel(channelName).requestClose();
      return "Channel (" + channelName + ") stopped/closed";
    } catch (Exception e) {
      return "Could not stop the channel: " + e.getMessage();
    }
  }

  @Usage("Start an Interlok Channel")
  @Man("Starts a stopped Interlok channel:\n" + 
       "% channel start <channel name>\n" + 
       "...\n")
  @Command
  public String start(@Usage("The channel name to start.") @Argument String channelName) throws Exception {

    try {
      if (!isStarted(getAdapter())) {
        return "Can't start any channels while the adapter is stopped";
      }
      getChannel(channelName).requestStart();
      return "Channel (" + channelName + ") started";
    } catch (Exception e) {
      return "Could not start the channel: " + e.getMessage();
    }
  }

  @Usage("List  channels")
  @Man("Lists all available Interlok Channels MBean info:\n" + 
       "% channel list\n" +
       "...")
  @Command
  public void list(InvocationContext<Object> context, @ShowJMXDetailsOptions final Boolean showJmxDetails) throws Exception {
    
    try {
      TableElement table = new TableElement().rightCellPadding(1);
      AdapterManagerMBean adapter = getAdapter();
      table.add(listRow(adapter, showJmxDetails));
      Collection<ChannelManagerMBean> channels = getAllChannels(adapter);
      for (ChannelManagerMBean c : channels) {
        table.add(listRow(c, showJmxDetails));
      }
      context.provide(table);
    } catch (Exception ex) {
      context.provide(new LabelElement(ex.getMessage()).style(Style.style(Color.red)));
    }
  }

}
