package com.adaptris.crash.commands;

import java.lang.management.ManagementFactory;
import java.util.*;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.crsh.cli.*;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.text.Color;
import org.crsh.text.RenderPrintWriter;

import com.adaptris.core.ComponentState;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.core.util.JmxHelper;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;

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

  @Usage("channel list")
  @Man("Lists all available Interlok Channels MBean info:\n" + 
       "% channel list\n" +
       "...\n" +
      "The results can be supplemented with JMX object names:\n" +
      "% channel list --show-jmx-details\n" +
      "...")
  @Command
  public void list(InvocationContext<Object> context,
                   @Usage("show jmx details")
                   @Option(names = {"j","show-jmx-details"})
                   final Boolean showJmxDetails) throws Exception {
    
    Collection<ChannelManagerMBean> channels = getAllChannels(getAdapter());
    try {
      TableElement table = new TableElement().rightCellPadding(1);
      for (ChannelManagerMBean c : channels) {
        RowElement channelRow = new RowElement();
        channelRow.add(new LabelElement("|-"));
        channelRow.add(new LabelElement(c.getUniqueId()).style(statusColor(c)));
        if (Boolean.TRUE.equals(showJmxDetails)) {
          channelRow.add(c.createObjectName().toString());
        }
        table.add(channelRow);
      }
      context.provide(table);
    } catch (Exception ex) {
      context.provide(new LabelElement(ex.getMessage()).style(Style.style(Color.red)));
    }
  }

}
