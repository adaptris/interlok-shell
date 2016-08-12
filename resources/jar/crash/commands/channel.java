package com.adaptris.crash.commands;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.text.Color;
import org.crsh.text.RenderPrintWriter;

import com.adaptris.core.ComponentState;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.core.util.JmxHelper;

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
      getChannel(channelName).requestStart();
      return "Channel (" + channelName + ") started";
    } catch (Exception e) {
      return "Could not start the channel: " + e.getMessage();
    }
  }

  @Usage("channel listjmx")
  @Man("Lists all available Interlok Channels MBean info:\n" + 
       "% channel listjmx\n" + 
       "...\n")
  @Command
  public void listjmx(InvocationContext<ObjectName> context) throws Exception {
    Set<ObjectInstance> results = queryJmx("com.adaptris:type=Channel,*");
    for (ObjectInstance instance : results) {
      context.provide(instance.getObjectName());
    }
  }

  @Usage("channel list")
  @Man("Lists all available Interlok Channels MBean info:\n" + 
       "% channel listjmx\n" + 
       "...\n")
  @Command
  public void list(InvocationContext<ObjectName> context) throws Exception {
    Set<ObjectInstance> results = queryJmx("com.adaptris:type=Channel,*");
    RenderPrintWriter writer = context.getWriter();
    for (ObjectInstance instance : results) {
      try {
        ComponentState state = (ComponentState) server.getAttribute(instance.getObjectName(), "ComponentState");
        if (state instanceof StartedState)
          writer.print(instance.getObjectName(), Color.green);
        else if (state instanceof InitialisedState)
          writer.print(instance.getObjectName(), Color.yellow);
        else
          writer.print(instance.getObjectName(), Color.red);

        writer.print("\n");
      } catch (Exception ex) {
        writer.print(ex.getMessage(), Color.red);
      }
    }
  }

}
